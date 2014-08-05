package ds.vkplus.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.Window;
import ds.vkplus.R;
import ds.vkplus.network.RestService;
import ds.vkplus.utils.T;


public class MainActivity extends FragmentActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(R.string.news);

		if (savedInstanceState == null) {
			RestService.get().login(this).subscribe(token -> showFragment(), e -> T.show(this, "Failed to login"));
		}
	}


	private void showFragment() {
		getSupportFragmentManager().beginTransaction()
		                           .replace(R.id.container, getFragment())
		                           .commit();
	}


	private Fragment getFragment() {
		return new NewsFragment();
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main,menu);
		return super.onCreateOptionsMenu(menu);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


}
