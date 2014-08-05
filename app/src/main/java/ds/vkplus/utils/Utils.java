package ds.vkplus.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;

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
}
