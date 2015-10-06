package ds.vkplus.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import ds.vkplus.R
import ds.vkplus.utils.Utils

class FixedSizeImageView(context: Context) : ImageView(context) {
	
	var displayH: Int = 0
	var displayW: Int = 0
	var placeholder: Drawable
	private val playIcon: Drawable
	private var isVideo: Boolean = false
	
	
	init {
		placeholder = ColorDrawable(context.resources.getColor(R.color.gray2))
		playIcon = context.resources.getDrawable(R.drawable.video_play)
		
		//setBackgroundColor(Color.GREEN);
	}
	
	
	fun toggleVideoIcon(enable: Boolean) {
		isVideo = enable
		invalidate()
	}
	
	
	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		
		if (isVideo) {
			val w2 = displayW / 2
			val h2 = displayH / 2
			var size = Math.min(displayW / 4, displayH / 4)
			size = Math.min(Utils.dp(context, 48), size)
			playIcon.setBounds(w2 - size / 2, h2 - size / 2, w2 + size / 2, h2 + size / 2)
			playIcon.draw(canvas)
		}
	}
	
	
	override fun onMeasure(n: Int, n2: Int) {
		this.setMeasuredDimension(this.displayW, this.displayH)
	}
	
	
}