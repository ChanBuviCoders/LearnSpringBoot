package config.serviceI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import config.DAO.ClientDocumentsR;
import config.DTO.Response;
import config.Entity.ClientDocuments;
import config.Service.AzureBlobService;
import config.Service.ClientDocumentS;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientDocumentI implements ClientDocumentS {

	private static final Logger logger = LoggerFactory.getLogger(ClientDocumentI.class);

	private final ClientDocumentsR clientDocumentsR;
	private final AzureBlobService azureBlobService;

	@Value("${app.client.documents.api-path:./documents/apiDocuments}")
	private String apiDocumentsPath;

	@Value("${app.client.documents.client-path:./documents/clientDocuments}")
	private String clientDocumentsPath;

	@Override
	public Response uploadImage(List<MultipartFile> multipartFiles, String uploadedBy, long userAccountId) {
		Response response = new Response();
		try {
			for (MultipartFile file : multipartFiles) {
				String fileName = azureBlobService.uploadFile(file, "client");
				ClientDocuments clientDoc = new ClientDocuments();
				clientDoc.setFileName(file.getOriginalFilename());
				clientDoc.setFileType(file.getContentType());
				clientDoc.setFileSize(String.valueOf(file.getSize()));
				clientDoc.setUploadedBy(uploadedBy);
				clientDoc.setFilePath(fileName);
				clientDoc.setUserAccountId(userAccountId);
				clientDoc.setUploadedDate(new Date(System.currentTimeMillis()));
				clientDocumentsR.save(clientDoc);
				logger.info("File uploaded successfully: {}", fileName);
			}
			response.setStatus(true);
			response.setMessage("Successfully uploaded");
			return response;
		} catch (Exception e) {
			logger.error("uploadImage failed", e);
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Override
	public Response getUploadedFileDetails(long userAccountId) {
		Response response = new Response();
		try {
			List<ClientDocuments> files = clientDocumentsR.findAllByUserAccountId(userAccountId);
			files = files.stream().map(document -> {
				document.setFilePath(azureBlobService.generateReadToken(document.getFilePath()));
				return document;
			}).toList();
			response.setData(files);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getUploadedFileDetails failed", e);
			response.setStatus(false);
			response.setMessage("Failure");
			return response;
		}
	}

	@Override
	public Response deleteFileDetails(ClientDocuments clientDocuments) {
		Response response = new Response();
		try {
			clientDocumentsR.deleteById(clientDocuments.getFileId());
			File file = new File(clientDocuments.getFilePath() + File.separator + clientDocuments.getFileName());
			if (file.exists() && file.delete()) {
				logger.info("File deleted successfully: {}", file.getAbsolutePath());
			}
			response.setStatus(true);
			response.setMessage("Deleted Successfully");
			return response;
		} catch (Exception e) {
			logger.warn("Error deleting file: {}", e.getMessage());
			response.setStatus(false);
			response.setMessage("Error deleting file");
			return response;
		}
	}

	@Override
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, String clientname,
			String fileName, Boolean preview) throws IOException {

		File file = new File(apiDocumentsPath + File.separator + clientname + File.separator + fileName);

		if (!file.exists()) {
			logger.warn("File not found: {}", file.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
			return;
		}

		logger.info("Preparing to download file: {}", file.getAbsolutePath());
		String mimeType = URLConnection.guessContentTypeFromName(file.getName());
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		response.setContentType(mimeType);
		if (Boolean.TRUE.equals(preview)) {
			response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
		} else {
			response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		}
		response.setContentLength((int) file.length());
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		}
		logger.info("File downloaded successfully: {}", fileName);
	}
}
