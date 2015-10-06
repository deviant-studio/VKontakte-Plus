package ds.vkplus.ui.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class HackyViewPager : ViewPager {
	
	private var isLocked: Boolean = false
	
	
	constructor(context: Context) : super(context) {
		isLocked = false
	}
	
	
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		isLocked = false
	}
	
	
	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		if (!isLocked) {
			try {
				return super.onInterceptTouchEvent(ev)
			} catch (e: IllegalArgumentException) {
				e.printStackTrace()
				return false
			}
			
		}
		return false
	}
	
	
	override fun onTouchEvent(event: MotionEvent): Boolean {
		return !isLocked && super.onTouchEvent(event)
	}
	
	
	fun toggleLock() {
		isLocked = !isLocked
	}
	
	
	fun setLocked(isLocked: Boolean) {
		this.isLocked = isLocked
	}
	
	
	fun isLocked(): Boolean {
		return isLocked
	}
	
}