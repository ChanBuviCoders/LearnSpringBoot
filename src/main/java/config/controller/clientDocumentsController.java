package config.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import config.DAO.userAccountR;
import config.DTO.response;
import config.Entity.clientDocuments;
import config.Service.clientDocumentS;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "*")
@RestController()
@RequestMapping(path = "/api")
public class clientDocumentsController {

	@Autowired
	userAccountR userAccountR;

	@Autowired
	response response;

	@Autowired
	clientDocumentS clientDocumentS;

	@RequestMapping(value = "/uploadImage", method = RequestMethod.POST, produces = "application/Json")
	public response uploadImage(@RequestParam("file") List<MultipartFile> multipartFiles,
			@RequestParam("uploadedBy") String uploadedBy, @RequestParam("userAccountId") long userAccountId) {
		try {
			return clientDocumentS.uploadImage(multipartFiles, uploadedBy, userAccountId);
		} catch (Exception e) {
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}

	}

//	----------------------------------------get uploaded file details-------------------------------
	@RequestMapping(value = "/getUploadedFileDetails", method = RequestMethod.POST, produces = "application/Json")
	public response getUploadedFileDetails(@RequestBody long userAccountId) {
		try {
			return clientDocumentS.getUploadedFileDetails(userAccountId);
		} catch (Exception e) {
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}

	}

//	----------------------------------------get uploaded file details-------------------------------
	@RequestMapping(value = "/deleteFileDetails", method = RequestMethod.POST, produces = "application/Json")
	public response deleteFileDetails(@RequestBody clientDocuments clientDocuments) {
		try {
			return clientDocumentS.deleteFileDetails(clientDocuments);
		} catch (Exception e) {
			response.setMessage("Something went wrong");
			response.setStatus(false);
			return response;
		}

	}

	/****************************
	 * servlet api calling method
	 *****************************/
	@RequestMapping("/file/{clientname}/{fileName}/{preview}")
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("clientname") String clientname, @PathVariable("fileName") String fileName,
			@PathVariable("preview") Boolean preview) throws IOException {

		clientDocumentS.downloadPDFResource(request, response, clientname, fileName, preview);
	}

}
