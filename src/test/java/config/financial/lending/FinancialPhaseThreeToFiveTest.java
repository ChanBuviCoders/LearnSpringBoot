package config.financial.lending;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import config.financial.audit.AuditService;

class FinancialPhaseThreeToFiveTest {

	@Test
	void paidOutRoundCannotBePaidAgain() {
		ChitSchemeRepository schemes = mock(ChitSchemeRepository.class);
		ChitMemberRepository members = mock(ChitMemberRepository.class);
		ChitRoundRepository rounds = mock(ChitRoundRepository.class);
		ChitContributionRepository contributions = mock(ChitContributionRepository.class);
		CustomerRepository customers = mock(CustomerRepository.class);
		AuditService audit = mock(AuditService.class);
		ChitRound paid = new ChitRound();
		paid.setId(9L);
		paid.setStatus(ChitRoundStatus.PAID_OUT);
		paid.setPayoutReference("PAY-1");
		when(rounds.findForUpdate(9L)).thenReturn(Optional.of(paid));

		ChitService service = new ChitService(schemes, members, rounds, contributions, customers, audit);

		assertThatThrownBy(() -> service.payout(9L,
				new PayoutRequest(new BigDecimal("100.00"), LocalDate.now(), "PAY-2")))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Payout is only allowed once");
	}

	@Test
	void dashboardUsesPostedCollectionsAndTodayBalances() {
		CustomerRepository customers = mock(CustomerRepository.class);
		LoanRepository loans = mock(LoanRepository.class);
		LoanProductRepository products = mock(LoanProductRepository.class);
		LoanScheduleRepository schedules = mock(LoanScheduleRepository.class);
		CollectionRepository collections = mock(CollectionRepository.class);
		ChitSchemeRepository chits = mock(ChitSchemeRepository.class);
		ChitMemberRepository members = mock(ChitMemberRepository.class);
		ChitRoundRepository rounds = mock(ChitRoundRepository.class);
		ChitContributionRepository contributions = mock(ChitContributionRepository.class);
		LocalDate today = LocalDate.of(2026, 9, 14);

		Customer customer = new Customer();
		customer.setStatus(CustomerStatus.ACTIVE);
		Loan loan = new Loan();
		loan.setStatus(LoanStatus.ACTIVE);
		loan.setPrincipalAmount(new BigDecimal("1000.00"));
		loan.setOutstandingPrincipal(new BigDecimal("600.00"));
		loan.setOutstandingInterest(new BigDecimal("40.00"));
		CollectionTransaction collection = new CollectionTransaction();
		collection.setStatus(CollectionStatus.COMPLETED);
		collection.setCollectionDate(today);
		collection.setPaidAmount(new BigDecimal("120.00"));
		collection.setPrincipalAmount(new BigDecimal("100.00"));
		collection.setInterestAmount(new BigDecimal("20.00"));
		LoanSchedule schedule = new LoanSchedule();
		schedule.setDueDate(today);
		schedule.setStatus(ScheduleStatus.PARTIALLY_PAID);
		schedule.setTotalDue(new BigDecimal("150.00"));
		schedule.setPaidAmount(new BigDecimal("50.00"));
		ChitScheme chit = new ChitScheme();
		chit.setStatus(ChitStatus.ACTIVE);
		ChitContribution contribution = new ChitContribution();
		contribution.setDueAmount(new BigDecimal("500.00"));
		contribution.setPaidAmount(new BigDecimal("300.00"));

		when(customers.findAll()).thenReturn(List.of(customer));
		when(loans.findAll()).thenReturn(List.of(loan));
		when(collections.findAll()).thenReturn(List.of(collection));
		when(schedules.findAll()).thenReturn(List.of(schedule));
		when(chits.findAll()).thenReturn(List.of(chit));
		when(contributions.findAll()).thenReturn(List.of(contribution));

		FinancialQueryService service = new FinancialQueryService(customers, loans, products, schedules,
				collections, chits, members, rounds, contributions);
		DashboardKpis result = service.dashboard(today);

		assertThat(result.totalCustomers()).isEqualTo(1);
		assertThat(result.activeLoans()).isEqualTo(1);
		assertThat(result.todayCollection()).isEqualByComparingTo("120.00");
		assertThat(result.todayPendingCollection()).isEqualByComparingTo("100.00");
		assertThat(result.totalOutstandingAmount()).isEqualByComparingTo("640.00");
		assertThat(result.pendingChitPayments()).isEqualByComparingTo("200.00");
	}
}
