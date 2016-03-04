package ds.vkplus.ui.activity

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.trello.rxlifecycle.components.support.RxFragment
import ds.vkplus.eventbus.EventBus
import ds.vkplus.network.RestService
import ds.vkplus.ui.view.RefreshButton
import ds.vkplus.utils.actionBar

public abstract class BaseFragment : RxFragment() {
	
	protected var rest: RestService = RestService.instance
	lateinit protected var refreshButton: RefreshButton
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		refreshButton = RefreshButton(activity)
		actionBar.setDisplayShowCustomEnabled(true)
		actionBar.customView = refreshButton
		//ab.setDisplayShowHomeEnabled(true);
		//ab.setDisplayUseLogoEnabled(true);


		refreshButton.setOnClickListener { v -> onRefresh() }
		//setRetainInstance(true);
	}
	
	
	protected abstract fun onRefresh()
	
	
	protected fun toggleProgress(enable: Boolean) {
		//getActivity().setProgressBarIndeterminateVisibility(enable);
		refreshButton.toggleProgress(enable)
		if (enable) {
			refreshButton.notificationsCount = 0
			//refreshButton.toggleVisibility(true);
		}
	}
	
	
	override fun onResume() {
		super.onResume()
		EventBus.register(this)
	}
	
	
	override fun onPause() {
		super.onPause()
		EventBus.unregister(this)
	}
	
	
	protected abstract fun onLoggedIn(token: String)
}
