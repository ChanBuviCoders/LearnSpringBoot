package config.financial.audit;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_logs", schema = "finance")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "entity_type", nullable = false, length = 120)
	private String entityType;

	@Column(name = "entity_id", nullable = false, length = 80)
	private String entityId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AuditAction action;

	@Column(name = "old_values", columnDefinition = "nvarchar(max)")
	private String oldValues;

	@Column(name = "new_values", columnDefinition = "nvarchar(max)")
	private String newValues;

	@Column(name = "performed_by", nullable = false, length = 100)
	private String performedBy;

	@Column(name = "performed_at", nullable = false)
	private Instant performedAt;

	@Column(name = "ip_address", length = 64)
	private String ipAddress;

	@Column(name = "correlation_id", length = 80)
	private String correlationId;
}
