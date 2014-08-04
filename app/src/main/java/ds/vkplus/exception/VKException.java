package ds.vkplus.exception;

import ds.vkplus.network.model.ApiResponse;

public class VKException extends RuntimeException {

	public static final int CODE_UNKNOWN = -1;
	public static final int CODE_TOKEN_OBSOLETED = 5;

	public int code;
	public String message;


	public VKException(ApiResponse.Error e) {
		code = e.error_code;
		message = e.error_msg;
	}


	public VKException() {
		code = CODE_UNKNOWN;
		message = "Unknown Error";

	}
}
