package config.financial.lending;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public final class ChitModule {
	private ChitModule() {
	}
}

enum ChitStatus { DRAFT, ACTIVE, COMPLETED, CANCELLED }
enum ChitMemberStatus { ACTIVE, INACTIVE }
enum ChitRoundStatus { OPEN, WINNER_SELECTED, PAID_OUT }
enum ChitPaymentStatus { PENDING, PARTIALLY_PAID, PAID, OVERDUE }

@Entity
@Table(name = "chit_schemes", schema = "finance")
@Getter
@Setter
class ChitScheme extends BaseAuditableEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "chit_code", nullable = false, unique = true, length = 40)
	private String chitCode;
	@Column(name = "chit_name", nullable = false, length = 150)
	private String name;
	@Column(name = "chit_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal chitAmount;
	@Column(name = "member_count", nullable = false)
	private Integer memberCount;
	@Column(name = "monthly_contribution", nullable = false, precision = 19, scale = 2)
	private BigDecimal monthlyContribution;
	@Column(name = "duration_months", nullable = false)
	private Integer durationMonths;
	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;
	@Column(name = "current_round", nullable = false)
	private Integer currentRound;
	@Enumerated(EnumType.STRING) @Column(nullable = false)
	private ChitStatus status;
}

@Entity
@Table(name = "chit_members", schema = "finance")
@Getter
@Setter
class ChitMember extends BaseAuditableEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "chit_scheme_id", nullable = false)
	private Long chitId;
	@Column(name = "customer_id", nullable = false)
	private Long customerId;
	@Column(name = "member_number", nullable = false)
	private Integer memberNumber;
	@Column(name = "joined_date", nullable = false)
	private LocalDate joinedDate;
	@Enumerated(EnumType.STRING) @Column(nullable = false)
	private ChitMemberStatus status;
	@Column(name = "total_contributed", nullable = false, precision = 19, scale = 2)
	private BigDecimal totalContributed;
	@Column(name = "outstanding_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal outstandingAmount;
}

@Entity
@Table(name = "chit_rounds", schema = "finance")
@Getter
@Setter
class ChitRound extends BaseAuditableEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "chit_scheme_id", nullable = false)
	private Long chitId;
	@Column(name = "round_number", nullable = false)
	private Integer roundNumber;
	@Column(name = "collection_date", nullable = false)
	private LocalDate collectionDate;
	@Column(name = "total_collection", nullable = false, precision = 19, scale = 2)
	private BigDecimal totalCollection;
	@Column(name = "winner_member_id")
	private Long winnerMemberId;
	@Column(name = "bid_amount", precision = 19, scale = 2)
	private BigDecimal bidAmount;
	@Column(name = "deduction_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal deductionAmount;
	@Column(name = "prize_amount", precision = 19, scale = 2)
	private BigDecimal payoutAmount;
	@Column(name = "payout_reference", length = 80)
	private String payoutReference;
	@Column(name = "payout_date")
	private LocalDate payoutDate;
	@Enumerated(EnumType.STRING) @Column(nullable = false)
	private ChitRoundStatus status;
}

@Entity
@Table(name = "chit_contributions", schema = "finance")
@Getter
@Setter
class ChitContribution extends BaseAuditableEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "chit_round_id", nullable = false)
	private Long roundId;
	@Column(name = "chit_member_id", nullable = false)
	private Long memberId;
	@Column(name = "due_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal dueAmount;
	@Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal paidAmount;
	@Column(name = "paid_date")
	private LocalDate paidDate;
	@Enumerated(EnumType.STRING) @Column(name = "payment_mode")
	private PaymentMode paymentMode;
	@Column(name = "transaction_reference", length = 80)
	private String transactionReference;
	@Column(name = "collector_id")
	private Long collectorId;
	@Column(length = 500)
	private String remarks;
	@Enumerated(EnumType.STRING) @Column(nullable = false)
	private ChitPaymentStatus status;
}

interface ChitSchemeRepository extends JpaRepository<ChitScheme, Long> {
	boolean existsByChitCodeIgnoreCase(String code);
	List<ChitScheme> findAllByOrderByStartDateDesc();
	List<ChitScheme> findByStatusOrderByStartDateDesc(ChitStatus status);
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from ChitScheme c where c.id=:id")
	java.util.Optional<ChitScheme> findForUpdate(@Param("id") Long id);
}

