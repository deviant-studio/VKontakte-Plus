package ds.vkplus.ui

import android.widget.AbsListView

class OnScrollBottomListener(private val cb: (Boolean) -> Unit) : AbsListView.OnScrollListener {
	
	
	override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
		if (!view.isScrollbarFadingEnabled)
			view.isScrollbarFadingEnabled = true
		
	}
	
	
	override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
		
		val lastView = view.getChildAt(visibleItemCount - 1)
		if (view.lastVisiblePosition == totalItemCount - 1 && lastView != null && lastView.bottom <= view.height) {
			cb(true)
		}
	}
}
