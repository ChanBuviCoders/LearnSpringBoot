package config.financial.lending;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import config.DTO.Response;
import config.commonConfig.ResponseBuilder;
import lombok.RequiredArgsConstructor;

public final class FinancialInsightsModule {
	private FinancialInsightsModule() {
	}
}

record DashboardKpis(long totalCustomers, long activeCustomers, long activeLoans,
		BigDecimal totalLoanAmount, BigDecimal totalAmountCollected,
		BigDecimal totalPrincipalCollected, BigDecimal totalInterestCollected,
		BigDecimal totalOutstandingAmount, BigDecimal todayCollection,
		BigDecimal todayPendingCollection, BigDecimal dailyCollection,
		BigDecimal weeklyCollection, BigDecimal monthlyCollection, long activeChits,
		BigDecimal totalChitCollection, BigDecimal pendingChitPayments) {
}

record ReportResult(List<String> columns, List<Map<String, Object>> rows, long totalRows) {
}

record FinancialProfileLoan(Long id, String loanNumber, Long customerId, String customerName,
		Long loanProductId, String productName, CollectionFrequency collectionFrequency,
		InterestMethod interestMethod, BigDecimal principalAmount, BigDecimal interestRate,
		BigDecimal interestAmount, BigDecimal disbursedAmount, BigDecimal collectionAmount,
		Integer termCount, Integer remainingTermCount, LocalDate startDate, LocalDate maturityDate,
		BigDecimal outstandingPrincipal, BigDecimal outstandingInterest,
		BigDecimal totalCollected, LoanStatus status) {
}

