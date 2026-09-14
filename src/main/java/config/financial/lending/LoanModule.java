package config.financial.lending;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
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
import config.financial.settings.BusinessSettingService;
import config.financial.common.BaseAuditableEntity;
import config.financial.lending.CalculationStrategies.LoanCalculation;
import config.financial.lending.CalculationStrategies.ScheduleLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public final class LoanModule {
	private LoanModule() {
	}
}

enum LoanStatus {
	PENDING, APPROVED, ACTIVE, PARTIALLY_PAID, COMPLETED, OVERDUE, CLOSED, CANCELLED
}

enum ScheduleStatus {
	PENDING, PARTIALLY_PAID, PAID
}

@Entity
@Table(name = "loans", schema = "finance")
@Getter
@Setter
class Loan extends BaseAuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "loan_number", nullable = false, unique = true, length = 40)
	private String loanNumber;
	@Column(name = "customer_id", nullable = false)
	private Long customerId;
	@Column(name = "loan_product_id", nullable = false)
	private Long loanProductId;
	@Enumerated(EnumType.STRING)
	@Column(name = "collection_frequency", nullable = false)
	private CollectionFrequency collectionFrequency;
	@Enumerated(EnumType.STRING)
	@Column(name = "interest_method", nullable = false)
	private InterestMethod interestMethod;
	@Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal principalAmount;
	@Column(name = "interest_rate", nullable = false, precision = 9, scale = 4)
	private BigDecimal interestRate;
	@Column(name = "interest_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal interestAmount;
	@Column(name = "disbursed_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal disbursedAmount;
	@Column(name = "collection_amount", precision = 19, scale = 2)
	private BigDecimal collectionAmount;
	@Column(name = "term_count")
	private Integer termCount;
	@Column(name = "remaining_term_count")
	private Integer remainingTermCount;
	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;
	@Column(name = "maturity_date")
	private LocalDate maturityDate;
	@Column(name = "outstanding_principal", nullable = false, precision = 19, scale = 2)
	private BigDecimal outstandingPrincipal;
	@Column(name = "outstanding_interest", nullable = false, precision = 19, scale = 2)
	private BigDecimal outstandingInterest;
	@Column(name = "total_collected", nullable = false, precision = 19, scale = 2)
	private BigDecimal totalCollected;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LoanStatus status;
	@Column(name = "approved_by")
	private String approvedBy;
	@Column(name = "approved_at")
	private Instant approvedAt;
}

@Entity
@Table(name = "loan_schedules", schema = "finance")
@Getter
@Setter
class LoanSchedule extends BaseAuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "loan_id", nullable = false)
	private Long loanId;
	@Column(name = "installment_number", nullable = false)
	private Integer installmentNumber;
	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;
	@Column(name = "principal_due", nullable = false, precision = 19, scale = 2)
	private BigDecimal principalDue;
	@Column(name = "interest_due", nullable = false, precision = 19, scale = 2)
	private BigDecimal interestDue;
	@Column(name = "total_due", nullable = false, precision = 19, scale = 2)
	private BigDecimal totalDue;
	@Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal paidAmount;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ScheduleStatus status;
	@Column(name = "paid_at")
	private Instant paidAt;
}

interface LoanRepository extends JpaRepository<Loan, Long> {
	boolean existsByLoanNumberIgnoreCase(String number);

	@Query("""
			select l from Loan l
			where (:status is null or l.status = :status)
			  and (:customerId is null or l.customerId = :customerId)
			""")
	Page<Loan> search(@Param("status") LoanStatus status, @Param("customerId") Long customerId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select l from Loan l where l.id = :id")
	java.util.Optional<Loan> findByIdForUpdate(@Param("id") Long id);
}

interface LoanScheduleRepository extends JpaRepository<LoanSchedule, Long> {
	List<LoanSchedule> findByLoanIdOrderByInstallmentNumber(Long loanId);

