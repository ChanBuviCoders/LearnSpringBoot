package config.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import config.DTO.Response;
import config.Entity.ClientDocuments;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface ClientDocumentS {

	void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, String clientname,
			String fileName, Boolean preview) throws IOException;

	Response deleteFileDetails(ClientDocuments clientDocuments);

	Response getUploadedFileDetails(long userAccountId);

	Response uploadImage(List<MultipartFile> multipartFiles, String uploadedBy, long userAccountId);

}
