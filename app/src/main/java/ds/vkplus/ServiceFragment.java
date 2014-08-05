/*
package ds.vkplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.View;

public abstract class ServiceFragment extends Fragment implements ServiceConnection {

	protected RestServiceOld service;




	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Intent service = new Intent(getActivity(), RestServiceOld.class);
		getActivity().bindService(service, this, Context.BIND_AUTO_CREATE);

	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unbindService(this);
	}


	@Override
	public void onServiceConnected(final ComponentName name, final IBinder b) {
		service = ((RestServiceOld.RestBinder) b).getService();
		onServiceBinded(service);
	}


	protected abstract void onServiceBinded(final RestServiceOld service);


	@Override
	public void onServiceDisconnected(final ComponentName name) { }


	protected void toggleProgress(boolean enable) {
		getActivity().setProgressBarIndeterminateVisibility(enable);
	}
}
*/
