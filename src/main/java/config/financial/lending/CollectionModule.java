package config.financial.lending;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import config.DTO.Response;
import config.commonConfig.ResponseBuilder;
import config.financial.audit.AuditAction;
import config.financial.audit.AuditService;
import config.financial.common.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public final class CollectionModule {
	private CollectionModule() {
	}
}

enum PaymentMode {
	CASH, BANK_TRANSFER, UPI, CHEQUE, CARD, OTHER
}

enum PaymentAllocation {
	REGULAR, PRINCIPAL_ONLY
}

enum CollectionStatus {
	COMPLETED, REVERSED, REVERSAL
}

@Entity
@Table(name = "collections", schema = "finance")
@Getter
@Setter
class CollectionTransaction extends BaseAuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "transaction_reference", nullable = false, unique = true, length = 80)
	private String transactionReference;
	@Column(name = "loan_id", nullable = false)
	private Long loanId;
	@Column(name = "customer_id", nullable = false)
	private Long customerId;
	@Column(name = "schedule_id")
	private Long scheduleId;
	@Column(name = "collection_date", nullable = false)
	private LocalDate collectionDate;
	@Column(name = "due_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal dueAmount;
	@Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal paidAmount;
	@Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal principalAmount;
	@Column(name = "interest_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal interestAmount;
	@Column(name = "outstanding_balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal outstandingBalance;
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_mode", nullable = false)
	private PaymentMode paymentMode;
	@Column(name = "collector_id")
	private Long collectorId;
	@Column(length = 500)
	private String remarks;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CollectionStatus status;
	@Column(name = "reversal_of_id")
	private Long reversalOfId;
}

interface CollectionRepository extends JpaRepository<CollectionTransaction, Long> {
	boolean existsByTransactionReferenceIgnoreCase(String reference);
	Page<CollectionTransaction> findByLoanId(Long loanId, Pageable pageable);
	List<CollectionTransaction> findByScheduleIdAndStatus(Long scheduleId, CollectionStatus status);

	@Query(value = """
			select c from CollectionTransaction c, Loan l
			where c.loanId = l.id
			  and (:loanId is null or c.loanId = :loanId)
			  and (:fromDate is null or c.collectionDate >= :fromDate)
			  and (:toDate is null or c.collectionDate <= :toDate)
			  and (:frequency is null or l.collectionFrequency = :frequency)
			  and (:customerId is null or c.customerId = :customerId)
			  and (:collectorId is null or c.collectorId = :collectorId)
			""",
			countQuery = """
			select count(c) from CollectionTransaction c, Loan l
			where c.loanId = l.id
			  and (:loanId is null or c.loanId = :loanId)
			  and (:fromDate is null or c.collectionDate >= :fromDate)
			  and (:toDate is null or c.collectionDate <= :toDate)
			  and (:frequency is null or l.collectionFrequency = :frequency)
			  and (:customerId is null or c.customerId = :customerId)
			  and (:collectorId is null or c.collectorId = :collectorId)
			""")
	Page<CollectionTransaction> search(@Param("loanId") Long loanId,
			@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
			@Param("frequency") CollectionFrequency frequency, @Param("customerId") Long customerId,
			@Param("collectorId") Long collectorId, Pageable pageable);
}

record CollectionRequest(
		@NotNull Long loanId,
		Long scheduleId,
		@NotNull LocalDate collectionDate,
		@NotNull @DecimalMin("0.01") BigDecimal paidAmount,
		@NotNull PaymentMode paymentMode,
		PaymentAllocation allocation,
		Long collectorId,
		@Size(max = 80) String transactionReference,
		@Size(max = 500) String remarks) {
}

record ReversalRequest(
		@Size(max = 500) String reason,
		@Size(max = 80) String transactionReference,
		LocalDate reversalDate,
		Long collectorId,
		@Size(max = 500) String remarks) {
}

record CollectionResponse(Long id, String transactionReference, Long loanId, Long customerId,
		Long scheduleId, LocalDate collectionDate, BigDecimal dueAmount, BigDecimal paidAmount,
		BigDecimal principalAmount, BigDecimal interestAmount, BigDecimal outstandingBalance,
		PaymentMode paymentMode, Long collectorId, String remarks, CollectionStatus status,
		Long reversalOfId, Long version) {
	static CollectionResponse from(CollectionTransaction c) {
		return new CollectionResponse(c.getId(), c.getTransactionReference(), c.getLoanId(), c.getCustomerId(),
				c.getScheduleId(), c.getCollectionDate(), c.getDueAmount(), c.getPaidAmount(),
				c.getPrincipalAmount(), c.getInterestAmount(), c.getOutstandingBalance(), c.getPaymentMode(),
				c.getCollectorId(), c.getRemarks(), c.getStatus(), c.getReversalOfId(), c.getVersion());
	}
}

@Service
@RequiredArgsConstructor
class CollectionService {
	private final CollectionRepository collectionRepository;
	private final LoanRepository loanRepository;
	private final LoanScheduleRepository scheduleRepository;
	private final AuditService auditService;

	@Transactional
	CollectionResponse post(CollectionRequest request) {
		String reference = resolveReference(request.transactionReference(), "COL");
		Loan loan = lockedActiveLoan(request.loanId());
		LoanStatus previousLoanStatus = loan.getStatus();
		List<LoanSchedule> schedules = scheduleRepository.findByLoanIdForUpdate(loan.getId());
		PaymentAllocation allocation = request.allocation() == null ? PaymentAllocation.REGULAR : request.allocation();
		LoanSchedule schedule = allocation == PaymentAllocation.PRINCIPAL_ONLY
				? requestedSchedule(schedules, request.scheduleId())
				: selectSchedule(schedules, request.scheduleId());
		BigDecimal paid = Money.round(request.paidAmount());
		Allocation split = allocate(loan, schedule, paid, allocation);

		CollectionTransaction transaction = new CollectionTransaction();
		transaction.setTransactionReference(reference);
		transaction.setLoanId(loan.getId());
		transaction.setCustomerId(loan.getCustomerId());
		transaction.setScheduleId(schedule == null ? null : schedule.getId());
		transaction.setCollectionDate(request.collectionDate());
		transaction.setDueAmount(allocation == PaymentAllocation.PRINCIPAL_ONLY
				? loan.getOutstandingPrincipal()
				: Money.round(schedule.getTotalDue().subtract(schedule.getPaidAmount())));
		transaction.setPaidAmount(paid);
		transaction.setPrincipalAmount(split.principal());
		transaction.setInterestAmount(split.interest());
		transaction.setPaymentMode(request.paymentMode());
		transaction.setCollectorId(request.collectorId());
		transaction.setRemarks(request.remarks());
		transaction.setStatus(CollectionStatus.COMPLETED);

		if (allocation == PaymentAllocation.PRINCIPAL_ONLY) {
			applyPrincipalPayment(loan, schedules, split.principal(), request.collectionDate());
		} else {
			applyScheduledPayment(loan, schedules, schedule, split, paid);
		}
		transaction.setOutstandingBalance(Money.round(
				loan.getOutstandingPrincipal().add(loan.getOutstandingInterest())));
		transaction = collectionRepository.save(transaction);
		loanRepository.save(loan);
		scheduleRepository.saveAll(schedules);
		CollectionResponse response = CollectionResponse.from(transaction);
		auditService.record("Collection", transaction.getId(), AuditAction.PAYMENT, null, response);
		if (previousLoanStatus != loan.getStatus()) {
			auditService.record("Loan", loan.getId(), AuditAction.STATUS_CHANGE,
					previousLoanStatus, loan.getStatus());
		}
		return response;
	}

	@Transactional
	CollectionResponse reverse(Long id, ReversalRequest request) {
		String reference = resolveReference(request == null ? null : request.transactionReference(), "REV");
		CollectionTransaction original = collectionRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
		if (original.getStatus() != CollectionStatus.COMPLETED || original.getReversalOfId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Collection cannot be reversed");
		}
		CollectionResponse originalBeforeReversal = CollectionResponse.from(original);
		Loan loan = loanRepository.findByIdForUpdate(original.getLoanId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		LoanStatus previousLoanStatus = loan.getStatus();
		List<LoanSchedule> schedules = scheduleRepository.findByLoanIdForUpdate(loan.getId());
		LoanSchedule schedule = original.getScheduleId() == null ? null : schedules.stream()
				.filter(item -> item.getId().equals(original.getScheduleId())).findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

		loan.setOutstandingPrincipal(Money.round(loan.getOutstandingPrincipal().add(original.getPrincipalAmount())));
		loan.setOutstandingInterest(Money.round(loan.getOutstandingInterest().add(original.getInterestAmount())));
		loan.setTotalCollected(Money.round(loan.getTotalCollected().subtract(original.getPaidAmount())));
		if (original.getPrincipalAmount().signum() > 0 && original.getInterestAmount().signum() == 0
				&& loan.getCollectionFrequency() == CollectionFrequency.MONTHLY) {
			recalculateFutureMonthlyInterest(loan, schedules, original.getCollectionDate());
		} else if (schedule != null) {
			schedule.setPaidAmount(Money.round(schedule.getPaidAmount().subtract(original.getPaidAmount())));
			updateScheduleStatus(schedule);
		}
		loan.setStatus(LoanLifecycle.afterReversal(loan.getTotalCollected()));
		original.setStatus(CollectionStatus.REVERSED);

		CollectionTransaction reversal = new CollectionTransaction();
		reversal.setTransactionReference(reference);
		reversal.setLoanId(original.getLoanId());
		reversal.setCustomerId(original.getCustomerId());
		reversal.setScheduleId(original.getScheduleId());
		reversal.setCollectionDate(request == null || request.reversalDate() == null
				? LocalDate.now() : request.reversalDate());
		reversal.setDueAmount(original.getDueAmount());
		reversal.setPaidAmount(original.getPaidAmount().negate());
		reversal.setPrincipalAmount(original.getPrincipalAmount().negate());
		reversal.setInterestAmount(original.getInterestAmount().negate());
		reversal.setOutstandingBalance(Money.round(
				loan.getOutstandingPrincipal().add(loan.getOutstandingInterest())));
		reversal.setPaymentMode(original.getPaymentMode());
		reversal.setCollectorId(request == null ? null : request.collectorId());
		reversal.setRemarks(reversalRemarks(request));
		reversal.setStatus(CollectionStatus.REVERSAL);
		reversal.setReversalOfId(original.getId());
		reversal = collectionRepository.save(reversal);
		collectionRepository.save(original);
		loanRepository.save(loan);
		scheduleRepository.saveAll(schedules);
		CollectionResponse response = CollectionResponse.from(reversal);
		auditService.record("Collection", original.getId(), AuditAction.REVERSE,
				originalBeforeReversal, response);
		if (previousLoanStatus != loan.getStatus()) {
			auditService.record("Loan", loan.getId(), AuditAction.STATUS_CHANGE,
					previousLoanStatus, loan.getStatus());
		}
		return response;
	}

	@Transactional(readOnly = true)
	Page<CollectionResponse> list(Long loanId, LocalDate from, LocalDate to,
			CollectionFrequency frequency, Long customerId, Long collectorId, Pageable pageable) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
		}
		return collectionRepository.search(loanId, from, to, frequency, customerId, collectorId, pageable)
				.map(CollectionResponse::from);
	}

	@Transactional(readOnly = true)
	CollectionResponse get(Long id) {
		return collectionRepository.findById(id).map(CollectionResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
	}

	private Loan lockedActiveLoan(Long id) {
		Loan loan = loanRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		if (!LoanLifecycle.canCollect(loan.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Collections require an active, partially-paid, or overdue loan");
		}
		return loan;
	}

	private LoanSchedule selectSchedule(List<LoanSchedule> schedules, Long requestedId) {
		return schedules.stream()
				.filter(s -> requestedId == null ? s.getStatus() != ScheduleStatus.PAID : s.getId().equals(requestedId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No payable schedule found"));
	}

	private LoanSchedule requestedSchedule(List<LoanSchedule> schedules, Long requestedId) {
		if (requestedId == null) {
			return null;
		}
		return schedules.stream().filter(schedule -> schedule.getId().equals(requestedId)).findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Schedule does not belong to the loan"));
	}

	private Allocation allocate(Loan loan, LoanSchedule schedule, BigDecimal amount, PaymentAllocation allocation) {
		if (allocation == PaymentAllocation.PRINCIPAL_ONLY
				&& loan.getCollectionFrequency() != CollectionFrequency.MONTHLY) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Principal-only payments are supported for monthly loans");
		}
		if (allocation == PaymentAllocation.PRINCIPAL_ONLY) {
			if (amount.compareTo(loan.getOutstandingPrincipal()) > 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Principal payment exceeds outstanding principal");
			}
			return new Allocation(Money.round(amount), BigDecimal.ZERO.setScale(2));
		}
		List<CollectionTransaction> posted = collectionRepository
				.findByScheduleIdAndStatus(schedule.getId(), CollectionStatus.COMPLETED);
		BigDecimal principalPaid = posted.stream().map(CollectionTransaction::getPrincipalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal interestPaid = posted.stream().map(CollectionTransaction::getInterestAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal principalDue = Money.round(schedule.getPrincipalDue().subtract(principalPaid)).max(BigDecimal.ZERO);
		BigDecimal interestDue = Money.round(schedule.getInterestDue().subtract(interestPaid)).max(BigDecimal.ZERO);
		BigDecimal principal;
		BigDecimal interest;
		interest = amount.min(interestDue).min(loan.getOutstandingInterest());
		principal = Money.round(amount.subtract(interest)).min(principalDue).min(loan.getOutstandingPrincipal());
		if (principal.add(interest).compareTo(amount) != 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Payment exceeds the selected schedule balance");
		}
		return new Allocation(Money.round(principal), Money.round(interest));
	}

	private void applyScheduledPayment(Loan loan, List<LoanSchedule> schedules,
			LoanSchedule schedule, Allocation split, BigDecimal paid) {
		loan.setOutstandingPrincipal(Money.round(loan.getOutstandingPrincipal().subtract(split.principal())));
		loan.setOutstandingInterest(Money.round(loan.getOutstandingInterest().subtract(split.interest())));
		loan.setTotalCollected(Money.round(loan.getTotalCollected().add(paid)));
		schedule.setPaidAmount(Money.round(schedule.getPaidAmount().add(paid)));
		updateScheduleStatus(schedule);
		updateLoanAfterPayment(loan, schedules);
	}

	private void applyPrincipalPayment(Loan loan, List<LoanSchedule> schedules,
			BigDecimal principal, LocalDate paymentDate) {
		loan.setOutstandingPrincipal(Money.round(loan.getOutstandingPrincipal().subtract(principal)));
		loan.setTotalCollected(Money.round(loan.getTotalCollected().add(principal)));
		recalculateFutureMonthlyInterest(loan, schedules, paymentDate);
		updateLoanAfterPayment(loan, schedules);
	}

	private void recalculateFutureMonthlyInterest(Loan loan, List<LoanSchedule> schedules, LocalDate effectiveDate) {
		BigDecimal revisedInterest = CalculationStrategies.monthlyInterest(
				loan.getOutstandingPrincipal(), loan.getInterestRate());
		BigDecimal outstandingDelta = BigDecimal.ZERO;
		for (LoanSchedule schedule : schedules) {
			if (schedule.getStatus() == ScheduleStatus.PAID || !schedule.getDueDate().isAfter(effectiveDate)) {
				continue;
			}
			BigDecimal oldRemaining = Money.round(schedule.getInterestDue().subtract(schedule.getPaidAmount()))
					.max(BigDecimal.ZERO);
			schedule.setPrincipalDue(BigDecimal.ZERO.setScale(2));
			schedule.setInterestDue(revisedInterest);
			schedule.setTotalDue(revisedInterest);
			updateScheduleStatus(schedule);
			BigDecimal newRemaining = Money.round(revisedInterest.subtract(schedule.getPaidAmount()))
					.max(BigDecimal.ZERO);
			outstandingDelta = outstandingDelta.add(newRemaining.subtract(oldRemaining));
		}
		loan.setOutstandingInterest(Money.round(loan.getOutstandingInterest().add(outstandingDelta))
				.max(BigDecimal.ZERO));
		loan.setInterestAmount(Money.round(loan.getInterestAmount().add(outstandingDelta))
				.max(BigDecimal.ZERO));
	}

	private void updateLoanAfterPayment(Loan loan, List<LoanSchedule> schedules) {
		loan.setRemainingTermCount((int) schedules.stream()
				.filter(schedule -> schedule.getStatus() != ScheduleStatus.PAID).count());
		loan.setStatus(LoanLifecycle.afterPayment(
				loan.getOutstandingPrincipal(), loan.getOutstandingInterest()));
		if (loan.getStatus() == LoanStatus.COMPLETED) {
			loan.setRemainingTermCount(0);
		}
	}

	private void updateScheduleStatus(LoanSchedule schedule) {
		if (schedule.getPaidAmount().signum() == 0) {
			schedule.setStatus(ScheduleStatus.PENDING);
			schedule.setPaidAt(null);
		} else if (schedule.getPaidAmount().compareTo(schedule.getTotalDue()) >= 0) {
			schedule.setStatus(ScheduleStatus.PAID);
			schedule.setPaidAt(Instant.now());
		} else {
			schedule.setStatus(ScheduleStatus.PARTIALLY_PAID);
			schedule.setPaidAt(null);
		}
	}

	private String resolveReference(String suppliedReference, String prefix) {
		if (suppliedReference != null && !suppliedReference.isBlank()) {
			String reference = suppliedReference.trim();
			if (collectionRepository.existsByTransactionReferenceIgnoreCase(reference)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction reference already exists");
			}
			return reference;
		}
		String generated;
		do {
			generated = prefix + "-" + LocalDate.now().toString().replace("-", "") + "-"
					+ UUID.randomUUID().toString().substring(0, 12).toUpperCase();
		} while (collectionRepository.existsByTransactionReferenceIgnoreCase(generated));
		return generated;
	}

	private String reversalRemarks(ReversalRequest request) {
		if (request == null) {
			return "Reversal";
		}
		String remarks = request.reason() != null && !request.reason().isBlank()
				? request.reason().trim()
				: request.remarks();
		if (remarks == null || remarks.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reversal reason is required");
		}
		if (remarks.length() > 500) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reversal reason must not exceed 500 characters");
		}
		return remarks;
	}

	private record Allocation(BigDecimal principal, BigDecimal interest) {
	}
}

@RestController
@RequestMapping("/api/v2/collections")
@RequiredArgsConstructor
class CollectionController {
	private final CollectionService service;

	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> post(@Valid @RequestBody CollectionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.post(request), "Collection posted"));
	}

	@PostMapping({"/{id}/reverse", "/{id}/reversal"})
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> reverse(@PathVariable Long id, @Valid @RequestBody ReversalRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.reverse(id, request), "Collection reversed"));
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> list(@RequestParam(required = false) Long loanId,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to,
			@RequestParam(required = false) CollectionFrequency frequency,
			@RequestParam(required = false) Long customerId,
			@RequestParam(required = false) Long collectorId,
			Pageable pageable) {
		return ResponseEntity.ok(ResponseBuilder.success(
				service.list(loanId, from, to, frequency, customerId, collectorId, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> get(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.get(id)));
	}
}