	@Query("""
			select distinct s.loanId from LoanSchedule s
			where s.status <> :paid and s.dueDate < :cutoff
			""")
	List<Long> findLoanIdsWithUnpaidDuesBefore(
			@Param("cutoff") LocalDate cutoff,
			@Param("paid") ScheduleStatus paid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from LoanSchedule s where s.loanId = :loanId order by s.installmentNumber")
	List<LoanSchedule> findByLoanIdForUpdate(@Param("loanId") Long loanId);
}

record LoanCreateRequest(
		@Size(max = 40) String loanNumber,
		@NotNull Long customerId,
		@NotNull Long loanProductId,
		@NotNull @DecimalMin("0.01") BigDecimal principalAmount,
		@DecimalMin("0.0") BigDecimal interestRate,
		@Positive Integer termCount,
		@NotNull LocalDate startDate) {
}

record ApprovalRequest(@Size(max = 100) String approvedBy) {
}

record StatusChangeRequest(@Size(max = 500) String reason) {
}

record ScheduleResponse(Long id, Integer installmentNumber, LocalDate dueDate,
		BigDecimal principalDue, BigDecimal interestDue, BigDecimal totalDue,
		BigDecimal paidAmount, ScheduleStatus status) {
	static ScheduleResponse from(LoanSchedule s) {
		return new ScheduleResponse(s.getId(), s.getInstallmentNumber(), s.getDueDate(), s.getPrincipalDue(),
				s.getInterestDue(), s.getTotalDue(), s.getPaidAmount(), s.getStatus());
	}
}

record LoanResponse(Long id, String loanNumber, Long customerId, Long loanProductId,
		CollectionFrequency collectionFrequency, InterestMethod interestMethod,
		BigDecimal principalAmount, BigDecimal interestRate, BigDecimal interestAmount,
		BigDecimal disbursedAmount, BigDecimal collectionAmount, Integer termCount,
		Integer remainingTermCount, LocalDate startDate, LocalDate maturityDate,
		BigDecimal outstandingPrincipal, BigDecimal outstandingInterest,
		BigDecimal totalCollected, LoanStatus status, String approvedBy, Instant approvedAt,
		Long version, List<ScheduleResponse> schedules) {
	static LoanResponse from(Loan loan, List<LoanSchedule> schedules) {
		return new LoanResponse(loan.getId(), loan.getLoanNumber(), loan.getCustomerId(), loan.getLoanProductId(),
				loan.getCollectionFrequency(), loan.getInterestMethod(), loan.getPrincipalAmount(),
				loan.getInterestRate(), loan.getInterestAmount(), loan.getDisbursedAmount(),
				loan.getCollectionAmount(), loan.getTermCount(), loan.getRemainingTermCount(),
				loan.getStartDate(), loan.getMaturityDate(), loan.getOutstandingPrincipal(),
				loan.getOutstandingInterest(), loan.getTotalCollected(), loan.getStatus(),
				loan.getApprovedBy(), loan.getApprovedAt(), loan.getVersion(),
				schedules.stream().map(ScheduleResponse::from).toList());
	}
}

@Service
@RequiredArgsConstructor
class LoanService {
	private final LoanRepository loanRepository;
	private final LoanScheduleRepository scheduleRepository;
	private final CustomerServiceV2 customerService;
	private final LoanProductService productService;
	private final AuditService auditService;
	private final BusinessSettingService settings;

	@Transactional
	LoanResponse create(LoanCreateRequest request) {
		String loanNumber = supplied(request.loanNumber()) ? request.loanNumber().trim() : generateLoanNumber();
		if (loanRepository.existsByLoanNumberIgnoreCase(loanNumber)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Loan number already exists");
		}
		customerService.required(request.customerId());
		LoanProduct product = productService.required(request.loanProductId());
		if (!product.isActive()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan product is inactive");
		}
		BigDecimal principal = Money.round(request.principalAmount());
		if (product.getMinAmount() != null && principal.compareTo(product.getMinAmount()) < 0
				|| product.getMaxAmount() != null && principal.compareTo(product.getMaxAmount()) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Principal is outside product limits");
		}
		int terms = request.termCount() == null ? requiredTerms(product) : request.termCount();
		BigDecimal rate = request.interestRate() == null ? product.getDefaultInterestRate() : request.interestRate();
		LoanCalculation calculation = strategy(product.getCollectionFrequency())
				.calculate(principal, rate, terms, request.startDate());
		Loan loan = new Loan();
		loan.setLoanNumber(loanNumber);
		loan.setCustomerId(request.customerId());
		loan.setLoanProductId(request.loanProductId());
		loan.setCollectionFrequency(product.getCollectionFrequency());
		loan.setInterestMethod(product.getInterestMethod());
		loan.setPrincipalAmount(principal);
		loan.setInterestRate(rate);
		loan.setInterestAmount(calculation.interestAmount());
		loan.setDisbursedAmount(calculation.disbursedAmount());
		loan.setCollectionAmount(calculation.schedules().get(0).totalDue());
		loan.setTermCount(terms);
		loan.setRemainingTermCount(terms);
		loan.setStartDate(request.startDate());
		loan.setMaturityDate(calculation.schedules().get(calculation.schedules().size() - 1).dueDate());
		loan.setOutstandingPrincipal(principal);
		// Daily interest is settled by deduction before disbursement, not collected again.
		loan.setOutstandingInterest(product.getCollectionFrequency() == CollectionFrequency.DAILY
				? BigDecimal.ZERO.setScale(2)
				: calculation.interestAmount());
		loan.setTotalCollected(BigDecimal.ZERO.setScale(2));
		loan.setStatus(LoanStatus.PENDING);
		loan = loanRepository.save(loan);
		LoanResponse response = LoanResponse.from(loan, List.of());
		auditService.record("Loan", loan.getId(), AuditAction.CREATE, null, response);
		return response;
	}

