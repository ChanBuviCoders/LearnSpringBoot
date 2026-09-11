package config.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

@Service
public class AzureBlobService {

	private static final Logger logger = LoggerFactory.getLogger(AzureBlobService.class);

	private final ObjectProvider<BlobServiceClient> blobServiceClientProvider;

	@Value("${azure.storage.container-name:fullstackstoragecontainer}")
	private String containerName;

	public AzureBlobService(ObjectProvider<BlobServiceClient> blobServiceClientProvider) {
		this.blobServiceClientProvider = blobServiceClientProvider;
	}

	public String uploadFile(MultipartFile file, String documentType) throws IOException {
		BlobServiceClient blobServiceClient = requireClient();
		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

		if (!containerClient.exists()) {
			containerClient.create();
		}

		String fileName = generateFileName(file, documentType);
		BlobClient blobClient = containerClient.getBlobClient(fileName);
		BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
		blobClient.upload(file.getInputStream(), file.getSize(), true);
		blobClient.setHttpHeaders(headers);
		logger.info("Uploaded blob: {}", fileName);
		return fileName;
	}

	private String generateFileName(MultipartFile file, String documentType) {
		String originalFileName = file.getOriginalFilename();
		String extension = "";
		String baseName = originalFileName;

		if (originalFileName != null && originalFileName.contains(".")) {
			int lastDot = originalFileName.lastIndexOf(".");
			extension = originalFileName.substring(lastDot);
			baseName = originalFileName.substring(0, lastDot);
		}

		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
		return documentType + "_" + timestamp + "_" + baseName + extension;
	}

	public String generateReadToken(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return fileName;
		}
		BlobServiceClient blobServiceClient = blobServiceClientProvider.getIfAvailable();
		if (blobServiceClient == null) {
			logger.warn("Azure Blob is not configured; returning raw file name");
			return fileName;
		}

		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
		BlobClient blobClient = containerClient.getBlobClient(fileName);
		BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
		OffsetDateTime startTime = OffsetDateTime.now().minusMinutes(1);
		OffsetDateTime expiry = OffsetDateTime.now().plusMinutes(30);
		BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiry, permission)
				.setStartTime(startTime);
		String sasToken = blobClient.generateSas(values);
		return blobClient.getBlobUrl() + "?" + sasToken;
	}

	private BlobServiceClient requireClient() {
		BlobServiceClient client = blobServiceClientProvider.getIfAvailable();
		if (client == null) {
			throw new IllegalStateException(
					"Azure storage is not configured. Set AZURE_STORAGE_CONNECTION_STRING / azure.storage.connection-string");
		}
		return client;
	}
}
