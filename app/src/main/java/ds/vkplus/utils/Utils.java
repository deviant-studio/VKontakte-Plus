package ds.vkplus.utils;

import android.os.Handler;
import android.os.Looper;

public class Utils {
	public static void post(final Runnable runnable) {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(runnable);
	}


	public static void postDelayed(final Runnable runnable, long delay) {
		Handler h = new Handler(Looper.getMainLooper());
		h.postDelayed(runnable, delay);
	}
}
