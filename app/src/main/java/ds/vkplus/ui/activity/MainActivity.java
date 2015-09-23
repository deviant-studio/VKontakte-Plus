package ds.vkplus.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import ds.vkplus.R;
import ds.vkplus.model.ApiResponse;
import ds.vkplus.network.RestService;
import ds.vkplus.utils.L;


public class MainActivity extends AppCompatActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//setTitle(R.string.news);

		ActionBar ab = getSupportActionBar();
		//ab.setHideOnContentScrollEnabled(true);
		ab.setIcon(R.drawable.logo);
		View v=getActionBarView();

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

	public View getActionBarView() {
		Window window = getWindow();
		View v = window.getDecorView();
		int resId = getResources().getIdentifier("action_bar_container", "id", "android");
		return v.findViewById(resId);
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

			case R.id.debug_layout:
				startActivity(new Intent(this,TempActivity.class));
				break;

		}
		return super.onOptionsItemSelected(item);
	}
}
