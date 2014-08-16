package ds.vkplus.model;

import com.google.gson.Gson;

final public class ApiResponse<T> {

	public T response;
	public Error error;


	@Override
	public String toString() {
		return new Gson().toJson(this);
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
