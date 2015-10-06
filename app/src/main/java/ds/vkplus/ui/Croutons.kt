package ds.vkplus.ui

import android.app.Activity
import de.keyboardsurfer.android.widget.crouton.Configuration
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback
import de.keyboardsurfer.android.widget.crouton.Style
import ds.vkplus.App
import ds.vkplus.R

public object Croutons {
	
	public val STYLE_ERROR: Int = 0
	public val STYLE_SUCCESS: Int = 1
	public val STYLE_INFO: Int = 3
	public val STYLE_WARN: Int = 4
	
	public val DURATION_SHORT: Int = 1000
	public val DURATION_LONG: Int = 2000
	
	private val DEFAULT_DURATION = DURATION_LONG
	
	
	public class Builder {
		
		var mess: String? = null
		var style: Int = 0
		var duration = DEFAULT_DURATION
		var callback: LifecycleCallback? = null
		lateinit var a: Activity
		
		
		constructor() {
		}
		
		
		private constructor(a: Activity) {
			this.a = a
		}
		
		
		public fun duration(d: Int): Builder {
			duration = d
			return this
		}
		
		
		public fun style(s: Int): Builder {
			style = s
			return this
		}
		
		
		public fun message(id: Int): Builder {
			mess = App.instance.getString(id)
			return this
		}
		
		
		public fun message(m: String): Builder {
			mess = m
			return this
		}
		
		public fun callback(cb: LifecycleCallback): Builder {
			callback = cb
			return this
		}
		
		
		public fun show(a: Activity) {
			//Crouton.makeText(a, message, getStyle());
			
			val appearance = R.style.crouton_appearance
			val croutonStyle = Style.Builder().setBackgroundColor(getColor(style)).setTextAppearance(appearance).build()
			val crouton: Crouton
			val conf = Configuration.Builder().setDuration(duration).build()
			crouton = Crouton.makeText(a, getMessage(), croutonStyle).setConfiguration(conf)
			if (callback != null)
				crouton.setLifecycleCallback(callback)
			crouton.show()
		}
		
		private fun getMessage(): String {
			if (mess == null)
				mess = "[no message]"
			
			return mess!!
		}
		
		
		private fun getColor(style: Int): Int {
			when (style) {
				STYLE_ERROR -> return android.R.color.holo_red_light
				STYLE_SUCCESS -> return android.R.color.holo_green_light
				STYLE_INFO -> return android.R.color.holo_blue_light
				STYLE_WARN -> return android.R.color.holo_orange_light
				else -> return R.color.gray1
			}
		}
	}
	
	
	public fun prepare(): Builder {
		return Builder()
	}
	
	
}
