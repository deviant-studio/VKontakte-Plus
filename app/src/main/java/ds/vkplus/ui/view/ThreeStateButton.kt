/**

 */
package ds.vkplus.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import ds.vkplus.R

public class ThreeStateButton(context: Context, attrs: AttributeSet? = null) : ImageView(context, attrs) {
	
	var state: Int = 0
		set(s) {
			$state = s
			redraw()
		}
	
	var onStateChangedListener: ((View) -> Unit)? = null

	init {
		state = ThreeStateButton.STATE_HALF
		setOnClickListener {
			state = if (state == STATE_CHECKED) STATE_UNCHECKED else STATE_CHECKED
			redraw()
			onStateChangedListener?.invoke(this)
		}

	}
	

	private fun redraw() {
		when (state) {
			STATE_HALF -> setBackgroundResource(R.drawable.threestate_half)
			STATE_CHECKED -> setBackgroundResource(R.drawable.threestate_checked)
			STATE_UNCHECKED -> setBackgroundResource(R.drawable.threestate_unchecked)
		}
	}
	
	companion object {
		
		public val STATE_CHECKED: Int = 0
		public val STATE_HALF: Int = 1
		public val STATE_UNCHECKED: Int = 2
	}
	
}