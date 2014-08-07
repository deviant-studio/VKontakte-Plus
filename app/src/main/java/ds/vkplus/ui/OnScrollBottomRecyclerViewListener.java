package ds.vkplus.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import ds.vkplus.callback.IntCallback;
import ds.vkplus.utils.L;

public class OnScrollBottomRecyclerViewListener implements RecyclerView.OnScrollListener {

	private IntCallback cb;
	private LinearLayoutManager lm;
	//private RecyclerView rv;

	private int lastTotal = 0;


	public OnScrollBottomRecyclerViewListener(LinearLayoutManager lm, final IntCallback callback) {
		cb = callback;
		this.lm = lm;
	}


	@Override
	public void onScrollStateChanged(final int i) {

	}


	@Override
	public void onScrolled(final int i, final int i2) {
		int last = lm.findLastCompletelyVisibleItemPosition();
		if (last == -1)
			last = lm.findLastVisibleItemPosition();
		int total = lm.getItemCount();
		//L.v("total=%s lastTotal=%s last=%s",total,lastTotal,last);
		if (/*i == 0 &&*/ last == total - 1 && lastTotal != total) {
			L.v("onBottom");
			cb.onResult(last);
			lastTotal = total;
		}

	}
}
