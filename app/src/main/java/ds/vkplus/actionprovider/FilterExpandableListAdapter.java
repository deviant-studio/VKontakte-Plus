package ds.vkplus.actionprovider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import ds.vkplus.R;
import ds.vkplus.db.DBHelper;
import ds.vkplus.model.Filter;
import ds.vkplus.model.FiltersDao;
import ds.vkplus.ui.view.ThreeStateButton;
import ds.vkplus.utils.L;

import java.sql.SQLException;
import java.util.List;

//custom fake expandable adapter
public class FilterExpandableListAdapter extends BaseExpandableListAdapter implements OnClickListener {

	public static final int VIEW_TYPE_HEADER = 0;
	public static final int VIEW_TYPE_ITEM = 1;

	private Context mCtx;
	private List<Filter> filters;
	private OnFilterUpdateListener mListener;
	private FiltersDao dao;


	public FilterExpandableListAdapter(Context context, List<Filter> filters) {
		if (filters == null) {
			L.w("filters are null");
			return;
		}
		mCtx = context;
		this.filters = filters;
		dao = DBHelper.instance().filtersDao;

		// expand filters with enabled items

	}


	// this is for threestate check
	@Override
	public void onClick(View v) {
		try {
			final ThreeStateButton check = (ThreeStateButton) v;
			final Filter item = (Filter) check.getTag();
			int state = check.getState();
			//item.setState(state);
			dao.toggleState(item);
			notifyDataSetChanged();
			// onUpdate(position);

			if (mListener != null)
				mListener.onFilterUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	public void setOnFilterUpdateListener(OnFilterUpdateListener l) {
		mListener = l;
	}


	public OnFilterUpdateListener getOnFilterUpdateListener() {
		return mListener;
	}


	@Override
	public int getGroupCount() {
		return filters.size();
	}


	@Override
	public int getChildrenCount(final int groupPosition) {
		return filters.get(groupPosition).getSubItems().size();
	}


	@Override
	public Object getGroup(final int groupPosition) {
		return filters.get(groupPosition);
	}


	@Override
	public Object getChild(final int groupPosition, final int childPosition) {
		return filters.get(groupPosition).getSubItems().get(childPosition);
	}


	@Override
	public long getGroupId(final int groupPosition) {
		return filters.get(groupPosition).id;
	}


	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return filters.get(groupPosition).getSubItems().get(childPosition).id;
	}


	@Override
	public boolean hasStableIds() {
		return true;
	}


	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, View v, final ViewGroup parent) {
		L.v("getGroupView");
		if (v == null) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expandable_list_group_row, parent, false);
		}

		Filter item = (Filter) getGroup(groupPosition);
		ThreeStateButton button = ((ThreeStateButton) v.findViewById(R.id.threeStateCheck));
		if (item.mode == Filter.MODE_CHECK) {
			button.setState(item.state.ordinal());
			button.setVisibility(View.VISIBLE);
			button.setTag(item);
			button.setOnStateChangedListener(this);
		} else
			button.setVisibility(View.INVISIBLE);

		((TextView) v.findViewById(R.id.text)).setText(item.title);

		return v;
	}


	@Override
	public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, View v, final ViewGroup parent) {
		L.v("getChildView");
		if (v == null) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expandable_list_row, parent, false);
		}

		Filter item = (Filter) getChild(groupPosition, childPosition);
		CheckBox check = ((CheckBox) v.findViewById(R.id.check));
		check.setChecked(item.state == Filter.State.CHECKED);
		check.setText(item.title);

		return v;
	}


	@Override
	public boolean isChildSelectable(final int groupPosition, final int childPosition) {
		return true;
	}


	public interface OnFilterUpdateListener {

		public void onFilterUpdate();
	}

}