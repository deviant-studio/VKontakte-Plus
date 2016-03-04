package ds.vkplus.utils

import android.content.Context
import android.widget.Toast

/**
 * Used for quick debugging toasts
 */
object T {

	fun show(c: Context, message: String) {
		if (L.DEBUG) {
			Toast.makeText(c, message, 0).show()
		}
	}


	fun show(c: Context, message: String, vararg params: Any) {
		if (L.DEBUG) {
			if (params.size == 0)
				return

			show(c, java.lang.String.format(message, *params))
		}
	}
}
