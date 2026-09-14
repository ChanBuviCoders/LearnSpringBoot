package config.financial.lending;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Marker for the normalized customer module. Package-private implementation
 * types intentionally keep the public API DTO based.
 */
public final class CustomerModule {
	private CustomerModule() {
	}
}

enum CustomerStatus {
	ACTIVE, INACTIVE, BLOCKED
}

@Entity
@Table(name = "customers", schema = "finance")
@Getter
@Setter
class Customer extends BaseAuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "customer_code", nullable = false, unique = true, length = 30)
	private String customerCode;
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;
	@Column(name = "last_name", length = 100)
	private String lastName;
	@Column(name = "mobile_number", nullable = false, length = 20)
	private String mobileNumber;
	@Column(name = "alternate_mobile_number", length = 20)
	private String alternateMobileNumber;
	private String email;
	private String gender;
	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;
	@Column(name = "address_line1")
	private String addressLine1;
	@Column(name = "address_line2")
	private String addressLine2;
	private String city;
	private String state;
	@Column(name = "postal_code")
	private String postalCode;
	@Column(name = "identification_type")
	private String identificationType;
	@Column(name = "identification_number")
	private String identificationNumber;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CustomerStatus status = CustomerStatus.ACTIVE;
	@Column(nullable = false)
	private boolean deleted;
}

interface CustomerRepository extends JpaRepository<Customer, Long> {
	Page<Customer> findByDeletedFalse(Pageable pageable);

	@Query("""
			select c from Customer c
			where c.deleted = false
			  and (:search is null or :search = ''
			    or lower(c.customerCode) like lower(concat('%', :search, '%'))
			    or lower(c.firstName) like lower(concat('%', :search, '%'))
			    or lower(coalesce(c.lastName, '')) like lower(concat('%', :search, '%'))
			    or c.mobileNumber like concat('%', :search, '%'))
			""")
	Page<Customer> search(@Param("search") String search, Pageable pageable);

	java.util.Optional<Customer> findByIdAndDeletedFalse(Long id);
	boolean existsByCustomerCodeIgnoreCase(String code);
}

record CustomerRequest(
		@Size(max = 30) String customerCode,
		@NotBlank @Size(max = 100) String firstName,
		@Size(max = 100) String lastName,
		@NotBlank @Size(max = 20) String mobileNumber,
		@Size(max = 20) String alternateMobileNumber,
		@Email @Size(max = 200) String email,
		@Size(max = 30) String gender,
		LocalDate dateOfBirth,
		@Size(max = 250) String addressLine1,
		@Size(max = 250) String addressLine2,
		@Size(max = 100) String city,
		@Size(max = 100) String state,
		@Size(max = 20) String postalCode,
		@Size(max = 50) String identificationType,
		@Size(max = 100) String identificationNumber,
		CustomerStatus status) {
}

record CustomerResponse(Long id, String customerCode, String firstName, String lastName,
		String mobileNumber, String alternateMobileNumber, String email, String gender,
		LocalDate dateOfBirth, String addressLine1, String addressLine2, String city,
		String state, String postalCode, String identificationType,
		String identificationNumber, CustomerStatus status, Long version) {
	static CustomerResponse from(Customer c) {
		return new CustomerResponse(c.getId(), c.getCustomerCode(), c.getFirstName(), c.getLastName(),
				c.getMobileNumber(), c.getAlternateMobileNumber(), c.getEmail(), c.getGender(),
				c.getDateOfBirth(), c.getAddressLine1(), c.getAddressLine2(), c.getCity(), c.getState(),
				c.getPostalCode(), c.getIdentificationType(), c.getIdentificationNumber(), c.getStatus(),
				c.getVersion());
	}
}

@Service
@RequiredArgsConstructor
class CustomerServiceV2 {
	private final CustomerRepository repository;
	private final AuditService auditService;

