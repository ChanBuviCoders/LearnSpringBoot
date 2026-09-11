package config.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AzureBlobService {

	private final BlobServiceClient blobServiceClient;

	@Value("${azure.storage.container-name}")
	private String containerName;

	public String uploadFile(MultipartFile file, String documentType) throws IOException {

		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

		if (!containerClient.exists()) {
			containerClient.create();
		}

		String fileName = generateFileName(file, documentType);

		BlobClient blobClient = containerClient.getBlobClient(fileName);

		BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());

		blobClient.upload(file.getInputStream(), file.getSize(), true);

		blobClient.setHttpHeaders(headers);

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
}