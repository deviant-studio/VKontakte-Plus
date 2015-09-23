package ds.vkplus.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.trello.rxlifecycle.components.support.RxFragment;
import ds.vkplus.eventbus.EventBus;
import ds.vkplus.network.RestService;
import ds.vkplus.ui.view.RefreshButton;

abstract public class BaseFragment extends RxFragment {

	protected RestService rest = RestService.get();
	protected RefreshButton refreshButton;


	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refreshButton = new RefreshButton(getActivity());
		ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
		ab.setDisplayShowCustomEnabled(true);
		ab.setCustomView(refreshButton);
		//ab.setDisplayShowHomeEnabled(true);
		//ab.setDisplayUseLogoEnabled(true);
		//ab.setDisplayHomeAsUpEnabled(true);

		refreshButton.setOnClickListener(v->onRefresh());
		//setRetainInstance(true);
	}


	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//rest.login(getActivity()).subscribe(this::onLoggedIn, e -> T.show(getActivity(), e.getMessage()));
		//onLoggedIn();


	}


	protected abstract void onRefresh();


	protected void toggleProgress(boolean enable) {
		//getActivity().setProgressBarIndeterminateVisibility(enable);
		refreshButton.toggleProgress(enable);
		if (enable) {
			refreshButton.setNotificationsCount(0);
			//refreshButton.toggleVisibility(true);
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		EventBus.register(this);
	}


	@Override
	public void onPause() {
		super.onPause();
		EventBus.unregister(this);
	}


	abstract protected void onLoggedIn(String token);
}