interface ChitMemberRepository extends JpaRepository<ChitMember, Long> {
	List<ChitMember> findByChitIdOrderByMemberNumber(Long chitId);
	List<ChitMember> findByChitIdAndStatusOrderByMemberNumber(Long chitId, ChitMemberStatus status);
	boolean existsByChitIdAndCustomerId(Long chitId, Long customerId);
	long countByChitId(Long chitId);
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select m from ChitMember m where m.id=:id")
	java.util.Optional<ChitMember> findForUpdate(@Param("id") Long id);
}

interface ChitRoundRepository extends JpaRepository<ChitRound, Long> {
	List<ChitRound> findByChitIdOrderByRoundNumber(Long chitId);
	boolean existsByChitIdAndWinnerMemberIdIsNotNullAndWinnerMemberId(Long chitId, Long memberId);
	boolean existsByPayoutReferenceIgnoreCase(String reference);
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from ChitRound r where r.id=:id")
	java.util.Optional<ChitRound> findForUpdate(@Param("id") Long id);
}

interface ChitContributionRepository extends JpaRepository<ChitContribution, Long> {
	List<ChitContribution> findByRoundIdOrderByMemberId(Long roundId);
	java.util.Optional<ChitContribution> findByRoundIdAndMemberId(Long roundId, Long memberId);
	boolean existsByTransactionReferenceIgnoreCase(String reference);
}

record ChitSchemeRequest(
		@NotBlank @Size(max = 150) String name,
		@NotNull @DecimalMin("0.01") BigDecimal chitAmount,
		@NotNull @Min(2) Integer memberCount,
		@NotNull @DecimalMin("0.01") BigDecimal monthlyContribution,
		@NotNull @Min(1) Integer durationMonths,
		@NotNull LocalDate startDate,
		@NotNull ChitStatus status) {
}

record ChitMemberRequest(@NotNull Long customerId, @NotNull LocalDate joinedDate) {
}

record ChitRoundRequest(@NotNull LocalDate collectionDate) {
}

record ContributionRequest(@NotNull Long memberId,
		@NotNull @DecimalMin("0.01") BigDecimal paidAmount,
		@NotNull PaymentMode paymentMode,
		@Size(max = 80) String transactionReference,
		Long collectorId, @Size(max = 500) String remarks) {
}

record WinnerRequest(@NotNull Long winnerMemberId,
		@NotNull @DecimalMin("0.00") BigDecimal bidAmount,
		@NotNull @DecimalMin("0.00") BigDecimal deductionAmount) {
}

record PayoutRequest(@NotNull @DecimalMin("0.01") BigDecimal payoutAmount,
		@NotNull LocalDate payoutDate,
		@NotBlank @Size(max = 80) String transactionReference) {
}

record ChitSchemeResponse(Long id, String chitCode, String name, BigDecimal chitAmount,
		Integer memberCount, BigDecimal monthlyContribution, Integer durationMonths,
		LocalDate startDate, Integer currentRound, ChitStatus status) {
	static ChitSchemeResponse from(ChitScheme c) {
		return new ChitSchemeResponse(c.getId(), c.getChitCode(), c.getName(), c.getChitAmount(),
				c.getMemberCount(), c.getMonthlyContribution(), c.getDurationMonths(), c.getStartDate(),
				c.getCurrentRound(), c.getStatus());
	}
}

record ChitMemberResponse(Long id, Long chitId, Long customerId, String customerCode,
		String customerName, LocalDate joinedDate, BigDecimal contributionAmount,
		BigDecimal amountPaid, BigDecimal amountPending, ChitMemberStatus status) {
}

record ChitRoundResponse(Long id, Long chitId, Integer roundNumber, LocalDate collectionDate,
		BigDecimal totalDue, BigDecimal totalCollected, BigDecimal totalPending,
		Long winnerMemberId, String winnerName, BigDecimal bidAmount,
		BigDecimal deductionAmount, BigDecimal payoutAmount, LocalDate payoutDate,
		ChitRoundStatus status) {
}

record ChitContributionResponse(Long id, Long roundId, Long memberId, String memberName,
		BigDecimal dueAmount, BigDecimal paidAmount, LocalDate paidDate, PaymentMode paymentMode,
		String transactionReference, ChitPaymentStatus status) {
}

