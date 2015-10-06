package ds.vkplus.ui.view

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import ds.vkplus.R
import ds.vkplus.utils.L

class RefreshButton(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {
	
	private lateinit var button: ImageView
	private lateinit var textLabel: TextView
	private lateinit var root: ViewGroup
	var notificationsCount: Int = 0
		set(count) {
			$notificationsCount = count
			textLabel.text = count.toString()
			textLabel.visibility = if (count > 0) View.VISIBLE else View.GONE
			
			if (count != 0)
				toggleVisibility(true)
		}
	
	private var rotating: Boolean = false
	
	
	init {
		root = LayoutInflater.from(context).inflate(R.layout.refresh_button, this, true) as ViewGroup
		val p = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		
		layoutParams = p
		textLabel = root.findViewById(R.id.text_label) as TextView
		button = root.findViewById(R.id.button) as ImageView
		
		postDelayed({ this.setupTransitions() }, 1000)   // god bless this line!
		
	}
	
	
	private fun setupTransitions() {
		val lt = LayoutTransition()
		val disappear = AnimatorSet()
		val appear = AnimatorSet()
		disappear.playTogether(ObjectAnimator.ofFloat(null, View.SCALE_X, 1f, 0f), ObjectAnimator.ofFloat(null, View.SCALE_Y, 1f, 0f))
		appear.playTogether(ObjectAnimator.ofFloat(null, View.SCALE_X, 0f, 1f), ObjectAnimator.ofFloat(null, View.SCALE_Y, 0f, 1f))
		disappear.interpolator = DecelerateInterpolator()
		appear.interpolator = OvershootInterpolator(3f)
		lt.setAnimator(LayoutTransition.DISAPPEARING, disappear)
		lt.setAnimator(LayoutTransition.APPEARING, appear)
		lt.setStartDelay(LayoutTransition.APPEARING, 0)
		lt.setDuration(250)
		(root.findViewById(R.id.root) as ViewGroup).layoutTransition = lt
	}
	
	
	override fun setOnClickListener(l: View.OnClickListener) {
		button.setOnClickListener {
			l.onClick(this@RefreshButton)
		}
	}
	
	
	fun toggleProgress(enable: Boolean) {
		L.v("progress " + enable)
		if (enable == rotating && enable)
			return
		
		rotating = enable
		if (enable) {
			button.startAnimation(AnimationUtils.loadAnimation(context, R.anim.progress_rotation))
			toggleVisibility(true)
		} else {
			button.clearAnimation()
			toggleVisibility(false)
		}
		
	}
	
	
	fun isProgressEnabled(): Boolean = rotating
	
	
	private fun toggleVisibility(enabled: Boolean) {
	}
}
