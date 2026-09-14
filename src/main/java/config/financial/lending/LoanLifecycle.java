package config.financial.lending;

import java.math.BigDecimal;
import java.util.EnumSet;

public final class LoanLifecycle {
	private static final EnumSet<LoanStatus> COLLECTIBLE = EnumSet.of(
			LoanStatus.ACTIVE, LoanStatus.PARTIALLY_PAID, LoanStatus.OVERDUE);

	private LoanLifecycle() {
	}

	public static boolean canCollect(LoanStatus status) {
		return COLLECTIBLE.contains(status);
	}

	public static boolean canApprove(LoanStatus status) {
		return status == LoanStatus.PENDING;
	}

	public static boolean canActivate(LoanStatus status) {
		return status == LoanStatus.APPROVED;
	}

	public static boolean canCancel(LoanStatus status) {
		return status == LoanStatus.PENDING || status == LoanStatus.APPROVED;
	}

	public static boolean canClose(LoanStatus status) {
		return status == LoanStatus.COMPLETED;
	}

	public static boolean canMarkOverdue(LoanStatus status) {
		return COLLECTIBLE.contains(status) && status != LoanStatus.OVERDUE;
	}

	public static LoanStatus afterPayment(BigDecimal outstandingPrincipal, BigDecimal outstandingInterest) {
		if (outstandingPrincipal.signum() == 0 && outstandingInterest.signum() == 0) {
			return LoanStatus.COMPLETED;
		}
		return LoanStatus.PARTIALLY_PAID;
	}

	public static LoanStatus afterReversal(BigDecimal totalCollected) {
		return totalCollected.signum() == 0 ? LoanStatus.ACTIVE : LoanStatus.PARTIALLY_PAID;
	}
}
