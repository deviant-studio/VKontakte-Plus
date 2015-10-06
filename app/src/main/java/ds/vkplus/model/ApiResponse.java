package ds.vkplus.model;

import com.google.gson.Gson;
import ds.vkplus.exception.VKException;

final public class ApiResponse<T> {

	public T response;
	public Error error;


	@Override
	public String toString() {
		return new Gson().toJson(this);
	}


	public static ApiResponse generateError() {
		ApiResponse errorResponse = new ApiResponse();
		errorResponse.error = new Error(VKException.CODE_NETWORK, "Retrofit error");
		return errorResponse;
	}


	public static class Error {

		public int error_code;
		public String error_msg;


		public Error() {}


		public Error(final int error_code, final String error_msg) {
			this.error_code = error_code;
			this.error_msg = error_msg;
		}
	}
}
