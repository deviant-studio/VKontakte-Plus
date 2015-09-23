package ds.vkplus.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import ds.vkplus.R;


public class CommentsActivity extends AppCompatActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(R.string.comments);

		if (savedInstanceState == null) {
			showFragment();
		}
	}


	private void showFragment() {
		getSupportFragmentManager().beginTransaction()
		                           .replace(R.id.container, getFragment())
		                           .commit();
	}


	private Fragment getFragment() {
		return new CommentsFragment();
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


}
