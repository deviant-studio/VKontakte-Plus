package ds.vkplus.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import ds.vkplus.R
import ds.vkplus.model.ApiResponse
import ds.vkplus.network.RestService
import ds.vkplus.utils.L
import rx.functions.Action0
import rx.functions.Action1


public class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
		supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val ab = supportActionBar
		ab.setIcon(R.drawable.logo)

		if (savedInstanceState == null) {
			showFragment()
		}
		
		RestService.instance.getGroups().subscribe({ L.v("groups: fetched successfully") }, { it.printStackTrace() })
	}
	
	public fun getActionBarView(): View {
		val window = window
		val v = window.decorView
		val resId = resources.getIdentifier("action_bar_container", "id", "android")
		return v.findViewById(resId)
	}
	
	
	private fun showFragment() {
		supportFragmentManager.beginTransaction().replace(R.id.container, getFragment()).commit()
	}
	
	
	private fun getFragment(): Fragment {
		return NewsFragment()
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main, menu)
		return super.onCreateOptionsMenu(menu)
	}
	
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.debug_rest -> RestService.instance.dummyRequestWithError(ApiResponse.Error(5, "fake error!"))
				.subscribe({
					L.v(it)
				}, {
					L.e("last mile error")
					it.printStackTrace()
				}, {
					L.v("completed")
				})
			
			R.id.debug_layout -> startActivity(Intent(this, TempActivity::class.java))
		}
		return super.onOptionsItemSelected(item)
	}
}
