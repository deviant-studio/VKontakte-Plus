package ds.vkplus.utils

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import ds.vkplus.App
import ds.vkplus.model.News

object Utils {
	

	fun setSpanForSubstring(text: CharSequence, substring: String, color: Int): CharSequence {
		val start = text.toString().indexOf(substring)
		val end = start + substring.length()
		val spannableStringBuilder = SpannableStringBuilder(text)
		spannableStringBuilder.setSpan(ForegroundColorSpan(color), start, end, 0)
		
		return spannableStringBuilder
	}
	
	
	fun dp(c: Context, dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), c.resources.displayMetrics).toInt()
	
	
	fun toggleView(v: View, visible: Boolean) {
		v.visibility = if (visible) View.VISIBLE else View.GONE
	}
	
	
	fun isAppForeground(): Boolean {
		val context = App.instance
		val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		val appProcesses = activityManager.runningAppProcesses ?: return false
		val packageName = context.packageName
		for (appProcess in appProcesses) {
			if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
				return true
			}
		}
		return false
	}
	
	
	fun isMainThread(): Boolean = (Looper.myLooper() == Looper.getMainLooper())
	
	
	fun openURL(url: String) {
		val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		App.instance.startActivity(i)
	}
	
	
	private var debugTimer: Long = 0

	fun startTimer() {
		debugTimer = System.currentTimeMillis()
	}
	
	
	fun stopTimer(message: String?) {
		if (debugTimer != 0L) {
			val time = (System.currentTimeMillis() - debugTimer) / 1000f
			L.i("%s TIME=%s sec.", message ?: "", time)
			debugTimer = 0
		} else {
			L.w("call startTimer() first")
		}
	}
	
	
	fun isWifi(): Boolean {
		val context = App.instance
		val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val networkInfo = mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
		return networkInfo != null && networkInfo.isConnected
	}
	
	
	fun shareText(ctx: Context, text: String) {
		val sendIntent = Intent()
		sendIntent.setAction(Intent.ACTION_SEND)
		sendIntent.putExtra(Intent.EXTRA_TEXT, text)
		sendIntent.setType("text/plain")
		ctx.startActivity(sendIntent)
	}
	
	
	fun getPostUrl(post: News): String = "https://vk.com/wall" + post.source_id + "_" + post.post_id
	
	fun copyNoteUrlToClipboard(ctx: Context, text: String) {
		val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val clip = ClipData.newPlainText("note", text)
		clipboard.primaryClip = clip
	}
}

fun View.toggle(visible: Boolean) = this.setVisibility(if (visible) View.VISIBLE else View.GONE)

fun Context.toDips(pixels: Float) = pixels / this.resources.displayMetrics.density

fun post(runnable: () -> Unit) {
	val h = Handler(Looper.getMainLooper())
	h.post(runnable)
}

fun postDelayed(delay: Long, runnable: () -> Unit) {
	val h = Handler(Looper.getMainLooper())
	h.postDelayed(runnable, delay)
}

fun format(msg: String, vararg args: Any) = java.lang.String.format(msg, *args)