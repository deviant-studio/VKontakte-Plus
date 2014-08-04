package ds.vkplus.ui;

import android.view.View;
import android.widget.AbsListView;
import ds.vkplus.callback.SimpleCallback;

public class OnScrollBottomListener implements AbsListView.OnScrollListener {

	private SimpleCallback cb;


	public OnScrollBottomListener(final SimpleCallback callback) {
		cb = callback;
	}


	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		if (!view.isScrollbarFadingEnabled())
			view.setScrollbarFadingEnabled(true);

	}


	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {

		View lastView = view.getChildAt(visibleItemCount - 1);
		if (view.getLastVisiblePosition() == totalItemCount - 1 && lastView != null && lastView.getBottom() <= view.getHeight()) {
			cb.onResult(true);
		}
	}
}