	@Transactional
	LoanResponse approve(Long id, ApprovalRequest request) {
		Loan loan = loanRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		if (!LoanLifecycle.canApprove(loan.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending loans can be approved");
		}
		LoanResponse old = LoanResponse.from(loan, List.of());
		loan.setStatus(LoanStatus.APPROVED);
		loan.setApprovedBy(request != null && request.approvedBy() != null ? request.approvedBy() : currentUser());
		loan.setApprovedAt(Instant.now());
		loanRepository.save(loan);
		LoanResponse response = LoanResponse.from(loan, List.of());
		auditService.record("Loan", id, AuditAction.APPROVE, old, response);
		auditService.record("Loan", id, AuditAction.STATUS_CHANGE, LoanStatus.PENDING, LoanStatus.APPROVED);
		return response;
	}

	@Transactional
	LoanResponse activate(Long id) {
		Loan loan = loanRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		if (!LoanLifecycle.canActivate(loan.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only approved loans can be activated");
		}
		if (!scheduleRepository.findByLoanIdOrderByInstallmentNumber(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Loan schedule already exists");
		}
		LoanCalculation calculation = strategy(loan.getCollectionFrequency()).calculate(loan.getPrincipalAmount(),
				loan.getInterestRate(), loan.getTermCount(), loan.getStartDate());
		List<LoanSchedule> schedules = calculation.schedules().stream()
				.map(line -> toEntity(loan.getId(), line)).toList();
		schedules = scheduleRepository.saveAll(schedules);
		loan.setStatus(LoanStatus.ACTIVE);
		loanRepository.save(loan);
		LoanResponse response = LoanResponse.from(loan, schedules);
		auditService.record("Loan", id, AuditAction.STATUS_CHANGE, LoanStatus.APPROVED, LoanStatus.ACTIVE);
		return response;
	}

	@Transactional(readOnly = true)
	Page<LoanResponse> list(LoanStatus status, Long customerId, Pageable pageable) {
		return loanRepository.search(status, customerId, pageable)
				.map(loan -> LoanResponse.from(loan, scheduleRepository.findByLoanIdOrderByInstallmentNumber(loan.getId())));
	}

	@Transactional(readOnly = true)
	LoanResponse get(Long id) {
		Loan loan = loanRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		return LoanResponse.from(loan, scheduleRepository.findByLoanIdOrderByInstallmentNumber(id));
	}

	@Transactional(readOnly = true)
	List<ScheduleResponse> schedule(Long id) {
		if (!loanRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found");
		}
		return scheduleRepository.findByLoanIdOrderByInstallmentNumber(id).stream()
				.map(ScheduleResponse::from).toList();
	}

	@Transactional
	LoanResponse cancel(Long id, StatusChangeRequest request) {
		Loan loan = loanRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		if (!LoanLifecycle.canCancel(loan.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending or approved loans can be cancelled");
		}
		LoanStatus previous = loan.getStatus();
		loan.setStatus(LoanStatus.CANCELLED);
		loanRepository.save(loan);
		LoanResponse response = LoanResponse.from(loan, List.of());
		auditService.record("Loan", id, AuditAction.STATUS_CHANGE, previous, LoanStatus.CANCELLED);
		if (request != null && request.reason() != null) {
			auditService.record("Loan", id, AuditAction.UPDATE, previous, request.reason());
		}
		return response;
	}

	@Transactional
	LoanResponse close(Long id, StatusChangeRequest request) {
		Loan loan = loanRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		if (!LoanLifecycle.canClose(loan.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only completed loans can be closed");
		}
		loan.setStatus(LoanStatus.CLOSED);
		loanRepository.save(loan);
		LoanResponse response = LoanResponse.from(loan, scheduleRepository.findByLoanIdOrderByInstallmentNumber(id));
		auditService.record("Loan", id, AuditAction.STATUS_CHANGE, LoanStatus.COMPLETED, LoanStatus.CLOSED);
		if (request != null && request.reason() != null) {
			auditService.record("Loan", id, AuditAction.UPDATE, LoanStatus.COMPLETED, request.reason());
		}
		return response;
	}

	@Transactional
	int markOverdueLoans(LocalDate asOf) {
		int graceDays = 0;
		try {
			graceDays = Math.max(0, settings.getInteger("GRACE_PERIOD_DAYS"));
		} catch (RuntimeException ignored) {
			graceDays = 0;
		}
		LocalDate cutoff = asOf.minusDays(graceDays);
		List<Long> loanIds = scheduleRepository.findLoanIdsWithUnpaidDuesBefore(cutoff, ScheduleStatus.PAID);
		int updated = 0;
		for (Long loanId : loanIds) {
			Loan loan = loanRepository.findByIdForUpdate(loanId).orElse(null);
			if (loan == null || !LoanLifecycle.canMarkOverdue(loan.getStatus())) {
				continue;
			}
			LoanStatus previous = loan.getStatus();
			loan.setStatus(LoanStatus.OVERDUE);
			loanRepository.save(loan);
			auditService.record("Loan", loanId, AuditAction.STATUS_CHANGE, previous, LoanStatus.OVERDUE);
			updated++;
		}
		return updated;
	}

	private int requiredTerms(LoanProduct product) {
		if (product.getDefaultTermCount() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Term count is required");
		}
		return product.getDefaultTermCount();
	}

	private CalculationStrategies.Strategy strategy(CollectionFrequency frequency) {
		return switch (frequency) {
			case DAILY -> new CalculationStrategies.DailyUpfrontStrategy();
			case WEEKLY -> new CalculationStrategies.WeeklyFlatStrategy();
			case MONTHLY -> new CalculationStrategies.MonthlyReducingBalanceStrategy();
		};
	}

	private LoanSchedule toEntity(Long loanId, ScheduleLine line) {
		LoanSchedule schedule = new LoanSchedule();
		schedule.setLoanId(loanId);
		schedule.setInstallmentNumber(line.installmentNumber());
		schedule.setDueDate(line.dueDate());
		schedule.setPrincipalDue(line.principalDue());
		schedule.setInterestDue(line.interestDue());
		schedule.setTotalDue(line.totalDue());
		schedule.setPaidAmount(BigDecimal.ZERO.setScale(2));
		schedule.setStatus(ScheduleStatus.PENDING);
		return schedule;
	}

	private String currentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth == null || auth.getName() == null ? "SYSTEM" : auth.getName();
	}

	private String generateLoanNumber() {
		String candidate;
		do {
			candidate = "LN-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
		} while (loanRepository.existsByLoanNumberIgnoreCase(candidate));
		return candidate;
	}

	private boolean supplied(String value) {
		return value != null && !value.isBlank();
	}
}

@RestController
@RequestMapping("/api/v2/loans")
@RequiredArgsConstructor
class LoanController {
	private final LoanService service;

	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> create(@Valid @RequestBody LoanCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.create(request), "Loan created"));
	}

	@PostMapping("/{id}/approve")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> approve(@PathVariable Long id, @RequestBody(required = false) ApprovalRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.approve(id, request), "Loan approved"));
	}