@Service
@RequiredArgsConstructor
class ChitService {
	private final ChitSchemeRepository schemes;
	private final ChitMemberRepository members;
	private final ChitRoundRepository rounds;
	private final ChitContributionRepository contributions;
	private final CustomerRepository customers;
	private final AuditService audit;

	@Transactional
	ChitSchemeResponse create(ChitSchemeRequest request) {
		ChitScheme scheme = new ChitScheme();
		scheme.setChitCode(generateCode());
		scheme.setName(request.name().trim());
		scheme.setChitAmount(Money.round(request.chitAmount()));
		scheme.setMemberCount(request.memberCount());
		scheme.setMonthlyContribution(Money.round(request.monthlyContribution()));
		scheme.setDurationMonths(request.durationMonths());
		scheme.setStartDate(request.startDate());
		scheme.setCurrentRound(0);
		scheme.setStatus(request.status());
		scheme = schemes.save(scheme);
		ChitSchemeResponse result = ChitSchemeResponse.from(scheme);
		audit.record("ChitScheme", scheme.getId(), AuditAction.CREATE, null, result);
		return result;
	}

	@Transactional(readOnly = true)
	List<ChitSchemeResponse> list(ChitStatus status) {
		List<ChitScheme> result = status == null ? schemes.findAllByOrderByStartDateDesc()
				: schemes.findByStatusOrderByStartDateDesc(status);
		return result.stream().map(ChitSchemeResponse::from).toList();
	}

	@Transactional
	ChitMemberResponse addMember(Long chitId, ChitMemberRequest request) {
		ChitScheme scheme = lockedScheme(chitId);
		if (scheme.getStatus() == ChitStatus.COMPLETED || scheme.getStatus() == ChitStatus.CANCELLED) {
			throw conflict("Members cannot be added to a closed chit");
		}
		if (members.countByChitId(chitId) >= scheme.getMemberCount()) {
			throw conflict("Chit member capacity has been reached");
		}
		if (members.existsByChitIdAndCustomerId(chitId, request.customerId())) {
			throw conflict("Customer is already a member of this chit");
		}
		Customer customer = customers.findByIdAndDeletedFalse(request.customerId())
				.orElseThrow(() -> notFound("Customer not found"));
		ChitMember member = new ChitMember();
		member.setChitId(chitId);
		member.setCustomerId(customer.getId());
		member.setMemberNumber((int) members.countByChitId(chitId) + 1);
		member.setJoinedDate(request.joinedDate());
		member.setStatus(ChitMemberStatus.ACTIVE);
		member.setTotalContributed(zero());
		member.setOutstandingAmount(zero());
		member = members.save(member);
		ChitMemberResponse result = memberResponse(member, scheme, customer);
		audit.record("ChitMember", member.getId(), AuditAction.CREATE, null, result);
		return result;
	}

	@Transactional(readOnly = true)
	List<ChitMemberResponse> listMembers(Long chitId) {
		ChitScheme scheme = requiredScheme(chitId);
		return members.findByChitIdOrderByMemberNumber(chitId).stream()
				.map(m -> memberResponse(m, scheme, requiredCustomer(m.getCustomerId()))).toList();
	}

