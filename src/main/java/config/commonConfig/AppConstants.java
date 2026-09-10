package config.commonConfig;

/**
 * Centralized application constants for error messages, API paths, and configuration values
 */
public class AppConstants {

	// Error Messages
	public static final String ERROR_SOMETHING_WENT_WRONG = "Something went wrong";
	public static final String ERROR_INVALID_DATA = "Invalid data provided";
	public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
	public static final String ERROR_FILE_UPLOAD_FAILED = "File upload failed";
	public static final String ERROR_INVALID_PASSWORD = "Invalid password";
	public static final String ERROR_CURRENT_PASSWORD_WRONG = "Current password is incorrect";
	public static final String ERROR_USER_NOT_FOUND = "User not found";
	public static final String ERROR_CUSTOMER_NOT_FOUND = "Customer not found";

	// Success Messages
	public static final String SUCCESS_OPERATION_COMPLETED = "Operation completed successfully";
	public static final String SUCCESS_USER_CREATED = "User created successfully";
	public static final String SUCCESS_PROFILE_UPDATED = "Profile updated successfully";
	public static final String SUCCESS_PASSWORD_CHANGED = "Password changed successfully";
	public static final String SUCCESS_FILE_UPLOADED = "File uploaded successfully";

	// HTTP Status Messages
	public static final String STATUS_OK = "OK";
	public static final String STATUS_ERROR = "ERROR";
	public static final String STATUS_CREATED = "CREATED";

	// Month Names
	public static final String[] MONTHS = {
		"", "January", "February", "March", "April", "May", "June",
		"July", "August", "September", "October", "November", "December"
	};

	// API Endpoints
	public static final String API_BASE_PATH = "/api";
	public static final String API_AUTH = API_BASE_PATH + "/auth";
	public static final String API_CUSTOMER = API_BASE_PATH + "/customer";
	public static final String API_USER = API_BASE_PATH + "/user";
	public static final String API_PROFILE = API_BASE_PATH + "/profile";

	// Configuration Keys
	public static final String CONFIG_FILE_UPLOAD_PATH = "app.upload.path";
	public static final String CONFIG_MAX_FILE_SIZE = "app.upload.max-file-size";
	public static final String CONFIG_JWT_SECRET = "app.jwt.secret";
	public static final String CONFIG_JWT_EXPIRATION = "app.jwt.expiration";
	
	// Client Documents Configuration Keys
	public static final String CONFIG_CLIENT_DOCS_BASE_PATH = "app.client.documents.base-path";
	public static final String CONFIG_CLIENT_DOCS_API_PATH = "app.client.documents.api-path";
	public static final String CONFIG_CLIENT_DOCS_CLIENT_PATH = "app.client.documents.client-path";

	private AppConstants() {
		// Private constructor to prevent instantiation
	}
}
