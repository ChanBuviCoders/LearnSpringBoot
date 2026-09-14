package config.financial.lending;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import config.financial.lending.CalculationStrategies.LoanCalculation;

class CalculationStrategiesTest {
	private static final LocalDate START = LocalDate.of(2026, 1, 1);

	@Test
	void dailyLoanDeductsInterestUpfrontAndSchedulesOnlyPrincipal() {
		LoanCalculation result = new CalculationStrategies.DailyUpfrontStrategy()
				.calculate(new BigDecimal("10000"), new BigDecimal("10"), 4, START);

		assertThat(result.interestAmount()).isEqualByComparingTo("1000.00");
		assertThat(result.disbursedAmount()).isEqualByComparingTo("9000.00");
		assertThat(result.schedules()).hasSize(4);
		assertThat(result.schedules()).allSatisfy(line -> {
			assertThat(line.principalDue()).isEqualByComparingTo("2500.00");
			assertThat(line.interestDue()).isEqualByComparingTo("0.00");
		});
		assertThat(result.schedules().get(0).dueDate()).isEqualTo(START.plusDays(1));
	}

	@Test
	void weeklyLoanAllocatesFlatInterestAcrossScheduleWithoutRoundingLoss() {
		LoanCalculation result = new CalculationStrategies.WeeklyFlatStrategy()
				.calculate(new BigDecimal("10000"), new BigDecimal("12"), 7, START);

		assertThat(result.interestAmount()).isEqualByComparingTo("1200.00");
		assertThat(result.disbursedAmount()).isEqualByComparingTo("10000.00");
		assertThat(result.schedules().stream().map(line -> line.principalDue())
				.reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("10000.00");
		assertThat(result.schedules().stream().map(line -> line.interestDue())
				.reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("1200.00");
		assertThat(result.schedules().get(6).dueDate()).isEqualTo(START.plusWeeks(7));
	}

	@Test
	void monthlyLoanSchedulesInterestOnlyUntilPrincipalIsExplicitlyPaid() {
		LoanCalculation result = new CalculationStrategies.MonthlyReducingBalanceStrategy()
				.calculate(new BigDecimal("12000"), new BigDecimal("2"), 3, START);

		assertThat(result.schedules()).extracting(line -> line.interestDue())
				.containsExactly(new BigDecimal("240.00"), new BigDecimal("240.00"), new BigDecimal("240.00"));
		assertThat(result.interestAmount()).isEqualByComparingTo("720.00");
		assertThat(result.schedules().stream().map(line -> line.principalDue())
				.reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("0.00");
		assertThat(result.schedules().get(2).dueDate()).isEqualTo(START.plusMonths(3));
	}

	@Test
	void monthlyInterestRepricesFromOutstandingPrincipal() {
		assertThat(CalculationStrategies.monthlyInterest(
				new BigDecimal("80000"), new BigDecimal("2"))).isEqualByComparingTo("1600.00");
	}
}
