package ds.vkplus.actionprovider


import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.view.ActionProvider
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.PopupWindow
import ds.vkplus.R
import ds.vkplus.db.DBHelper
import ds.vkplus.eventbus.EventBus
import ds.vkplus.eventbus.events.FilterEvent
import ds.vkplus.model.Filter
import ds.vkplus.utils.L
import ds.vkplus.utils.Utils

import java.sql.SQLException

public class FilterActionProvider(private val ctx: Context) : ActionProvider(ctx) {

    lateinit var filters: List<Filter>
    lateinit var anchor: View


    override fun onCreateActionView(): View {
        val layoutInflater = LayoutInflater.from(context)
        anchor = layoutInflater.inflate(R.layout.filter_action_provider, null)
        val button = anchor.findViewById(R.id.button) as ImageView
        button.setOnClickListener { v -> performAction() }
        return anchor
    }


    public fun init(filterType: Int) {
        filters = DBHelper.instance.filtersDao.fetchFilters(filterType)
    }


    private fun performAction() {
        val content = LayoutInflater.from(context).inflate(R.layout.popup_filters, null)
        val popup = PopupWindow(content)
        val a = FilterExpandableListAdapter(filters)

        a.onFilterUpdateListener = { postEvent(null) }

        val list = content.findViewById(R.id.expandable_list) as ExpandableListView
        list.setAdapter(a)
        list.setGroupIndicator(null)
        list.itemsCanFocus = true    // important
        list.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            try {
                L.v("child clicked")
                val sub = parent.expandableListAdapter.getChild(groupPosition, childPosition) as Filter
                DBHelper.instance.filtersDao.toggleState(sub)
                (parent.expandableListAdapter as FilterExpandableListAdapter).notifyDataSetChanged()
                val f = parent.expandableListAdapter.getChild(groupPosition, childPosition) as Filter
                postEvent(f)
                return@setOnChildClickListener true
            } catch (e: SQLException) {
                e.printStackTrace()
                return@setOnChildClickListener false
            }
        }

        for (i in filters.indices) {
            val filter = filters.get(i)
            if (filter.unfolded)
                list.expandGroup(i)
        }

        popup.width = Utils.dp(context, 280)
        popup.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popup.isFocusable = true
        popup.setBackgroundDrawable(ColorDrawable())
        popup.setOnDismissListener { L.v("dismiss filter list") }

        popup.showAsDropDown(anchor)

    }


    private fun postEvent(f: Filter?) {
        EventBus.post(FilterEvent(f))
    }


    override fun onPerformDefaultAction(): Boolean {
        anchor = (context as Activity).findViewById(android.R.id.home)    // bad!
        performAction()
        return super.onPerformDefaultAction()
    }
}