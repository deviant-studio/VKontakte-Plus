package ds.vkplus.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import ds.vkplus.callback.IntCallback;
import ds.vkplus.utils.L;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

public class OnScrollBottomRecyclerViewListener implements RecyclerView.OnScrollListener {

	private IntCallback cb;
	private LinearLayoutManager lm;

	public int prevLast = 0;
	private int speed = 0;
	private int scrollState;
	private int last;
	private int total;


	public OnScrollBottomRecyclerViewListener(LinearLayoutManager lm, final IntCallback callback) {
		cb = callback;
		this.lm = lm;
	}


	@Override
	public void onScrollStateChanged(final int i) {
		scrollState = i;
		//L.v("state=" + i);
		if ((total < 3 && scrollState == SCROLL_STATE_IDLE)) {
			L.v("onBottom");
			cb.onResult(last);
		}
	}


	private void checkResult() {
		if (last == total - 1 && prevLast != last) {
			L.v("onBottom");
			cb.onResult(last);
		}
	}


	@Override
	public void onScrolled(final int i, final int speed) {
		last = lm.findLastCompletelyVisibleItemPosition();
		if (last == -1)
			last = lm.findLastVisibleItemPosition();
		total = lm.getItemCount();
		//L.v("total=%s prevLast=%s last=%s sate=%s", total, prevLast, last, scrollState);
		checkResult();
		prevLast = last;
		this.speed = speed;
	}


	public int getSpeed() {
		return speed;
	}

}
