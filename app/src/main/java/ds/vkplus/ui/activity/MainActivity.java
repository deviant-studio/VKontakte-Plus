package ds.vkplus.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import ds.vkplus.R;
import ds.vkplus.model.ApiResponse;
import ds.vkplus.network.RestService;
import ds.vkplus.utils.L;


public class MainActivity extends FragmentActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(R.string.news);

		if (savedInstanceState == null) {
			showFragment();
			//RestService.get().login(this).subscribe(token -> showFragment(), e -> T.show(this, "Failed to login"));
		}

		RestService.get()
		           .getGroups()
		           .subscribe(groups -> {
			           L.v("groups fetched successfully");
		           }, Throwable::printStackTrace);
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
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.debug_rest:
				RestService.get()
				           .dummyRequestWithError(new ApiResponse.Error(5, "fake error!"))
				           .subscribe(
						           L::v,
						           e -> {
							           L.e("last mile error");
							           e.printStackTrace();
						           },
						           () -> L.v("completed"));

				break;

		}
		return super.onOptionsItemSelected(item);
	}
}