	@Transactional
	ChitRoundResponse createRound(Long chitId, ChitRoundRequest request) {
		ChitScheme scheme = lockedScheme(chitId);
		if (scheme.getStatus() != ChitStatus.ACTIVE) {
			throw conflict("Only active chits can open rounds");
		}
		List<ChitRound> existing = rounds.findByChitIdOrderByRoundNumber(chitId);
		if (!existing.isEmpty() && existing.get(existing.size() - 1).getStatus() != ChitRoundStatus.PAID_OUT) {
			throw conflict("The previous round must be paid out first");
		}
		int next = scheme.getCurrentRound() + 1;
		if (next > scheme.getDurationMonths()) {
			throw conflict("All chit rounds have already been created");
		}
		List<ChitMember> activeMembers = members.findByChitIdAndStatusOrderByMemberNumber(chitId,
				ChitMemberStatus.ACTIVE);
		if (activeMembers.size() != scheme.getMemberCount()) {
			throw conflict("All member slots must be filled before opening a round");
		}
		ChitRound round = new ChitRound();
		round.setChitId(chitId);
		round.setRoundNumber(next);
		round.setCollectionDate(request.collectionDate());
		round.setTotalCollection(zero());
		round.setDeductionAmount(zero());
		round.setStatus(ChitRoundStatus.OPEN);
		round = rounds.save(round);
		for (ChitMember member : activeMembers) {
			ChitContribution contribution = new ChitContribution();
			contribution.setRoundId(round.getId());
			contribution.setMemberId(member.getId());
			contribution.setDueAmount(scheme.getMonthlyContribution());
			contribution.setPaidAmount(zero());
			contribution.setStatus(ChitPaymentStatus.PENDING);
			contributions.save(contribution);
			member.setOutstandingAmount(Money.round(member.getOutstandingAmount()
					.add(scheme.getMonthlyContribution())));
		}
		members.saveAll(activeMembers);
		scheme.setCurrentRound(next);
		schemes.save(scheme);
		ChitRoundResponse result = roundResponse(round, scheme);
		audit.record("ChitRound", round.getId(), AuditAction.CREATE, null, result);
		return result;
	}

	@Transactional(readOnly = true)
	List<ChitRoundResponse> listRounds(Long chitId) {
		ChitScheme scheme = requiredScheme(chitId);
		return rounds.findByChitIdOrderByRoundNumber(chitId).stream()
				.map(r -> roundResponse(r, scheme)).toList();
	}

	@Transactional
	ChitContributionResponse contribute(Long roundId, ContributionRequest request) {
		ChitRound round = lockedRound(roundId);
		if (round.getStatus() == ChitRoundStatus.PAID_OUT) {
			throw conflict("A paid-out round is immutable");
		}
		ChitMember member = members.findForUpdate(request.memberId())
				.orElseThrow(() -> notFound("Chit member not found"));
		if (!member.getChitId().equals(round.getChitId()) || member.getStatus() != ChitMemberStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member is not eligible for this round");
		}
		ChitContribution contribution = contributions.findByRoundIdAndMemberId(roundId, request.memberId())
				.orElseThrow(() -> notFound("Contribution obligation not found"));
		BigDecimal amount = Money.round(request.paidAmount());
		BigDecimal remaining = Money.round(contribution.getDueAmount().subtract(contribution.getPaidAmount()));
		if (amount.compareTo(remaining) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment exceeds contribution balance");
		}
		String reference = request.transactionReference();
		if (reference != null && !reference.isBlank()
				&& contributions.existsByTransactionReferenceIgnoreCase(reference.trim())) {
			throw conflict("Transaction reference already exists");
		}
		ChitContributionResponse before = contributionResponse(contribution, member);
		contribution.setPaidAmount(Money.round(contribution.getPaidAmount().add(amount)));
		contribution.setPaidDate(round.getCollectionDate());
		contribution.setPaymentMode(request.paymentMode());
		contribution.setTransactionReference(reference == null || reference.isBlank() ? generatedReference("CHC")
				: reference.trim());
		contribution.setCollectorId(request.collectorId());
		contribution.setRemarks(request.remarks());
		contribution.setStatus(contribution.getPaidAmount().compareTo(contribution.getDueAmount()) == 0
				? ChitPaymentStatus.PAID : ChitPaymentStatus.PARTIALLY_PAID);
		member.setTotalContributed(Money.round(member.getTotalContributed().add(amount)));
		member.setOutstandingAmount(Money.round(member.getOutstandingAmount().subtract(amount)));
		round.setTotalCollection(Money.round(round.getTotalCollection().add(amount)));
		contributions.save(contribution);
		members.save(member);
		rounds.save(round);
		ChitContributionResponse result = contributionResponse(contribution, member);
		audit.record("ChitContribution", contribution.getId(), AuditAction.PAYMENT, before, result);
		return result;
	}

	@Transactional(readOnly = true)
	List<ChitContributionResponse> listContributions(Long roundId) {
		if (!rounds.existsById(roundId)) throw notFound("Chit round not found");
		return contributions.findByRoundIdOrderByMemberId(roundId).stream()
				.map(c -> contributionResponse(c, requiredMember(c.getMemberId()))).toList();
	}

