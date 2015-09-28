package ds.vkplus.ui

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import ds.vkplus.callback.IntCallback
import ds.vkplus.utils.L

public class OnScrollBottomRecyclerViewListener(private val lm: LinearLayoutManager, private val callback: (Int)->Unit) : RecyclerView.OnScrollListener() {
	
	public var prevLast: Int = 0
	public val speed: Int = 0
	//private int scrollState;
	private var last: Int = 0
	private var total: Int = 0
	
	
	private fun checkResult() {
		if (last == total - 1 && prevLast != last) {
			L.v("onBottom")
			callback(last)
		}
	}
	
	

	override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
		//scrollState = newState;
		L.v("state=" + newState)
		if (newState == RecyclerView.SCROLL_STATE_IDLE) {

			if (total < 3) {
				L.v("onBottom")
				callback(last)
			}
		}
	}
	
	
	override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
			last = lm.findLastCompletelyVisibleItemPosition()
			if (last == -1)
				last = lm.findLastVisibleItemPosition()
			total = lm.itemCount
			checkResult()
			prevLast = last
	}
	
}
