package config.financial.lending;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public final class LoanProductModule {
	private LoanProductModule() {
	}
}

enum CollectionFrequency {
	DAILY, WEEKLY, MONTHLY
}

enum InterestMethod {
	UPFRONT, FLAT, REDUCING_BALANCE
}

@Entity
@Table(name = "loan_products", schema = "finance")
@Getter
@Setter
class LoanProduct extends BaseAuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "product_code", nullable = false, unique = true, length = 30)
	private String productCode;
	@Column(name = "product_name", nullable = false, length = 150)
	private String productName;
	@Enumerated(EnumType.STRING)
	@Column(name = "collection_frequency", nullable = false)
	private CollectionFrequency collectionFrequency;
	@Enumerated(EnumType.STRING)
	@Column(name = "interest_method", nullable = false)
	private InterestMethod interestMethod;
	@Column(name = "default_interest_rate", nullable = false, precision = 9, scale = 4)
	private BigDecimal defaultInterestRate;
	@Column(name = "default_term_count")
	private Integer defaultTermCount;
	@Column(name = "min_amount", precision = 19, scale = 2)
	private BigDecimal minAmount;
	@Column(name = "max_amount", precision = 19, scale = 2)
	private BigDecimal maxAmount;
	@Column(nullable = false)
	private boolean active = true;
}

interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
	boolean existsByProductCodeIgnoreCase(String code);
}

record LoanProductRequest(
		@NotBlank @Size(max = 30) String productCode,
		@NotBlank @Size(max = 150) String productName,
		@NotNull CollectionFrequency collectionFrequency,
		@NotNull InterestMethod interestMethod,
		@NotNull @DecimalMin("0.0") BigDecimal defaultInterestRate,
		@Positive Integer defaultTermCount,
		@DecimalMin("0.01") BigDecimal minAmount,
		@DecimalMin("0.01") BigDecimal maxAmount,
		Boolean active) {
}

record LoanProductResponse(Long id, String productCode, String productName,
		CollectionFrequency collectionFrequency, InterestMethod interestMethod,
		BigDecimal defaultInterestRate, Integer defaultTermCount, BigDecimal minAmount,
		BigDecimal maxAmount, boolean active, Long version) {
	static LoanProductResponse from(LoanProduct p) {
		return new LoanProductResponse(p.getId(), p.getProductCode(), p.getProductName(),
				p.getCollectionFrequency(), p.getInterestMethod(), p.getDefaultInterestRate(),
				p.getDefaultTermCount(), p.getMinAmount(), p.getMaxAmount(), p.isActive(), p.getVersion());
	}
}

@Service
@RequiredArgsConstructor
class LoanProductService {
	private final LoanProductRepository repository;
	private final AuditService auditService;

	@Transactional
	LoanProductResponse create(LoanProductRequest request) {
		validate(request, null);
		LoanProduct product = new LoanProduct();
		apply(product, request);
		product = repository.save(product);
		LoanProductResponse response = LoanProductResponse.from(product);
		auditService.record("LoanProduct", product.getId(), AuditAction.CREATE, null, response);
		return response;
	}

	@Transactional(readOnly = true)
	Page<LoanProductResponse> list(Pageable pageable) {
		return repository.findAll(pageable).map(LoanProductResponse::from);
	}

	@Transactional(readOnly = true)
	LoanProductResponse get(Long id) {
		return LoanProductResponse.from(required(id));
	}

	@Transactional
	LoanProductResponse update(Long id, LoanProductRequest request) {
		LoanProduct product = required(id);
		LoanProductResponse old = LoanProductResponse.from(product);
		boolean previouslyActive = product.isActive();
		validate(request, product);
		apply(product, request);
		LoanProductResponse response = LoanProductResponse.from(repository.save(product));
		auditService.record("LoanProduct", id, AuditAction.UPDATE, old, response);
		if (previouslyActive != product.isActive()) {
			auditService.record("LoanProduct", id, AuditAction.STATUS_CHANGE,
					previouslyActive, product.isActive());
		}
		return response;
	}

	LoanProduct required(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan product not found"));
	}

	private void validate(LoanProductRequest r, LoanProduct existing) {
		if ((existing == null || !existing.getProductCode().equalsIgnoreCase(r.productCode()))
				&& repository.existsByProductCodeIgnoreCase(r.productCode())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Product code already exists");
		}
		InterestMethod expected = switch (r.collectionFrequency()) {
			case DAILY -> InterestMethod.UPFRONT;
			case WEEKLY -> InterestMethod.FLAT;
			case MONTHLY -> InterestMethod.REDUCING_BALANCE;
		};
		if (r.interestMethod() != expected) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Interest method must be " + expected + " for " + r.collectionFrequency());
		}
		if (r.minAmount() != null && r.maxAmount() != null && r.minAmount().compareTo(r.maxAmount()) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum amount exceeds maximum amount");
		}
	}

	private void apply(LoanProduct p, LoanProductRequest r) {
		p.setProductCode(r.productCode().trim());
		p.setProductName(r.productName().trim());
		p.setCollectionFrequency(r.collectionFrequency());
		p.setInterestMethod(r.interestMethod());
		p.setDefaultInterestRate(r.defaultInterestRate());
		p.setDefaultTermCount(r.defaultTermCount());
		p.setMinAmount(Money.round(r.minAmount()));
		p.setMaxAmount(Money.round(r.maxAmount()));
		p.setActive(r.active() == null || r.active());
	}
}

@RestController
@RequestMapping("/api/v2/loan-products")
@RequiredArgsConstructor
class LoanProductController {
	private final LoanProductService service;

	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> create(@Valid @RequestBody LoanProductRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.create(request), "Loan product created"));
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> list(Pageable pageable) {
		return ResponseEntity.ok(ResponseBuilder.success(service.list(pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> get(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.get(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> update(@PathVariable Long id, @Valid @RequestBody LoanProductRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.update(id, request), "Loan product updated"));
	}
}