	@Transactional
	ChitRoundResponse winner(Long roundId, WinnerRequest request) {
		ChitRound round = lockedRound(roundId);
		if (round.getStatus() != ChitRoundStatus.OPEN || round.getWinnerMemberId() != null) {
			throw conflict("Winner has already been selected");
		}
		ChitMember winner = members.findForUpdate(request.winnerMemberId())
				.orElseThrow(() -> notFound("Chit member not found"));
		if (!winner.getChitId().equals(round.getChitId()) || winner.getStatus() != ChitMemberStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Winner is not an active member of this chit");
		}
		if (rounds.existsByChitIdAndWinnerMemberIdIsNotNullAndWinnerMemberId(round.getChitId(), winner.getId())) {
			throw conflict("This member has already won a chit round");
		}
		ChitContribution winnerContribution = contributions.findByRoundIdAndMemberId(roundId, winner.getId())
				.orElseThrow(() -> notFound("Winner contribution not found"));
		if (winnerContribution.getStatus() != ChitPaymentStatus.PAID) {
			throw conflict("Winner must fully pay the current round contribution");
		}
		ChitScheme scheme = requiredScheme(round.getChitId());
		BigDecimal bid = Money.round(request.bidAmount());
		BigDecimal deduction = Money.round(request.deductionAmount());
		BigDecimal prize = Money.round(scheme.getChitAmount().subtract(bid).subtract(deduction));
		if (prize.signum() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bid and deductions exceed chit amount");
		}
		ChitRoundResponse before = roundResponse(round, scheme);
		round.setWinnerMemberId(winner.getId());
		round.setBidAmount(bid);
		round.setDeductionAmount(deduction);
		round.setPayoutAmount(prize);
		round.setStatus(ChitRoundStatus.WINNER_SELECTED);
		rounds.save(round);
		ChitRoundResponse result = roundResponse(round, scheme);
		audit.record("ChitRound", roundId, AuditAction.APPROVE, before, result);
		return result;
	}

	@Transactional
	ChitRoundResponse payout(Long roundId, PayoutRequest request) {
		ChitRound round = lockedRound(roundId);
		if (round.getStatus() != ChitRoundStatus.WINNER_SELECTED || round.getPayoutReference() != null) {
			throw conflict("Payout is only allowed once after winner selection");
		}
		BigDecimal requested = Money.round(request.payoutAmount());
		if (requested.compareTo(round.getPayoutAmount()) != 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Payout amount must equal the calculated prize amount");
		}
		if (rounds.existsByPayoutReferenceIgnoreCase(request.transactionReference().trim())) {
			throw conflict("Payout reference already exists");
		}
		ChitScheme scheme = lockedScheme(round.getChitId());
		ChitRoundResponse before = roundResponse(round, scheme);
		round.setPayoutReference(request.transactionReference().trim());
		round.setPayoutDate(request.payoutDate());
		round.setStatus(ChitRoundStatus.PAID_OUT);
		if (round.getRoundNumber().equals(scheme.getDurationMonths())) {
			scheme.setStatus(ChitStatus.COMPLETED);
			schemes.save(scheme);
		}
		rounds.save(round);
		ChitRoundResponse result = roundResponse(round, scheme);
		audit.record("ChitPayout", roundId, AuditAction.PAYMENT, before, result);
		return result;
	}

	private ChitMemberResponse memberResponse(ChitMember m, ChitScheme s, Customer c) {
		String name = (c.getFirstName() + " " + (c.getLastName() == null ? "" : c.getLastName())).trim();
		return new ChitMemberResponse(m.getId(), m.getChitId(), m.getCustomerId(), c.getCustomerCode(), name,
				m.getJoinedDate(), s.getMonthlyContribution(), m.getTotalContributed(),
				m.getOutstandingAmount(), m.getStatus());
	}

	private ChitRoundResponse roundResponse(ChitRound r, ChitScheme s) {
		BigDecimal due = Money.round(s.getMonthlyContribution().multiply(BigDecimal.valueOf(s.getMemberCount())));
		String winnerName = null;
		if (r.getWinnerMemberId() != null) {
			ChitMember member = requiredMember(r.getWinnerMemberId());
			Customer customer = requiredCustomer(member.getCustomerId());
			winnerName = (customer.getFirstName() + " "
					+ (customer.getLastName() == null ? "" : customer.getLastName())).trim();
		}
		return new ChitRoundResponse(r.getId(), r.getChitId(), r.getRoundNumber(), r.getCollectionDate(), due,
				r.getTotalCollection(), Money.round(due.subtract(r.getTotalCollection()).max(BigDecimal.ZERO)),
				r.getWinnerMemberId(), winnerName, r.getBidAmount(), r.getDeductionAmount(),
				r.getPayoutAmount(), r.getPayoutDate(), r.getStatus());
	}

