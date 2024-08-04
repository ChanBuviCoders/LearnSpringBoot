package config.serviceI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import config.DAO.clientDocumentsR;
import config.DTO.response;
import config.Entity.clientDocuments;
import config.Service.clientDocumentS;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class clientDocumentI implements clientDocumentS {

	@Autowired
	clientDocumentsR clientDocumentsR;

	String apiFilePath = "C:\\Users\\murug\\projects\\LearnSpringBoot-main\\documents\\apiDocuments";

	@Override
	public response uploadImage(List<MultipartFile> multipartFiles, String uploadedBy, long userAccountId) {
		response response = new response();
		try {

			String dPath = apiFilePath.concat("\\" + uploadedBy);// dPath--->Directory path
			File f = new File(dPath);

			if (!f.exists() && !f.isDirectory())
				f.mkdir();

			StringBuffer SB = new StringBuffer();
			SB.append("");

			for (MultipartFile file : multipartFiles) {

				String filename = file.getOriginalFilename();
				String filePath = dPath + File.separator + filename;
				File ifExist = new File(filePath);
				if (!ifExist.exists()) {
					clientDocuments fub = new clientDocuments();
					fub.setFileName(file.getOriginalFilename()); // file upload bean
					fub.setFileType(file.getContentType());
					fub.setFileSize(String.valueOf(file.getSize()));
					fub.setUploadedBy(uploadedBy);
					fub.setFilePath(dPath);
					fub.setUserAccountId(userAccountId);
					fub.setUploadedDate(new Date(System.currentTimeMillis()));;
					clientDocumentsR.save(fub);
					file.transferTo(Paths.get(filePath));
				} else {

					SB.append((SB.isEmpty() ? "" : ",") + filename);
					;
				}
			}

			response.setMessage("Saved Successfully");
			response.setStatus(true);

			if (!SB.isEmpty()) {
				response.setStatus(true);
				response.setMessage(SB.toString() + "these files are already exists");

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
	public response getUploadedFileDetails(@RequestBody long userAccountId) {
		response response = new response();
		List<clientDocuments> fub = null;
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

	/*---------------------------get uploaded file details-------------------------------*/
	@Override
	public response deleteFileDetails(clientDocuments clientDocuments) {
		response response = new response();
		try {
			clientDocumentsR.deleteById(clientDocuments.getFileId());
			File file = new File(clientDocuments.getFilePath() + "\\" + clientDocuments.getFileName());
			file.delete();
			response.setStatus(true);
			response.setMessage("Deleted Successfully");
			return response;
		} catch (Exception e) {
			response.setStatus(false);
			return response;
		}

	}

	/****************************
	 * servlet api calling method
	 *****************************/
	@Override
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, String clientname,
			String fileName, Boolean preview) throws IOException {

		File file = new File(apiFilePath + "/" + clientname + "/" + fileName);
		if (file.exists()) {

			// get the mimetype
			String mimeType = URLConnection.guessContentTypeFromName(file.getName());
			if (mimeType == null) {
				// unknown mimetype so set the mimetype to application/octet-stream
				mimeType = "application/octet-stream";
			}

			response.setContentType(mimeType);

			/**
			 * In a regular HTTP response, the Content-Disposition response header is a
			 * header indicating if the content is expected to be displayed inline in the
			 * browser, that is, as a Web page or as part of a Web page, or as an
			 * attachment, that is downloaded and saved locally.
			 * 
			 */

			/**
			 * Here we have mentioned it to show inline
			 */
			if (preview)
				response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
			else
				// Here we have mentioned it to show as attachment
				response.setHeader("Content-Disposition",
						String.format("attachment; filename=\"" + file.getName() + "\""));

			response.setContentLength((int) file.length());

			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

			FileCopyUtils.copy(inputStream, response.getOutputStream());

		}
	}
}