record CustomerFinancialProfile(CustomerResponse customer, List<FinancialProfileLoan> activeLoans,
		List<FinancialProfileLoan> completedLoans, List<ChitSchemeResponse> activeChits,
		List<ChitSchemeResponse> completedChits, List<ChitRoundResponse> chitHistory,
		BigDecimal totalBorrowedAmount,
		BigDecimal totalPrincipalPaid, BigDecimal totalInterestPaid,
		BigDecimal outstandingPrincipal, BigDecimal monthlyContributions,
		BigDecimal pendingContributions) {
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class FinancialQueryService {
	private static final List<LoanStatus> ACTIVE_LOAN_STATUSES =
			List.of(LoanStatus.APPROVED, LoanStatus.ACTIVE, LoanStatus.PARTIALLY_PAID, LoanStatus.OVERDUE);

	private final CustomerRepository customers;
	private final LoanRepository loans;
	private final LoanProductRepository products;
	private final LoanScheduleRepository schedules;
	private final CollectionRepository collections;
	private final ChitSchemeRepository chits;
	private final ChitMemberRepository chitMembers;
	private final ChitRoundRepository chitRounds;
	private final ChitContributionRepository chitContributions;

	DashboardKpis dashboard(LocalDate today) {
		List<Customer> customerRows = customers.findAll().stream().filter(c -> !c.isDeleted()).toList();
		List<Loan> loanRows = loans.findAll();
		List<CollectionTransaction> posted = collections.findAll().stream()
				.filter(c -> c.getStatus() == CollectionStatus.COMPLETED).toList();
		List<LoanSchedule> scheduleRows = schedules.findAll();
		List<ChitContribution> contributionRows = chitContributions.findAll();
		LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate monthStart = today.withDayOfMonth(1);
		return new DashboardKpis(
				customerRows.size(),
				customerRows.stream().filter(c -> c.getStatus() == CustomerStatus.ACTIVE).count(),
				loanRows.stream().filter(l -> ACTIVE_LOAN_STATUSES.contains(l.getStatus())).count(),
				sumLoans(loanRows, Loan::getPrincipalAmount),
				sumCollections(posted, CollectionTransaction::getPaidAmount),
				sumCollections(posted, CollectionTransaction::getPrincipalAmount),
				sumCollections(posted, CollectionTransaction::getInterestAmount),
				sumLoans(loanRows, l -> l.getOutstandingPrincipal().add(l.getOutstandingInterest())),
				sumCollections(posted.stream().filter(c -> today.equals(c.getCollectionDate())).toList(),
						CollectionTransaction::getPaidAmount),
				sumSchedules(scheduleRows.stream().filter(s -> today.equals(s.getDueDate())
						&& s.getStatus() != ScheduleStatus.PAID).toList(),
						s -> s.getTotalDue().subtract(s.getPaidAmount()).max(BigDecimal.ZERO)),
				sumCollections(posted.stream().filter(c -> today.equals(c.getCollectionDate())).toList(),
						CollectionTransaction::getPaidAmount),
				sumCollections(posted.stream().filter(c -> !c.getCollectionDate().isBefore(weekStart)
						&& !c.getCollectionDate().isAfter(today)).toList(), CollectionTransaction::getPaidAmount),
				sumCollections(posted.stream().filter(c -> !c.getCollectionDate().isBefore(monthStart)
						&& !c.getCollectionDate().isAfter(today)).toList(), CollectionTransaction::getPaidAmount),
				chits.findAll().stream().filter(c -> c.getStatus() == ChitStatus.ACTIVE).count(),
				sumContributions(contributionRows, ChitContribution::getPaidAmount),
				sumContributions(contributionRows,
						c -> c.getDueAmount().subtract(c.getPaidAmount()).max(BigDecimal.ZERO)));
	}

	ReportResult report(String reportType, ReportFilter filter) {
		validateDates(filter.from(), filter.to());
		return switch (reportType) {
			case "customer-loans", "active-loans", "completed-loans", "outstanding-loans", "overdue-loans",
					"outstanding-balance" ->
					loanReport(reportType, filter);
			case "daily-collections", "weekly-collections", "monthly-collections",
					"principal-collection", "interest-collection", "date-range-collections",
					"collector-collections" -> collectionReport(reportType, filter);
			case "customer-statement" -> customerStatement(filter);
			case "active-chits" -> chitSchemeReport(filter, true);
			case "member-chits" -> memberChitReport(filter);
			case "pending-chit-payments" -> pendingChitReport(filter);
			case "chit-history" -> chitHistoryReport(filter);
			case "monthly-chit-collection" -> monthlyChitCollectionReport(filter);
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown report type: " + reportType);
		};
	}

	CustomerFinancialProfile profile(Long customerId) {
		Customer customer = customers.findByIdAndDeletedFalse(customerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
		Map<Long, LoanProduct> productMap = new java.util.HashMap<>();
		products.findAll().forEach(p -> productMap.put(p.getId(), p));
		String customerName = fullName(customer);
		List<Loan> customerLoans = loans.findAll().stream()
				.filter(l -> customerId.equals(l.getCustomerId())).toList();
		List<FinancialProfileLoan> active = customerLoans.stream()
				.filter(l -> ACTIVE_LOAN_STATUSES.contains(l.getStatus()))
				.map(l -> profileLoan(l, customerName, productMap.get(l.getLoanProductId()))).toList();
		List<FinancialProfileLoan> completed = customerLoans.stream()
				.filter(l -> l.getStatus() == LoanStatus.COMPLETED || l.getStatus() == LoanStatus.CLOSED)
				.map(l -> profileLoan(l, customerName, productMap.get(l.getLoanProductId()))).toList();
		List<Long> membershipIds = chitMembers.findAll().stream()
				.filter(m -> customerId.equals(m.getCustomerId())).map(ChitMember::getChitId).distinct().toList();
		List<ChitScheme> customerChits = chits.findAll().stream()
				.filter(c -> membershipIds.contains(c.getId())).toList();
		List<ChitRoundResponse> history = customerChits.stream()
				.flatMap(scheme -> chitRounds.findByChitIdOrderByRoundNumber(scheme.getId()).stream()
						.map(round -> roundHistory(round, scheme)))
				.toList();
		List<CollectionTransaction> posted = collections.findAll().stream()
				.filter(c -> customerId.equals(c.getCustomerId()) && c.getStatus() == CollectionStatus.COMPLETED)
				.toList();
		List<ChitMember> memberships = chitMembers.findAll().stream()
				.filter(m -> customerId.equals(m.getCustomerId())).toList();
		return new CustomerFinancialProfile(CustomerResponse.from(customer), active, completed,
				customerChits.stream().filter(c -> c.getStatus() == ChitStatus.ACTIVE)
						.map(ChitSchemeResponse::from).toList(),
				customerChits.stream().filter(c -> c.getStatus() == ChitStatus.COMPLETED)
						.map(ChitSchemeResponse::from).toList(),
				history,
				sumLoans(customerLoans, Loan::getPrincipalAmount),
				sumCollections(posted, CollectionTransaction::getPrincipalAmount),
				sumCollections(posted, CollectionTransaction::getInterestAmount),
				sumLoans(customerLoans, Loan::getOutstandingPrincipal),
				sumMemberships(memberships, m -> chits.findById(m.getChitId())
						.map(ChitScheme::getMonthlyContribution).orElse(BigDecimal.ZERO)),
				sumMemberships(memberships, ChitMember::getOutstandingAmount));
	}

	private ReportResult loanReport(String type, ReportFilter f) {
		Map<Long, Customer> customerMap = customerMap();
		Map<Long, LoanProduct> productMap = productMap();
		Predicate<Loan> typeFilter = switch (type) {
			case "active-loans" -> l -> ACTIVE_LOAN_STATUSES.contains(l.getStatus());
			case "completed-loans" -> l -> l.getStatus() == LoanStatus.COMPLETED || l.getStatus() == LoanStatus.CLOSED;
			case "outstanding-loans", "outstanding-balance" ->
					l -> l.getOutstandingPrincipal().add(l.getOutstandingInterest()).signum() > 0;
			case "overdue-loans" -> l -> l.getStatus() == LoanStatus.OVERDUE;
			default -> l -> true;
		};
		List<Map<String, Object>> rows = loans.findAll().stream()
				.filter(typeFilter)
				.filter(l -> f.customerId() == null || f.customerId().equals(l.getCustomerId()))
				.filter(l -> f.loanType() == null || f.loanType().isBlank()
						|| l.getCollectionFrequency().name().equalsIgnoreCase(f.loanType()))
				.filter(l -> f.status() == null || f.status().isBlank()
						|| l.getStatus().name().equalsIgnoreCase(f.status()))
				.filter(l -> inRange(l.getStartDate(), f.from(), f.to()))
				.map(l -> row("loanNumber", l.getLoanNumber(), "customerId", l.getCustomerId(),
						"customerName", fullName(customerMap.get(l.getCustomerId())),
						"productName", productMap.containsKey(l.getLoanProductId())
								? productMap.get(l.getLoanProductId()).getProductName() : null,
						"loanType", l.getCollectionFrequency(), "principalAmount", l.getPrincipalAmount(),
						"outstandingPrincipal", l.getOutstandingPrincipal(),
						"outstandingInterest", l.getOutstandingInterest(), "startDate", l.getStartDate(),
						"maturityDate", l.getMaturityDate(), "status", l.getStatus())).toList();
		return result(rows);
	}

	private ReportResult collectionReport(String type, ReportFilter f) {
		if ("collector-collections".equals(type) && f.collectorId() == null) {
			// Still valid: report all collectors, grouped visually by collectorId column.
		}
		CollectionFrequency requiredFrequency = switch (type) {
			case "daily-collections" -> CollectionFrequency.DAILY;
			case "weekly-collections" -> CollectionFrequency.WEEKLY;
			case "monthly-collections" -> CollectionFrequency.MONTHLY;
			default -> null;
		};
		Map<Long, Loan> loanMap = new java.util.HashMap<>();
		loans.findAll().forEach(l -> loanMap.put(l.getId(), l));
		Map<Long, Customer> customerMap = customerMap();
		List<Map<String, Object>> rows = collections.findAll().stream()
				.filter(c -> c.getStatus() == CollectionStatus.COMPLETED)
				.filter(c -> f.customerId() == null || f.customerId().equals(c.getCustomerId()))
				.filter(c -> f.collectorId() == null || f.collectorId().equals(c.getCollectorId()))
				.filter(c -> inRange(c.getCollectionDate(), f.from(), f.to()))
				.filter(c -> requiredFrequency == null || loanMap.get(c.getLoanId()) != null
						&& loanMap.get(c.getLoanId()).getCollectionFrequency() == requiredFrequency)
				.filter(c -> f.loanType() == null || f.loanType().isBlank()
						|| loanMap.get(c.getLoanId()) != null
						&& loanMap.get(c.getLoanId()).getCollectionFrequency().name().equalsIgnoreCase(f.loanType()))
				.map(c -> row("transactionReference", c.getTransactionReference(),
						"collectionDate", c.getCollectionDate(), "customerId", c.getCustomerId(),
						"customerName", fullName(customerMap.get(c.getCustomerId())),
						"loanId", c.getLoanId(), "paidAmount", c.getPaidAmount(),
						"principalAmount", c.getPrincipalAmount(), "interestAmount", c.getInterestAmount(),
						"reportedAmount", "principal-collection".equals(type) ? c.getPrincipalAmount()
								: "interest-collection".equals(type) ? c.getInterestAmount() : c.getPaidAmount(),
						"paymentMode", c.getPaymentMode(), "collectorId", c.getCollectorId())).toList();
		return result(rows);
	}

	private ReportResult chitSchemeReport(ReportFilter f, boolean activeOnly) {
		List<Map<String, Object>> rows = chits.findAll().stream()
				.filter(c -> !activeOnly || c.getStatus() == ChitStatus.ACTIVE)
				.filter(c -> f.chitId() == null || f.chitId().equals(c.getId()))
				.filter(c -> f.status() == null || f.status().isBlank()
						|| c.getStatus().name().equalsIgnoreCase(f.status()))
				.filter(c -> inRange(c.getStartDate(), f.from(), f.to()))
				.map(c -> row("chitId", c.getId(), "chitCode", c.getChitCode(), "name", c.getName(),
						"chitAmount", c.getChitAmount(), "memberCount", c.getMemberCount(),
						"monthlyContribution", c.getMonthlyContribution(), "currentRound", c.getCurrentRound(),
						"startDate", c.getStartDate(), "status", c.getStatus())).toList();
		return result(rows);
	}

	private ReportResult memberChitReport(ReportFilter f) {
		Map<Long, ChitScheme> schemeMap = new java.util.HashMap<>();
		chits.findAll().forEach(c -> schemeMap.put(c.getId(), c));
		Map<Long, Customer> customerMap = customerMap();
		List<Map<String, Object>> rows = chitMembers.findAll().stream()
				.filter(m -> f.customerId() == null || f.customerId().equals(m.getCustomerId()))
				.filter(m -> f.chitId() == null || f.chitId().equals(m.getChitId()))
				.filter(m -> f.status() == null || f.status().isBlank()
						|| m.getStatus().name().equalsIgnoreCase(f.status()))
				.filter(m -> inRange(m.getJoinedDate(), f.from(), f.to()))
				.map(m -> row("chitId", m.getChitId(), "chitCode",
						schemeMap.containsKey(m.getChitId()) ? schemeMap.get(m.getChitId()).getChitCode() : null,
						"customerId", m.getCustomerId(), "customerName", fullName(customerMap.get(m.getCustomerId())),
						"joinedDate", m.getJoinedDate(), "amountPaid", m.getTotalContributed(),
						"amountPending", m.getOutstandingAmount(), "status", m.getStatus())).toList();
		return result(rows);
	}

	private ReportResult pendingChitReport(ReportFilter f) {
		Map<Long, ChitRound> roundMap = new java.util.HashMap<>();
		chitRounds.findAll().forEach(r -> roundMap.put(r.getId(), r));
		Map<Long, ChitMember> memberMap = new java.util.HashMap<>();
		chitMembers.findAll().forEach(m -> memberMap.put(m.getId(), m));
		Map<Long, Customer> customerMap = customerMap();
		List<Map<String, Object>> rows = chitContributions.findAll().stream()
				.filter(c -> c.getStatus() != ChitPaymentStatus.PAID)
				.filter(c -> {
					ChitRound r = roundMap.get(c.getRoundId());
					ChitMember m = memberMap.get(c.getMemberId());
					return r != null && m != null
							&& (f.chitId() == null || f.chitId().equals(r.getChitId()))
							&& (f.customerId() == null || f.customerId().equals(m.getCustomerId()))
							&& inRange(r.getCollectionDate(), f.from(), f.to())
							&& (f.status() == null || f.status().isBlank()
									|| c.getStatus().name().equalsIgnoreCase(f.status()));
				})
				.map(c -> {
					ChitRound r = roundMap.get(c.getRoundId());
					ChitMember m = memberMap.get(c.getMemberId());
					return row("chitId", r.getChitId(), "roundNumber", r.getRoundNumber(),
							"customerId", m.getCustomerId(), "customerName",
							fullName(customerMap.get(m.getCustomerId())), "dueAmount", c.getDueAmount(),
							"paidAmount", c.getPaidAmount(), "pendingAmount",
							c.getDueAmount().subtract(c.getPaidAmount()), "collectionDate", r.getCollectionDate(),
							"status", c.getStatus());
				}).toList();
		return result(rows);
	}

	private ReportResult chitHistoryReport(ReportFilter f) {
		Map<Long, ChitScheme> schemeMap = new java.util.HashMap<>();
		chits.findAll().forEach(c -> schemeMap.put(c.getId(), c));
		List<Map<String, Object>> rows = chitRounds.findAll().stream()
				.filter(r -> f.chitId() == null || f.chitId().equals(r.getChitId()))
				.filter(r -> inRange(r.getCollectionDate(), f.from(), f.to()))
				.filter(r -> f.status() == null || f.status().isBlank()
						|| r.getStatus().name().equalsIgnoreCase(f.status()))
				.map(r -> row("chitId", r.getChitId(), "chitCode",
						schemeMap.containsKey(r.getChitId()) ? schemeMap.get(r.getChitId()).getChitCode() : null,
						"roundNumber", r.getRoundNumber(), "collectionDate", r.getCollectionDate(),
						"totalCollected", r.getTotalCollection(), "winnerMemberId", r.getWinnerMemberId(),
						"bidAmount", r.getBidAmount(), "deductionAmount", r.getDeductionAmount(),
						"payoutAmount", r.getPayoutAmount(), "payoutDate", r.getPayoutDate(),
						"status", r.getStatus())).toList();
		return result(rows);
	}

	private ReportResult monthlyChitCollectionReport(ReportFilter f) {
		Map<Long, ChitScheme> schemeMap = new java.util.HashMap<>();
		chits.findAll().forEach(c -> schemeMap.put(c.getId(), c));
		List<Map<String, Object>> rows = chitRounds.findAll().stream()
				.filter(r -> f.chitId() == null || f.chitId().equals(r.getChitId()))
				.filter(r -> inRange(r.getCollectionDate(), f.from(), f.to()))
				.map(r -> {
					ChitScheme scheme = schemeMap.get(r.getChitId());
					BigDecimal due = scheme == null ? BigDecimal.ZERO
							: scheme.getMonthlyContribution().multiply(BigDecimal.valueOf(scheme.getMemberCount()));
					return row("yearMonth", r.getCollectionDate().withDayOfMonth(1),
							"chitId", r.getChitId(),
							"chitCode", scheme == null ? null : scheme.getChitCode(),
							"roundNumber", r.getRoundNumber(),
							"collectionDate", r.getCollectionDate(),
							"totalDue", due,
							"totalCollected", r.getTotalCollection(),
							"pendingAmount", due.subtract(r.getTotalCollection()).max(BigDecimal.ZERO),
							"status", r.getStatus());
				}).toList();
		return result(rows);
	}

	private ReportResult customerStatement(ReportFilter f) {
		if (f.customerId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required for a customer statement");
		}
		CustomerFinancialProfile profile = profile(f.customerId());
		List<Map<String, Object>> rows = new ArrayList<>();
		rows.add(row("section", "SUMMARY", "customerCode", profile.customer().customerCode(),
				"customerName", profile.customer().firstName() + " " + (profile.customer().lastName() == null ? "" : profile.customer().lastName()),
				"totalBorrowed", profile.totalBorrowedAmount(), "principalPaid", profile.totalPrincipalPaid(),
				"interestPaid", profile.totalInterestPaid(), "outstandingPrincipal", profile.outstandingPrincipal(),
				"pendingChit", profile.pendingContributions()));
		profile.activeLoans().forEach(l -> rows.add(row("section", "ACTIVE_LOAN", "loanNumber", l.loanNumber(),
				"status", l.status(), "principalAmount", l.principalAmount(),
				"outstandingPrincipal", l.outstandingPrincipal(), "outstandingInterest", l.outstandingInterest())));
		profile.completedLoans().forEach(l -> rows.add(row("section", "COMPLETED_LOAN", "loanNumber", l.loanNumber(),
				"status", l.status(), "principalAmount", l.principalAmount(),
				"outstandingPrincipal", l.outstandingPrincipal(), "outstandingInterest", l.outstandingInterest())));
		collections.findAll().stream()
				.filter(c -> f.customerId().equals(c.getCustomerId()) && c.getStatus() == CollectionStatus.COMPLETED)
				.filter(c -> inRange(c.getCollectionDate(), f.from(), f.to()))
				.forEach(c -> rows.add(row("section", "COLLECTION", "transactionReference", c.getTransactionReference(),
						"collectionDate", c.getCollectionDate(), "paidAmount", c.getPaidAmount(),
						"principalAmount", c.getPrincipalAmount(), "interestAmount", c.getInterestAmount())));
		return result(rows);
	}

	private ChitRoundResponse roundHistory(ChitRound r, ChitScheme s) {
		BigDecimal due = Money.round(s.getMonthlyContribution().multiply(BigDecimal.valueOf(s.getMemberCount())));
		return new ChitRoundResponse(r.getId(), r.getChitId(), r.getRoundNumber(), r.getCollectionDate(), due,
				r.getTotalCollection(), Money.round(due.subtract(r.getTotalCollection()).max(BigDecimal.ZERO)),
				r.getWinnerMemberId(), null, r.getBidAmount(), r.getDeductionAmount(),
				r.getPayoutAmount(), r.getPayoutDate(), r.getStatus());
	}

	private FinancialProfileLoan profileLoan(Loan l, String customerName, LoanProduct p) {
		return new FinancialProfileLoan(l.getId(), l.getLoanNumber(), l.getCustomerId(), customerName,
				l.getLoanProductId(), p == null ? null : p.getProductName(), l.getCollectionFrequency(),
				l.getInterestMethod(), l.getPrincipalAmount(), l.getInterestRate(), l.getInterestAmount(),
				l.getDisbursedAmount(), l.getCollectionAmount(), l.getTermCount(), l.getRemainingTermCount(),
				l.getStartDate(), l.getMaturityDate(), l.getOutstandingPrincipal(), l.getOutstandingInterest(),
				l.getTotalCollected(), l.getStatus());
	}

	private ReportResult result(List<Map<String, Object>> rows) {
		List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());
		return new ReportResult(columns, rows, rows.size());
	}
	private Map<String, Object> row(Object... values) {
		Map<String, Object> row = new LinkedHashMap<>();
		for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
		return row;
	}
	private Map<Long, Customer> customerMap() {
		Map<Long, Customer> map = new java.util.HashMap<>();
		customers.findAll().forEach(c -> map.put(c.getId(), c));
		return map;
	}
	private Map<Long, LoanProduct> productMap() {
		Map<Long, LoanProduct> map = new java.util.HashMap<>();
		products.findAll().forEach(p -> map.put(p.getId(), p));
		return map;
	}
	private String fullName(Customer c) {
		return c == null ? null : (c.getFirstName() + " " + (c.getLastName() == null ? "" : c.getLastName())).trim();
	}
	private boolean inRange(LocalDate date, LocalDate from, LocalDate to) {
		return date != null && (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
	}
	private void validateDates(LocalDate from, LocalDate to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
		}
	}
	private BigDecimal sumLoans(List<Loan> values, java.util.function.Function<Loan, BigDecimal> field) {
		return values.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
	}
	private BigDecimal sumCollections(List<CollectionTransaction> values,
			java.util.function.Function<CollectionTransaction, BigDecimal> field) {
		return values.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
	}
	private BigDecimal sumSchedules(List<LoanSchedule> values,
			java.util.function.Function<LoanSchedule, BigDecimal> field) {
		return values.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
	}
	private BigDecimal sumContributions(List<ChitContribution> values,
			java.util.function.Function<ChitContribution, BigDecimal> field) {
		return values.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
	}
	private BigDecimal sumMemberships(List<ChitMember> values,
			java.util.function.Function<ChitMember, BigDecimal> field) {
		return values.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
	}
}

record ReportFilter(LocalDate from, LocalDate to, Long customerId, String loanType,
		Long chitId, Long collectorId, String status) {
}

@RestController
@RequiredArgsConstructor
class FinancialInsightsController {
	private final FinancialQueryService service;

	@GetMapping("/api/v2/dashboard")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> dashboard() {
		return ResponseEntity.ok(ResponseBuilder.success(service.dashboard(LocalDate.now())));
	}

	@GetMapping("/api/v2/customers/{id}/financial-profile")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> profile(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.profile(id)));
	}

	@GetMapping("/api/v2/reports/{reportType}")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<?> report(@PathVariable String reportType,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to,
			@RequestParam(required = false) Long customerId,
			@RequestParam(required = false) String loanType,
			@RequestParam(required = false) Long chitId,
			@RequestParam(required = false) Long collectorId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String format) {
		ReportResult result = service.report(reportType,
				new ReportFilter(from, to, customerId, loanType, chitId, collectorId, status));
		if ("csv".equalsIgnoreCase(format)) {
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + reportType + "-" + LocalDate.now() + ".csv\"")
					.contentType(MediaType.parseMediaType("text/csv"))
					.body(toCsv(result));
		}
		return ResponseEntity.ok(ResponseBuilder.success(result));
	}

	private String toCsv(ReportResult result) {
		StringBuilder csv = new StringBuilder();
		csv.append(result.columns().stream().map(this::csvValue)
				.collect(java.util.stream.Collectors.joining(","))).append("\r\n");
		for (Map<String, Object> row : result.rows()) {
			csv.append(result.columns().stream().map(c -> csvValue(row.get(c)))
					.collect(java.util.stream.Collectors.joining(","))).append("\r\n");
		}
		return csv.toString();
	}

	private String csvValue(Object value) {
		String text = value == null ? "" : String.valueOf(value);
		return "\"" + text.replace("\"", "\"\"") + "\"";
	}
}
