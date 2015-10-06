package ds.vkplus.ui.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Window
import ds.vkplus.R

public class CommentsActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setTitle(R.string.comments)
		
		if (savedInstanceState == null) {
			showFragment()
		}
	}
	
	
	private fun showFragment() {
		supportFragmentManager
			.beginTransaction()
			.replace(R.id.container, getFragment())
			.commit()
	}
	
	
	private fun getFragment(): Fragment {
		return CommentsFragment()
	}
	
	
}