	private ChitContributionResponse contributionResponse(ChitContribution c, ChitMember member) {
		Customer customer = requiredCustomer(member.getCustomerId());
		String name = (customer.getFirstName() + " "
				+ (customer.getLastName() == null ? "" : customer.getLastName())).trim();
		return new ChitContributionResponse(c.getId(), c.getRoundId(), c.getMemberId(), name, c.getDueAmount(),
				c.getPaidAmount(), c.getPaidDate(), c.getPaymentMode(), c.getTransactionReference(), c.getStatus());
	}

	private ChitScheme lockedScheme(Long id) {
		return schemes.findForUpdate(id).orElseThrow(() -> notFound("Chit scheme not found"));
	}
	private ChitScheme requiredScheme(Long id) {
		return schemes.findById(id).orElseThrow(() -> notFound("Chit scheme not found"));
	}
	private ChitRound lockedRound(Long id) {
		return rounds.findForUpdate(id).orElseThrow(() -> notFound("Chit round not found"));
	}
	private ChitMember requiredMember(Long id) {
		return members.findById(id).orElseThrow(() -> notFound("Chit member not found"));
	}
	private Customer requiredCustomer(Long id) {
		return customers.findByIdAndDeletedFalse(id).orElseThrow(() -> notFound("Customer not found"));
	}
	private BigDecimal zero() { return BigDecimal.ZERO.setScale(2); }
	private ResponseStatusException notFound(String message) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
	}
	private ResponseStatusException conflict(String message) {
		return new ResponseStatusException(HttpStatus.CONFLICT, message);
	}
	private String generateCode() {
		String value;
		do value = "CHIT-" + LocalDate.now().getYear() + "-"
				+ UUID.randomUUID().toString().substring(0, 10).toUpperCase();
		while (schemes.existsByChitCodeIgnoreCase(value));
		return value;
	}
	private String generatedReference(String prefix) {
		String value;
		do value = prefix + "-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
		while (contributions.existsByTransactionReferenceIgnoreCase(value));
		return value;
	}
}

@RestController
@RequiredArgsConstructor
class ChitController {
	private final ChitService service;

	@GetMapping("/api/v2/chits")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> list(@RequestParam(required = false) ChitStatus status) {
		return ResponseEntity.ok(ResponseBuilder.success(service.list(status)));
	}

	@PostMapping("/api/v2/chits")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> create(@Valid @RequestBody ChitSchemeRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.create(request), "Chit scheme created"));
	}

	@GetMapping("/api/v2/chits/{id}/members")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> members(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.listMembers(id)));
	}

	@PostMapping("/api/v2/chits/{id}/members")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> addMember(@PathVariable Long id, @Valid @RequestBody ChitMemberRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.addMember(id, request), "Chit member added"));
	}

	@GetMapping("/api/v2/chits/{id}/rounds")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> rounds(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.listRounds(id)));
	}

	@PostMapping("/api/v2/chits/{id}/rounds")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> createRound(@PathVariable Long id, @Valid @RequestBody ChitRoundRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.createRound(id, request), "Chit round opened"));
	}

	@GetMapping("/api/v2/chit-rounds/{id}/contributions")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> contributions(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.listContributions(id)));
	}

	@PostMapping("/api/v2/chit-rounds/{id}/contributions")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> contribute(@PathVariable Long id, @Valid @RequestBody ContributionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.contribute(id, request), "Contribution recorded"));
	}

	@PostMapping("/api/v2/chit-rounds/{id}/winner")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> winner(@PathVariable Long id, @Valid @RequestBody WinnerRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.winner(id, request), "Winner recorded"));
	}

	@PostMapping("/api/v2/chit-rounds/{id}/payout")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> payout(@PathVariable Long id, @Valid @RequestBody PayoutRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.payout(id, request), "Payout recorded"));
	}
}
