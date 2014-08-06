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

	}
}
