package config.financial.audit;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import config.DTO.Response;
import config.commonConfig.ResponseBuilder;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/audit")
@RequiredArgsConstructor
public class AuditController {

	private final AuditService service;

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT', 'ROLE_USER_1')")
	public ResponseEntity<Response> search(
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to,
			@RequestParam(required = false) String actor,
			@RequestParam(required = false) String action,
			@RequestParam(required = false) String entityType,
			@RequestParam(required = false) String entityId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.ok(ResponseBuilder.success(
				service.search(from, to, actor, action, entityType, entityId, page, size)));
	}
}
