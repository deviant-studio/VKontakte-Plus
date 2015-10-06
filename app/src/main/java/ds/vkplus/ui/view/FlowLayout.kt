package ds.vkplus.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import ds.vkplus.utils.L

import java.util.Vector

class FlowLayout(context: Context, attributeSet: AttributeSet) : ViewGroup(context, attributeSet) {
	
	private val lineHeights = Vector<Int>()
	var pwidth: Int
	
	
	init {
		
		pwidth = scale(context, 20f)
		
		//setBackgroundColor(Color.RED);
	}
	
	
	override fun checkLayoutParams(layoutParams: ViewGroup.LayoutParams): Boolean {
		return layoutParams is LayoutParams
	}
	
	
	override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
		return LayoutParams(scale(context, 2.0f), scale(context, 2.0f))
	}
	
	
	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
		val count = childCount
		val w = r - l
		var pl = paddingLeft
		var pt = paddingTop
		//L.v("pl=%s pt=%s", pl, pt)
		var index = 0
		var breakAfter = false
		var bl3 = false
		var n10 = 0
		for (i in 0..count - 1) {
			val view = this.getChildAt(i)
			if (view.visibility == View.GONE) continue
			val layoutParams = view.layoutParams as LayoutParams
			val wMeasured = view.measuredWidth
			val hMeasured = view.measuredHeight
			if (!layoutParams.floating && bl3) {
				pt = n10
			}
			if (breakAfter || pl + wMeasured > w + this.pwidth) {
				pl = paddingLeft
				pt += lineHeights.elementAt(index)
				++index
			}
			view.layout(pl, pt, pl + wMeasured, pt + hMeasured)
			if (layoutParams.floating) {
				if (!bl3) {
					n10 = pt
					bl3 = true
				}
				pt += hMeasured + layoutParams.vertical_spacing
			} else {
				pl += wMeasured + layoutParams.horizontal_spacing
				bl3 = false
			}
			breakAfter = layoutParams.breakAfter
		}
	}
	
	
	/*
	 * Enabled aggressive block sorting
	 */
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val width = View.MeasureSpec.getSize(widthMeasureSpec) - this.paddingLeft - this.paddingRight
		var height = View.MeasureSpec.getSize(heightMeasureSpec) - this.paddingTop - this.paddingBottom
		val childsCount = childCount
		var h1 = 0
		var paddingLeft = paddingLeft
		var paddingTop = paddingTop
		var wAndPadding = 0
		val hSpec = if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST)
			View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
		else
			View.MeasureSpec.makeMeasureSpec(0, 0)
		this.lineHeights.clear()
		var breakAfter = false
		var h2 = 0
		for (i in 0..childsCount - 1) {
			val view: View? = getChildAt(i)
			if (view == null || view.visibility == View.GONE) continue
			val layoutParams = view.layoutParams as LayoutParams
			val wSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST)
			view.measure(wSpec, hSpec)
			val wMeasured = view.measuredWidth
			val hMeasured = view.measuredHeight
			if (breakAfter || paddingLeft + wMeasured > width + this.pwidth) {
				paddingLeft = this.paddingLeft
				paddingTop += Math.max(h1, h2)
				this.lineHeights.add(Math.max(h1, h2))
				h1 = 0
				h2 = 0
			}
			h1 = Math.max(h1, hMeasured + layoutParams.vertical_spacing)
			if (layoutParams.floating) {
				paddingTop += hMeasured + layoutParams.vertical_spacing
				h2 += hMeasured + layoutParams.vertical_spacing
				wAndPadding = Math.max(wAndPadding, paddingLeft + wMeasured)
			} else {
				paddingLeft += wMeasured + layoutParams.horizontal_spacing
				h2 = 0
			}
			breakAfter = layoutParams.breakAfter
			wAndPadding = Math.max(wAndPadding, paddingLeft - layoutParams.horizontal_spacing)
		}
		if (View.MeasureSpec.getMode(heightMeasureSpec) == 0) {
			height = Math.max(h1, h2)
			for (lineHeight in this.lineHeights) {
				height += lineHeight!!
			}
		} else if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST && paddingTop + h1 < height) {
			height = h1
			for (lineHeight in this.lineHeights) {
				height += lineHeight!!
			}
		}
		if (View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY) {
			this.setMeasuredDimension(width, height)
			return
		}
		this.setMeasuredDimension(wAndPadding, height)
	}
	

	class LayoutParams(h: Int, v: Int) : ViewGroup.LayoutParams(0, 0) {
		
		var breakAfter = false
		var floating = false
		var horizontal_spacing = 0
		var vertical_spacing = 0
		

		init {
			horizontal_spacing = h
			vertical_spacing = v
		}
	}
	
	companion object {

		fun scale(ctx: Context, paramFloat: Float): Int {
			val displayDensity = ctx.resources.displayMetrics.density
			return Math.round(paramFloat * displayDensity)
		}
	}
}