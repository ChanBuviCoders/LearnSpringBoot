package config.financial.settings;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import config.DTO.Response;
import config.commonConfig.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/settings")
@RequiredArgsConstructor
public class BusinessSettingController {

	private final BusinessSettingService service;

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ACCOUNTANT', 'ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_USER_1')")
	public ResponseEntity<Response> getAggregate() {
		return ResponseEntity.ok(ResponseBuilder.success(service.getAggregate()));
	}

	@PutMapping
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_USER_1')")
	public ResponseEntity<Response> updateAggregate(
			@Valid @RequestBody BusinessSettingsRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(
				service.updateAggregate(request), "Settings updated"));
	}

	@GetMapping("/entries")
	@PreAuthorize("hasAnyAuthority('ROLE_ACCOUNTANT', 'ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_USER_1')")
	public ResponseEntity<Response> findAll() {
		return ResponseEntity.ok(ResponseBuilder.success(service.findAll()));
	}

	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER_1')")
	public ResponseEntity<Response> create(@Valid @RequestBody BusinessSettingRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.save(request), "Setting saved"));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER_1')")
	public ResponseEntity<Response> update(
			@PathVariable Long id,
			@Valid @RequestBody BusinessSettingRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.update(id, request), "Setting updated"));
	}
}
