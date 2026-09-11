package config.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import config.DTO.Response;
import config.Entity.ClientDocuments;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ClientDocumentS {

	Response uploadImage(List<MultipartFile> multipartFiles, String uploadedBy, long userAccountId);

	Response getUploadedFileDetails(long userAccountId);

	Response deleteFileDetails(ClientDocuments clientDocuments);

	void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, String clientname,
			String fileName, Boolean preview) throws IOException;
}
