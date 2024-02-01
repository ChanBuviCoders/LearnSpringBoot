package config.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import config.DTO.response;
import config.Entity.clientDocuments;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface clientDocumentS {

	void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, String clientname,
			String fileName, Boolean preview) throws IOException;

	response deleteFileDetails(clientDocuments clientDocuments);

	response getUploadedFileDetails(long userAccountId);

	response uploadImage(List<MultipartFile> multipartFiles, String uploadedBy, long userAccountId);

}
