package ds.vkplus.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import ds.vkplus.callback.IntCallback;
import ds.vkplus.utils.L;

public class OnScrollBottomRecyclerViewListener extends RecyclerView.OnScrollListener {

	private IntCallback cb;
	private LinearLayoutManager lm;

	public int prevLast = 0;
	private int speed = 0;
	//private int scrollState;
	private int last;
	private int total;


	public OnScrollBottomRecyclerViewListener(LinearLayoutManager lm, final IntCallback callback) {
		cb = callback;
		this.lm = lm;
	}


	private void checkResult() {
		if (last == total - 1 && prevLast != last) {
			L.v("onBottom");
			cb.onResult(last);
		}
	}


	/*public boolean isScrolling() {
		L.v("scroll state="+scrollState);
		return scrollState != SCROLL_STATE_IDLE;
	}*/


	@Override
	public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
		//scrollState = newState;
		L.v("state=" + newState);
		if (newState == RecyclerView.SCROLL_STATE_IDLE) {
			//recyclerView.getAdapter().notifyItemRangeChanged(lm.findFirstVisibleItemPosition(), lm.findLastVisibleItemPosition());

			if (total < 3) {
				L.v("onBottom");
				cb.onResult(last);
			} /*else {
				((NewsFragment.NewsRecyclerAdapter) recyclerView.getAdapter()).resetInitTime();
				recyclerView.getAdapter().notifyItemRangeChanged(lm.findFirstVisibleItemPosition(), lm.findLastVisibleItemPosition());
			}*/
		}
	}


	@Override
	public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
		if (lm != null) {
			last = lm.findLastCompletelyVisibleItemPosition();
			if (last == -1)
				last = lm.findLastVisibleItemPosition();
			total = lm.getItemCount();
			//L.v("total=%s prevLast=%s last=%s sate=%s", total, prevLast, last, scrollState);
			checkResult();
			prevLast = last;
			this.speed = speed;
		}
	}


	public int getSpeed() {
		return speed;
	}

}
