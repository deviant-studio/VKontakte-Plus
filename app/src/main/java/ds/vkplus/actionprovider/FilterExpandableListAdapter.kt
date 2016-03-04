package ds.vkplus.actionprovider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView
import ds.vkplus.R
import ds.vkplus.db.DBHelper
import ds.vkplus.model.Filter
import ds.vkplus.model.FiltersDao
import ds.vkplus.ui.view.ThreeStateButton
import ds.vkplus.utils.L
import java.sql.SQLException

//custom fake expandable adapter
class FilterExpandableListAdapter(private val filters: List<Filter>) : BaseExpandableListAdapter() {

	lateinit var onFilterUpdateListener: () -> Unit

	private val dao: FiltersDao

	init {
		dao = DBHelper.instance.filtersDao
	}

	private val listener: (View) -> Unit = {
		try {
			val check = it as ThreeStateButton
			val i = check.tag as Filter
			dao.toggleState(i)
			notifyDataSetChanged()
			onFilterUpdateListener()
		} catch (e: SQLException) {
			e.printStackTrace()
		}
	}



	override fun getGroupCount(): Int = filters.size

	override fun getChildrenCount(groupPosition: Int): Int = filters.get(groupPosition).getSubItems().size

	override fun getGroup(groupPosition: Int): Any = filters.get(groupPosition)

	override fun getChild(groupPosition: Int, childPosition: Int): Any = filters.get(groupPosition).getSubItems().get(childPosition)

	override fun getGroupId(groupPosition: Int): Long = filters.get(groupPosition).id.toLong()

	override fun getChildId(groupPosition: Int, childPosition: Int): Long = filters.get(groupPosition).getSubItems().get(childPosition).id.toLong()

	override fun hasStableIds(): Boolean = true


	override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
		var v = convertView
		if (v == null) {
			v = LayoutInflater.from(parent.context).inflate(R.layout.expandable_list_group_row, parent, false)
		}

		val item = getGroup(groupPosition) as Filter
		val button = v?.findViewById(R.id.threeStateCheck) as ThreeStateButton
		if (item.mode == Filter.MODE_CHECK) {
			button.state = item.state.ordinal
			button.visibility = View.VISIBLE
			button.tag = item
			button.onStateChangedListener = listener
		} else
			button.visibility = View.INVISIBLE

		(v?.findViewById(R.id.text) as TextView).text = item.title

		return v!!
	}


	override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
		var v = convertView
		L.v("getChildView")
		if (v == null) {
			v = LayoutInflater.from(parent.context).inflate(R.layout.expandable_list_row, parent, false)
		}

		val item = getChild(groupPosition, childPosition) as Filter
		val check = (v!!.findViewById(R.id.check) as CheckBox)
		check.isChecked = item.state == Filter.State.CHECKED
		check.text = item.title

		return v
	}


	override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true


}