	@Transactional
	CustomerResponse create(CustomerRequest request) {
		String customerCode = supplied(request.customerCode())
				? request.customerCode().trim()
				: generateCustomerCode();
		if (repository.existsByCustomerCodeIgnoreCase(customerCode)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer code already exists");
		}
		Customer customer = new Customer();
		apply(customer, request, customerCode);
		customer = repository.save(customer);
		CustomerResponse response = CustomerResponse.from(customer);
		auditService.record("Customer", customer.getId(), AuditAction.CREATE, null, response);
		return response;
	}

	@Transactional(readOnly = true)
	Page<CustomerResponse> list(String search, Pageable pageable) {
		return repository.search(search, pageable).map(CustomerResponse::from);
	}

	@Transactional(readOnly = true)
	CustomerResponse get(Long id) {
		return CustomerResponse.from(required(id));
	}

	@Transactional
	CustomerResponse update(Long id, CustomerRequest request) {
		Customer customer = required(id);
		CustomerResponse old = CustomerResponse.from(customer);
		CustomerStatus previousStatus = customer.getStatus();
		String customerCode = supplied(request.customerCode())
				? request.customerCode().trim()
				: customer.getCustomerCode();
		if (!customer.getCustomerCode().equalsIgnoreCase(customerCode)
				&& repository.existsByCustomerCodeIgnoreCase(customerCode)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer code already exists");
		}
		apply(customer, request, customerCode);
		CustomerResponse response = CustomerResponse.from(repository.save(customer));
		auditService.record("Customer", id, AuditAction.UPDATE, old, response);
		if (previousStatus != customer.getStatus()) {
			auditService.record("Customer", id, AuditAction.STATUS_CHANGE, previousStatus, customer.getStatus());
		}
		return response;
	}

	@Transactional
	void softDelete(Long id) {
		Customer customer = required(id);
		CustomerResponse old = CustomerResponse.from(customer);
		customer.setDeleted(true);
		customer.setStatus(CustomerStatus.INACTIVE);
		repository.save(customer);
		auditService.record("Customer", id, AuditAction.DELETE, old, CustomerResponse.from(customer));
	}

	Customer required(Long id) {
		return repository.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
	}

	private void apply(Customer c, CustomerRequest r, String customerCode) {
		c.setCustomerCode(customerCode);
		c.setFirstName(r.firstName().trim());
		c.setLastName(r.lastName());
		c.setMobileNumber(r.mobileNumber().trim());
		c.setAlternateMobileNumber(r.alternateMobileNumber());
		c.setEmail(r.email());
		c.setGender(r.gender());
		c.setDateOfBirth(r.dateOfBirth());
		c.setAddressLine1(r.addressLine1());
		c.setAddressLine2(r.addressLine2());
		c.setCity(r.city());
		c.setState(r.state());
		c.setPostalCode(r.postalCode());
		c.setIdentificationType(r.identificationType());
		c.setIdentificationNumber(r.identificationNumber());
		if (r.status() != null) {
			c.setStatus(r.status());
		}
	}

	private String generateCustomerCode() {
		String candidate;
		do {
			candidate = "CUS-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
		} while (repository.existsByCustomerCodeIgnoreCase(candidate));
		return candidate;
	}

	private boolean supplied(String value) {
		return value != null && !value.isBlank();
	}
}

@RestController
@RequestMapping("/api/v2/customers")
@RequiredArgsConstructor
class CustomerControllerV2 {
	private final CustomerServiceV2 service;

	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> create(@Valid @RequestBody CustomerRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.create(request), "Customer created"));
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> list(@RequestParam(required = false) String search, Pageable pageable) {
		return ResponseEntity.ok(ResponseBuilder.success(service.list(search, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> get(@PathVariable Long id) {
		return ResponseEntity.ok(ResponseBuilder.success(service.get(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.update(id, request), "Customer updated"));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> delete(@PathVariable Long id) {
		service.softDelete(id);
		return ResponseEntity.ok(ResponseBuilder.success("Customer deleted"));
	}
}