	@PostMapping("/{id}/activate")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> activate(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.activate(id), "Loan activated"));
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> list(@RequestParam(required = false) LoanStatus status,
			@RequestParam(required = false) Long customerId, Pageable pageable) {
		return ResponseEntity.ok(ResponseBuilder.success(service.list(status, customerId, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> get(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.get(id)));
	}

	@GetMapping("/{id}/schedule")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> schedule(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.schedule(id)));
	}

	@PostMapping("/{id}/cancel")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> cancel(@PathVariable Long id, @RequestBody(required = false) StatusChangeRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.cancel(id, request), "Loan cancelled"));
	}

	@PostMapping("/{id}/close")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> close(@PathVariable Long id, @RequestBody(required = false) StatusChangeRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.close(id, request), "Loan closed"));
	}

	@PostMapping("/overdue-scan")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> overdueScan() {
		int updated = service.markOverdueLoans(LocalDate.now());
		return ResponseEntity.ok(ResponseBuilder.success(updated, updated + " loan(s) marked overdue"));
	}
}

@Component
@RequiredArgsConstructor
class LoanOverdueScheduler {
	private final LoanService loanService;

	@Scheduled(cron = "0 15 0 * * *", zone = "Asia/Kolkata")
	void scanOverdueLoans() {
		loanService.markOverdueLoans(LocalDate.now());
	}
}
