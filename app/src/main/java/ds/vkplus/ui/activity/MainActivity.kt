package ds.vkplus.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import ds.vkplus.R
import ds.vkplus.model.ApiResponse
import ds.vkplus.network.RestService
import ds.vkplus.utils.L
import ds.vkplus.utils.Utils
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers


class MainActivity : RxAppCompatActivity() {

	var newTransitionName: String? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		supportActionBar?.setIcon(R.drawable.logo)
		supportActionBar?.setDisplayShowTitleEnabled(false)

		if (savedInstanceState == null) {
			showFragment()
		}
		
		//RestService.instance.getGroups().subscribe({ L.v("groups: fetched successfully") }, { it.printStackTrace() })
	}
	
	fun getActionBarView(): View {
		val window = window
		val v = window.decorView
		val resId = resources.getIdentifier("action_bar_container", "id", "android")
		return v.findViewById(resId)
	}
	
	
	private fun showFragment() {
		observable<ProgressDialog> {
			val progressDialog = ProgressDialog.show(this@MainActivity, null, "Loading...")
			progressDialog.setCancelable(true)
			it.onNext(progressDialog)
		}
			.observeOn(Schedulers.computation())
			.doOnNext {
				if (Utils.isMainThread()) throw IllegalThreadStateException()
				RestService.instance.getGroups().toBlocking().first()
			}
			.doOnError { it.printStackTrace() }
			.observeOn(AndroidSchedulers.mainThread())
			.compose<ProgressDialog> (bindToLifecycle())
			.subscribe ({
				it.dismiss()
				supportFragmentManager.beginTransaction().replace(R.id.container, getFragment(), "news").commit()
			},{
				L.e("failed to attach fragment")
				it.printStackTrace()
			})
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
			
			//R.id.debug_layout -> startActivity(Intent(this, TempActivity::class.java))
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onActivityReenter(requestCode: Int, data: Intent) {
		super.onActivityReenter(requestCode, data)
		val id = data.extras.getString("id")
		newTransitionName = id
	}


}
