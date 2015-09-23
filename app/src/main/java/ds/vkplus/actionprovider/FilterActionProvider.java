package ds.vkplus.actionprovider;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ActionProvider;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import ds.vkplus.R;
import ds.vkplus.db.DBHelper;
import ds.vkplus.eventbus.EventBus;
import ds.vkplus.eventbus.events.FilterEvent;
import ds.vkplus.model.Filter;
import ds.vkplus.utils.L;
import ds.vkplus.utils.Utils;

import java.sql.SQLException;
import java.util.List;

public class FilterActionProvider extends ActionProvider {

	List<Filter> filters;
	View anchor;
	private Context ctx;
	MenuItem forItem;


	public FilterActionProvider(final Context context) {
		super(context);
		ctx = context;
	}


	@Override
	public View onCreateActionView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		anchor = layoutInflater.inflate(R.layout.filter_action_provider, null);
		ImageView button = (ImageView) anchor.findViewById(R.id.button);
		button.setOnClickListener(v -> performAction());
		return anchor;
	}


	public void init(int filterType) {
		filters = DBHelper.instance().filtersDao.fetchFilters(filterType);
	}


	private void performAction() {
		L.v("click!");

		ListPopupWindow popupList = new ListPopupWindow(getContext());
		//popupList.set
		PopupWindow popup = new PopupWindow(getContext());
		FilterExpandableListAdapter a = new FilterExpandableListAdapter(getContext(), filters);

		a.setOnFilterUpdateListener(() -> postEvent(null));

		ExpandableListView list = new ExpandableListView(getContext());
		list.setAdapter(a);
		list.setBackgroundColor(Color.WHITE);
		list.setGroupIndicator(null);
		list.setItemsCanFocus(true);    // important
		list.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
			try {
				L.v("child clicked");
				Filter sub = (Filter) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
				DBHelper.instance().filtersDao.toggleState(sub);
				//sub.toggleState();
				((FilterExpandableListAdapter) parent.getExpandableListAdapter()).notifyDataSetChanged();
				Filter f = (Filter) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
				postEvent(f);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		});

		for (int i = 0; i < filters.size(); i++) {
			final Filter filter = filters.get(i);
			if (filter.unfolded)
				list.expandGroup(i);
		}

		popup.setContentView(list);
		popup.setWidth(Utils.dp(getContext(), 280));
		popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		popup.setFocusable(true);

		popup.setOnDismissListener(() -> L.v("dismiss filter list"));

		popup.showAsDropDown(anchor);

	}


	private void postEvent(Filter f) {
		EventBus.post(new FilterEvent(f));
	}


	/*private Context getContext() {
		return ctx;
	}*/


	@Override
	public boolean onPerformDefaultAction() {
		anchor = ((Activity) getContext()).findViewById(android.R.id.home);    // bad!
		performAction();
		return super.onPerformDefaultAction();
	}
}