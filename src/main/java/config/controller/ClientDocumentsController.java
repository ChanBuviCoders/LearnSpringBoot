package config.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import config.DTO.Response;
import config.Entity.ClientDocuments;
import config.Service.ClientDocumentS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Client document upload and download APIs")
public class ClientDocumentsController {

	private static final Logger logger = LoggerFactory.getLogger(ClientDocumentsController.class);

	private final ClientDocumentS clientDocumentService;

	@Operation(summary = "Upload documents to Azure Blob")
	@PostMapping(value = "/uploadImage", produces = "application/json")
	public Response uploadImage(@RequestParam("file") List<MultipartFile> multipartFiles,
			@RequestParam("uploadedBy") String uploadedBy, @RequestParam("userAccountId") long userAccountId) {
		Response response = new Response();
		try {
			return clientDocumentService.uploadImage(multipartFiles, uploadedBy, userAccountId);
		} catch (Exception e) {
			logger.error("uploadImage failed", e);
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "List uploaded files for a user")
	@PostMapping(value = "/getUploadedFileDetails", produces = "application/json")
	public Response getUploadedFileDetails(@RequestBody long userAccountId) {
		Response response = new Response();
		try {
			return clientDocumentService.getUploadedFileDetails(userAccountId);
		} catch (Exception e) {
			logger.error("getUploadedFileDetails failed", e);
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Delete file metadata")
	@PostMapping(value = "/deleteFileDetails", produces = "application/json")
	public Response deleteFileDetails(@RequestBody ClientDocuments clientDocuments) {
		Response response = new Response();
		try {
			return clientDocumentService.deleteFileDetails(clientDocuments);
		} catch (Exception e) {
			logger.error("deleteFileDetails failed", e);
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Download or preview a local document file")
	@RequestMapping("/file/{clientname}/{fileName}/{preview}")
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("clientname") String clientname, @PathVariable("fileName") String fileName,
			@PathVariable("preview") Boolean preview) throws IOException {
		clientDocumentService.downloadPDFResource(request, response, clientname, fileName, preview);
	}
}
