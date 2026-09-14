package config.financial.audit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository repository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void record(
			String entityType,
			Object entityId,
			AuditAction action,
			Object oldValues,
			Object newValues) {
		AuditLog log = new AuditLog();
		log.setEntityType(entityType);
		log.setEntityId(String.valueOf(entityId));
		log.setAction(action);
		log.setOldValues(toJson(oldValues));
		log.setNewValues(toJson(newValues));
		log.setPerformedBy(currentUsername());
		log.setPerformedAt(Instant.now());
		log.setCorrelationId(UUID.randomUUID().toString());
		repository.save(log);
	}

	@Transactional(readOnly = true)
	public Page<AuditEntryResponse> search(LocalDate from, LocalDate to, String actor,
			String action, String entityType, String entityId, int page, int size) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException("'from' must not be after 'to'");
		}
		PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
				Sort.by(Sort.Direction.DESC, "performedAt"));
		Specification<AuditLog> specification = Specification.where(null);
		if (from != null) {
			Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
			specification = specification.and((root, query, cb) ->
					cb.greaterThanOrEqualTo(root.get("performedAt"), start));
		}
		if (to != null) {
			Instant endExclusive = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
			specification = specification.and((root, query, cb) ->
					cb.lessThan(root.get("performedAt"), endExclusive));
		}
		if (actor != null && !actor.isBlank()) {
			specification = specification.and((root, query, cb) ->
					cb.like(cb.lower(root.get("performedBy")), "%" + actor.toLowerCase() + "%"));
		}
		if (action != null && !action.isBlank()) {
			AuditAction parsed;
			try {
				parsed = AuditAction.valueOf(action.trim().toUpperCase());
			} catch (IllegalArgumentException ex) {
				return Page.empty(pageable);
			}
			specification = specification.and((root, query, cb) -> cb.equal(root.get("action"), parsed));
		}
		if (entityType != null && !entityType.isBlank()) {
			specification = specification.and((root, query, cb) ->
					cb.equal(cb.lower(root.get("entityType")), entityType.toLowerCase()));
		}
		if (entityId != null && !entityId.isBlank()) {
			specification = specification.and((root, query, cb) -> cb.equal(root.get("entityId"), entityId));
		}
		return repository.findAll(specification, pageable).map(this::toResponse);
	}

	private String currentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication == null || authentication.getName() == null
				? "SYSTEM"
				: authentication.getName();
	}

	private String toJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Could not serialize audit values", ex);
		}
	}

	private AuditEntryResponse toResponse(AuditLog log) {
		Object before = fromJson(log.getOldValues());
		Object after = fromJson(log.getNewValues());
		return new AuditEntryResponse(log.getId(), log.getPerformedAt(), log.getPerformedBy(), null,
				log.getAction().name(), log.getEntityType(), log.getEntityId(),
				transactionReference(before, after), log.getAction() + " " + log.getEntityType()
						+ " #" + log.getEntityId(),
				before, after);
	}

	private Object fromJson(String value) {
		if (value == null) return null;
		try {
			return objectMapper.readValue(value, Object.class);
		} catch (JsonProcessingException ex) {
			return value;
		}
	}

	private String transactionReference(Object before, Object after) {
		String value = mapReference(after);
		return value == null ? mapReference(before) : value;
	}

	private String mapReference(Object value) {
		if (!(value instanceof Map<?, ?> map)) return null;
		Object reference = map.get("transactionReference");
		if (reference == null) reference = map.get("payoutReference");
		return reference == null ? null : String.valueOf(reference);
	}
}

record AuditEntryResponse(Long id, Instant occurredAt, String actorName, String actorRole,
		String action, String entityType, String entityId, String transactionReference,
		String summary, Object beforeValue, Object afterValue) {
}
