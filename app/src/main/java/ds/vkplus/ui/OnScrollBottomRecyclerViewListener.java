package ds.vkplus.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import ds.vkplus.callback.SimpleCallback;
import ds.vkplus.utils.L;

public class OnScrollBottomRecyclerViewListener implements RecyclerView.OnScrollListener {

	private SimpleCallback cb;
	private LinearLayoutManager lm;
	private RecyclerView rv;

	private int lastTotal = 0;


	public OnScrollBottomRecyclerViewListener(LinearLayoutManager lm, final SimpleCallback callback) {
		cb = callback;
		this.lm = lm;
	}


	@Override
	public void onScrollStateChanged(final int i) {
		int last = lm.findLastCompletelyVisibleItemPosition();
		if (last == -1)
			last = lm.findLastVisibleItemPosition();
		int total = lm.getItemCount();
		if (i == 0 && last == total - 1 && lastTotal != total) {
			L.v("onBottom");
			cb.onResult(true);
			lastTotal = total;
		}
	}


	@Override
	public void onScrolled(final int i, final int i2) { }
}
