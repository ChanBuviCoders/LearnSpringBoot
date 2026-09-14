package config.financial.documents;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import config.DTO.Response;
import config.Service.AzureBlobService;
import config.commonConfig.ResponseBuilder;
import config.financial.audit.AuditAction;
import config.financial.audit.AuditService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public final class FinanceAttachmentModule {
	private FinanceAttachmentModule() {
	}
}

@Entity
@Table(name = "attachments", schema = "finance")
@Getter
@Setter
class FinanceAttachment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "entity_type", nullable = false, length = 80)
	private String entityType;
	@Column(name = "entity_id", nullable = false, length = 80)
	private String entityId;
	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;
	@Column(name = "content_type", length = 120)
	private String contentType;
	@Column(name = "file_size")
	private Long fileSize;
	@Column(name = "storage_path", nullable = false, length = 500)
	private String storagePath;
	@Column(name = "uploaded_by", nullable = false, length = 100)
	private String uploadedBy;
	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;
}

interface FinanceAttachmentRepository extends JpaRepository<FinanceAttachment, Long> {
	List<FinanceAttachment> findByEntityTypeIgnoreCaseAndEntityIdOrderByUploadedAtDesc(
			String entityType, String entityId);
}

record AttachmentResponse(Long id, String entityType, String entityId, String fileName,
		String contentType, Long fileSize, String downloadUrl, String uploadedBy, Instant uploadedAt) {
}

@Service
@RequiredArgsConstructor
class FinanceAttachmentService {
	private final FinanceAttachmentRepository repository;
	private final AzureBlobService azureBlobService;
	private final AuditService auditService;

	@Transactional
	AttachmentResponse upload(String entityType, String entityId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A file is required");
		}
		String type = normalizeEntityType(entityType);
		String id = normalizeEntityId(entityId);
		validateFile(file);
		try {
			String stored = azureBlobService.uploadFile(file, type + "-" + id);
			FinanceAttachment attachment = new FinanceAttachment();
			attachment.setEntityType(type);
			attachment.setEntityId(id);
			attachment.setFileName(safeFileName(file));
			attachment.setContentType(file.getContentType());
			attachment.setFileSize(file.getSize());
			attachment.setStoragePath(stored);
			attachment.setUploadedBy(currentUser());
			attachment.setUploadedAt(Instant.now());
			attachment = repository.save(attachment);
			AttachmentResponse response = toResponse(attachment);
			auditService.record("Attachment", attachment.getId(), AuditAction.CREATE, null, response);
			return response;
		} catch (IllegalStateException ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
		} catch (IOException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to store the attachment");
		}
	}

	@Transactional(readOnly = true)
	List<AttachmentResponse> list(String entityType, String entityId) {
		return repository.findByEntityTypeIgnoreCaseAndEntityIdOrderByUploadedAtDesc(
				normalizeEntityType(entityType), normalizeEntityId(entityId))
				.stream().map(this::toResponse).toList();
	}

	private AttachmentResponse toResponse(FinanceAttachment attachment) {
		return new AttachmentResponse(attachment.getId(), attachment.getEntityType(), attachment.getEntityId(),
				attachment.getFileName(), attachment.getContentType(), attachment.getFileSize(),
				azureBlobService.generateReadToken(attachment.getStoragePath()),
				attachment.getUploadedBy(), attachment.getUploadedAt());
	}

	private String currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication == null || authentication.getName() == null ? "SYSTEM" : authentication.getName();
	}

	private void validateFile(MultipartFile file) {
		if (file.getSize() > MAX_ATTACHMENT_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be 10 MB or smaller");
		}
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
		String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
		boolean allowedType = ALLOWED_CONTENT_TYPES.contains(contentType);
		boolean allowedName = ALLOWED_EXTENSIONS.stream().anyMatch(name::endsWith);
		if (!allowedType && !allowedName) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF, JPEG, PNG, or WebP files are allowed");
		}
	}

	private String normalizeEntityType(String entityType) {
		if (entityType == null || entityType.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entity type is required");
		}
		String type = entityType.trim().toUpperCase(Locale.ROOT);
		if (!ALLOWED_ENTITY_TYPES.contains(type)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported attachment entity");
		}
		return type;
	}

	private String normalizeEntityId(String entityId) {
		if (entityId == null || !entityId.trim().matches("\\d{1,18}")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A numeric entity id is required");
		}
		return entityId.trim();
	}

	private String safeFileName(MultipartFile file) {
		String name = file.getOriginalFilename();
		if (name == null || name.isBlank()) {
			return "document";
		}
		return name.replaceAll("[\\\\/]+", "_");
	}

	private static final long MAX_ATTACHMENT_BYTES = 10L * 1024 * 1024;
	private static final Set<String> ALLOWED_ENTITY_TYPES = Set.of("CUSTOMER", "LOAN", "CHIT");
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"application/pdf", "image/jpeg", "image/png", "image/webp");
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".jpg", ".jpeg", ".png", ".webp");
}

@RestController
@RequestMapping("/api/v2/attachments")
@RequiredArgsConstructor
class FinanceAttachmentController {
	private final FinanceAttachmentService service;

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> list(@RequestParam String entityType, @RequestParam String entityId) {
		return ResponseEntity.ok(ResponseBuilder.success(service.list(entityType, entityId)));
	}

	@PostMapping("/{entityType}/{entityId}")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> upload(
			@PathVariable String entityType,
			@PathVariable String entityId,
			@RequestParam("file") MultipartFile file) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseBuilder.success(service.upload(entityType, entityId, file), "Attachment uploaded"));
	}
}
