package config.commonConfig;

import config.DTO.Response;

/**
 * Utility class for building standardized responses
 */
public class ResponseBuilder {

	private ResponseBuilder() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Build a success response
	 */
	public static Response success(Object data, String message) {
		Response response = new Response();
		response.setStatus(true);
		response.setMessage(message != null ? message : AppConstants.SUCCESS_OPERATION_COMPLETED);
		response.setData(data);
		return response;
	}

	/**
	 * Build a success response with default message
	 */
	public static Response success(Object data) {
		return success(data, AppConstants.SUCCESS_OPERATION_COMPLETED);
	}

	/**
	 * Build a success response with only message
	 */
	public static Response success(String message) {
		return success(null, message);
	}

	/**
	 * Build an error response
	 */
	public static Response error(String message) {
		Response response = new Response();
		response.setStatus(false);
		response.setMessage(message != null ? message : AppConstants.ERROR_SOMETHING_WENT_WRONG);
		return response;
	}

	/**
	 * Build an error response with default message
	 */
	public static Response error() {
		return error(AppConstants.ERROR_SOMETHING_WENT_WRONG);
	}

	/**
	 * Build an error response with exception details
	 */
	public static Response error(Exception ex) {
		Response response = new Response();
		response.setStatus(false);
		response.setMessage(ex.getMessage() != null ? ex.getMessage() : AppConstants.ERROR_SOMETHING_WENT_WRONG);
		return response;
	}
}
