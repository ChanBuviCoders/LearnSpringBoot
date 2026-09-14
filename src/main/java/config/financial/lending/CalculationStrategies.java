package config.financial.lending;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CalculationStrategies {
	public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

	private CalculationStrategies() {
	}

	public record ScheduleLine(int installmentNumber, LocalDate dueDate,
			BigDecimal principalDue, BigDecimal interestDue) {
		public BigDecimal totalDue() {
			return Money.round(principalDue.add(interestDue));
		}
	}

	public record LoanCalculation(BigDecimal interestAmount, BigDecimal disbursedAmount,
			List<ScheduleLine> schedules) {
	}

	public interface Strategy {
		LoanCalculation calculate(BigDecimal principal, BigDecimal rate, int terms, LocalDate startDate);
	}

	public static final class DailyUpfrontStrategy implements Strategy {
		@Override
		public LoanCalculation calculate(BigDecimal principal, BigDecimal rate, int terms, LocalDate startDate) {
			validate(principal, rate, terms, startDate);
			BigDecimal interest = Money.round(principal.multiply(rate).divide(ONE_HUNDRED));
			if (interest.compareTo(principal) >= 0) {
				throw new IllegalArgumentException("Upfront interest must be less than principal");
			}
			List<ScheduleLine> lines = equalPrincipal(principal, terms, startDate, FrequencyDays.DAILY,
					BigDecimal.ZERO);
			return new LoanCalculation(interest, Money.round(principal.subtract(interest)), lines);
		}
	}

	public static final class WeeklyFlatStrategy implements Strategy {
		@Override
		public LoanCalculation calculate(BigDecimal principal, BigDecimal rate, int terms, LocalDate startDate) {
			validate(principal, rate, terms, startDate);
			BigDecimal interest = Money.round(principal.multiply(rate).divide(ONE_HUNDRED));
			List<ScheduleLine> lines = equalPrincipal(principal, terms, startDate, FrequencyDays.WEEKLY, interest);
			return new LoanCalculation(interest, Money.round(principal), lines);
		}
	}

	public static final class MonthlyReducingBalanceStrategy implements Strategy {
		@Override
		public LoanCalculation calculate(BigDecimal principal, BigDecimal rate, int terms, LocalDate startDate) {
			validate(principal, rate, terms, startDate);
			BigDecimal monthlyInterest = monthlyInterest(principal, rate);
			List<ScheduleLine> lines = new ArrayList<>(terms);
			for (int i = 1; i <= terms; i++) {
				lines.add(new ScheduleLine(i, startDate.plusMonths(i), BigDecimal.ZERO.setScale(2), monthlyInterest));
			}
			return new LoanCalculation(Money.round(monthlyInterest.multiply(BigDecimal.valueOf(terms))),
					Money.round(principal), List.copyOf(lines));
		}
	}

	public static BigDecimal monthlyInterest(BigDecimal outstandingPrincipal, BigDecimal monthlyRate) {
		if (outstandingPrincipal == null || monthlyRate == null) {
			throw new IllegalArgumentException("Outstanding principal and monthly rate are required");
		}
		return Money.round(outstandingPrincipal.multiply(monthlyRate).divide(ONE_HUNDRED));
	}

	private static List<ScheduleLine> equalPrincipal(BigDecimal principal, int terms, LocalDate startDate,
			FrequencyDays frequency, BigDecimal totalInterest) {
		BigDecimal principalPart = principal.divide(BigDecimal.valueOf(terms), 2, RoundingMode.HALF_UP);
		BigDecimal interestPart = totalInterest.divide(BigDecimal.valueOf(terms), 2, RoundingMode.HALF_UP);
		BigDecimal remainingPrincipal = Money.round(principal);
		BigDecimal remainingInterest = Money.round(totalInterest);
		List<ScheduleLine> lines = new ArrayList<>(terms);
		for (int i = 1; i <= terms; i++) {
			BigDecimal p = i == terms ? remainingPrincipal : principalPart.min(remainingPrincipal);
			BigDecimal interest = i == terms ? remainingInterest : interestPart.min(remainingInterest);
			LocalDate due = frequency == FrequencyDays.DAILY ? startDate.plusDays(i) : startDate.plusWeeks(i);
			lines.add(new ScheduleLine(i, due, p, interest));
			remainingPrincipal = Money.round(remainingPrincipal.subtract(p));
			remainingInterest = Money.round(remainingInterest.subtract(interest));
		}
		return List.copyOf(lines);
	}

	private static void validate(BigDecimal principal, BigDecimal rate, int terms, LocalDate startDate) {
		if (principal == null || principal.signum() <= 0 || rate == null || rate.signum() < 0
				|| terms <= 0 || startDate == null) {
			throw new IllegalArgumentException("Principal, rate, terms and start date are invalid");
		}
	}

	private enum FrequencyDays {
		DAILY, WEEKLY
	}
}

final class Money {
	private Money() {
	}

	static BigDecimal round(BigDecimal value) {
		return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
	}
}
