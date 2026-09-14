package config.financial.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

	Page<AuditLog> findByEntityTypeIgnoreCase(String entityType, Pageable pageable);

	Page<AuditLog> findByEntityTypeIgnoreCaseAndEntityId(
			String entityType,
			String entityId,
			Pageable pageable);
}
