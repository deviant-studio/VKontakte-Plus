package ds.vkplus.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import ds.vkplus.eventbus.EventBus;
import ds.vkplus.network.RestService;
import ds.vkplus.utils.T;

abstract public class BaseFragment extends Fragment {

	protected RestService rest = RestService.get();

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		rest.login(getActivity()).subscribe(this::onLoggedIn, e -> T.show(getActivity(), e.getMessage()));
	}

	protected void toggleProgress(boolean enable) {
		getActivity().setProgressBarIndeterminateVisibility(enable);
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
