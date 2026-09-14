package config.financial.lending;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class LoanLifecycleTest {

	@Test
	void approvalAndActivationAreDistinctTransitions() {
		assertThat(LoanLifecycle.canApprove(LoanStatus.PENDING)).isTrue();
		assertThat(LoanLifecycle.canApprove(LoanStatus.APPROVED)).isFalse();
		assertThat(LoanLifecycle.canActivate(LoanStatus.APPROVED)).isTrue();
		assertThat(LoanLifecycle.canActivate(LoanStatus.PENDING)).isFalse();
	}

	@Test
	void onlyOperationalLoanStatusesAcceptCollections() {
		assertThat(LoanLifecycle.canCollect(LoanStatus.ACTIVE)).isTrue();
		assertThat(LoanLifecycle.canCollect(LoanStatus.PARTIALLY_PAID)).isTrue();
		assertThat(LoanLifecycle.canCollect(LoanStatus.OVERDUE)).isTrue();
		assertThat(LoanLifecycle.canCollect(LoanStatus.PENDING)).isFalse();
		assertThat(LoanLifecycle.canCollect(LoanStatus.APPROVED)).isFalse();
		assertThat(LoanLifecycle.canCollect(LoanStatus.COMPLETED)).isFalse();
	}

	@Test
	void paymentStatusIsPartialUntilAllBalancesAreZero() {
		assertThat(LoanLifecycle.afterPayment(new BigDecimal("100.00"), BigDecimal.ZERO))
				.isEqualTo(LoanStatus.PARTIALLY_PAID);
		assertThat(LoanLifecycle.afterPayment(BigDecimal.ZERO, new BigDecimal("1.00")))
				.isEqualTo(LoanStatus.PARTIALLY_PAID);
		assertThat(LoanLifecycle.afterPayment(BigDecimal.ZERO, BigDecimal.ZERO))
				.isEqualTo(LoanStatus.COMPLETED);
	}

	@Test
	void reversalReturnsUntouchedLoanToActive() {
		assertThat(LoanLifecycle.afterReversal(BigDecimal.ZERO)).isEqualTo(LoanStatus.ACTIVE);
		assertThat(LoanLifecycle.afterReversal(new BigDecimal("10.00")))
				.isEqualTo(LoanStatus.PARTIALLY_PAID);
	}

	@Test
	void cancelAndCloseAreRestrictedTransitions() {
		assertThat(LoanLifecycle.canCancel(LoanStatus.PENDING)).isTrue();
		assertThat(LoanLifecycle.canCancel(LoanStatus.APPROVED)).isTrue();
		assertThat(LoanLifecycle.canCancel(LoanStatus.ACTIVE)).isFalse();
		assertThat(LoanLifecycle.canClose(LoanStatus.COMPLETED)).isTrue();
		assertThat(LoanLifecycle.canClose(LoanStatus.ACTIVE)).isFalse();
		assertThat(LoanLifecycle.canMarkOverdue(LoanStatus.ACTIVE)).isTrue();
		assertThat(LoanLifecycle.canMarkOverdue(LoanStatus.OVERDUE)).isFalse();
		assertThat(LoanLifecycle.canMarkOverdue(LoanStatus.PENDING)).isFalse();
	}
}
