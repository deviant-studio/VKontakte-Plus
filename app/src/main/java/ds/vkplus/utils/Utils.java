package ds.vkplus.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import ds.vkplus.App;

import java.util.List;

public class Utils {

	public static void post(final Runnable runnable) {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(runnable);
	}


	public static void postDelayed(final Runnable runnable, long delay) {
		Handler h = new Handler(Looper.getMainLooper());
		h.postDelayed(runnable, delay);
	}


	public static CharSequence setSpanForSubstring(CharSequence text, String substring, int color) {
		int start = text.toString().indexOf(substring);
		int end = start + substring.length();
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
		spannableStringBuilder.setSpan(new ForegroundColorSpan(color), start, end, 0);

		return spannableStringBuilder;
	}


	public static int dp(Context c, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
	}


	public static void toggleView(final View v, final boolean visible) {
		v.setVisibility(visible ? View.VISIBLE : View.GONE);
	}


	public static boolean isAppForeground() {
		Context context = App.instance();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null) {
			return false;
		}
		final String packageName = context.getPackageName();
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}


	public static boolean isMainThread() {
		return (Looper.myLooper() == Looper.getMainLooper());
	}


	public static void openURL(final String url) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		App.instance().startActivity(i);
	}


	private static long debugTimer;


	public static void startTimer() {
		debugTimer = System.currentTimeMillis();
	}


	public static void stopTimer(String message) {
		if (debugTimer != 0) {
			float time = (System.currentTimeMillis() - debugTimer) / 1000F;
			L.i("%s TIME=%s sec.", message != null ? message : "", time);
			debugTimer = 0;
		} else {
			L.w("call startTimer() first");
		}
	}


	public static boolean isWifi() {
		Context context = App.instance();
		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return networkInfo != null && networkInfo.isConnected();
	}
}
