package config.serviceI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import config.DAO.ClientDocumentsR;
import config.DTO.Response;
import config.Entity.ClientDocuments;
import config.Service.ClientDocumentS;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Service for handling client document uploads and downloads Paths are
 * configurable via application.properties and environment variables
 */
@Service
@RequiredArgsConstructor
public class ClientDocumentI implements ClientDocumentS {

	private static final Logger logger = Logger.getLogger(ClientDocumentI.class.getName());

	private final ClientDocumentsR clientDocumentsR;

	// Inject server-level configurable paths
	@Value("${app.client.documents.api-path:./documents/apiDocuments}")
	private String apiDocumentsPath;

	@Value("${app.client.documents.client-path:./documents/clientDocuments}")
	private String clientDocumentsPath;

	@Override
	public Response uploadImage(List<MultipartFile> multipartFiles, String uploadedBy, long userAccountId) {
		Response response = new Response();
		try {
			// Use configurable API documents path
			String dPath = apiDocumentsPath + File.separator + uploadedBy;
			File dirFile = new File(dPath);

			// Create directory if it doesn't exist
			if (!dirFile.exists()) {
				Files.createDirectories(Paths.get(dPath));
				logger.info("Created directory: " + dPath);
			}

			StringBuffer failedFiles = new StringBuffer();
			int successCount = 0;

			for (MultipartFile file : multipartFiles) {
				String filename = file.getOriginalFilename();
				String filePath = dPath + File.separator + filename;
				File fileToCheck = new File(filePath);

				if (!fileToCheck.exists()) {
					// Save file metadata to database
					ClientDocuments clientDoc = new ClientDocuments();
					clientDoc.setFileName(file.getOriginalFilename());
					clientDoc.setFileType(file.getContentType());
					clientDoc.setFileSize(String.valueOf(file.getSize()));
					clientDoc.setUploadedBy(uploadedBy);
					clientDoc.setFilePath(dPath);
					clientDoc.setUserAccountId(userAccountId);
					clientDoc.setUploadedDate(new Date(System.currentTimeMillis()));
					clientDocumentsR.save(clientDoc);

					// Save file to disk
					file.transferTo(Paths.get(filePath));
					successCount++;
					logger.info("File uploaded successfully: " + filename);
				} else {
					// File already exists
					failedFiles.append((failedFiles.length() > 0 ? "," : "") + filename);
				}
			}

			response.setStatus(true);
			if (successCount > 0) {
				response.setMessage("Successfully uploaded " + successCount + " file(s)");
			}

			// Add warning about existing files
			if (failedFiles.length() > 0) {
				response.setStatus(true);
				response.setMessage(failedFiles.toString() + " - these files already exist");
			}

			return response;

		} catch (Exception e) {
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}

	}

	/*-------------------------get uploaded file details-------------------------------*/
	@Override
	public Response getUploadedFileDetails(@RequestBody long userAccountId) {
		Response response = new Response();
		List<ClientDocuments> fub = null;
		try {
			fub = clientDocumentsR.findAllByUserAccountId(userAccountId);
			response.setData(fub);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Failure");
			return response;
		}
	}

	/*---------------------------delete uploaded file details-------------------------------*/
	@Override
	public Response deleteFileDetails(ClientDocuments clientDocuments) {
		Response response = new Response();
		try {
			clientDocumentsR.deleteById(clientDocuments.getFileId());

			// Use cross-platform file separator
			File file = new File(clientDocuments.getFilePath() + File.separator + clientDocuments.getFileName());
			if (file.exists() && file.delete()) {
				logger.info("File deleted successfully: " + file.getAbsolutePath());
			}

			response.setStatus(true);
			response.setMessage("Deleted Successfully");
			return response;
		} catch (Exception e) {
			logger.warning("Error deleting file: " + e.getMessage());
			response.setStatus(false);
			response.setMessage("Error deleting file");
			return response;
		}
	}

	/****************************
	 * servlet api calling method
	 *****************************/
	@Override
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, String clientname,
			String fileName, Boolean preview) throws IOException {

		// Use configurable API documents path
		File file = new File(apiDocumentsPath + File.separator + clientname + File.separator + fileName);

		if (file.exists()) {
			logger.info("Preparing to download file: " + file.getAbsolutePath());

			// Determine MIME type
			String mimeType = URLConnection.guessContentTypeFromName(file.getName());
			if (mimeType == null) {
				// Default to application/octet-stream for unknown types
				mimeType = "application/octet-stream";
			}

			response.setContentType(mimeType);

			/**
			 * Content-Disposition header: controls whether content is displayed inline in
			 * browser or downloaded as attachment
			 */
			if (preview) {
				// Display inline in browser
				response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
			} else {
				// Download as attachment
				response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
			}

			response.setContentLength((int) file.length());

			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, response.getOutputStream());

			logger.info("File downloaded successfully: " + fileName);
		} else {
			logger.warning("File not found: " + file.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
		}
	}
}
