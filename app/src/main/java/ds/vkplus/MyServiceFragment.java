/*
package ds.vkplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.gson.Gson;
import ds.vkplus.utils.L;
import ds.vkplus.utils.T;
import rx.Observable;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;

import javax.inject.Inject;

public class MyServiceFragment extends ServiceFragment {

	//RxLoaderManager lm;


	@Inject
	Rest rest;
	@InjectView(R.id.text)
	TextView text;
	@InjectView(R.id.button)
	Button button;


	//private RxLoader<WeatherData> weatherLoader;
	Observable<WeatherData> weatherObservable;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}


	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		App.inject(this);
		ButterKnife.inject(this, view);


	}


	@Override
	protected void onServiceBinded(final RestService service) {
		L.v("onServiceBinded");
		weatherObservable = rest.getWeather("Kharkiv");
		AndroidObservable.bindFragment(this, weatherObservable);

		load();
	}


	private void load() {
		service.run(weatherObservable)
		       .subscribe(new Subscriber<WeatherData>() {
			       @Override
			       public void onStart() {
				       toggleProgress(true);
				       button.setEnabled(false);
			       }


			       @Override
			       public void onCompleted() {
				       toggleProgress(false);
				       button.setEnabled(true);
			       }


			       @Override
			       public void onError(final Throwable e) {
				       e.printStackTrace();
				       T.show(getActivity(), e.getMessage());
				       toggleProgress(false);
				       button.setEnabled(true);
			       }


			       @Override
			       public void onNext(final WeatherData weatherData) {
				       showWeather(weatherData);
			       }
		       });
	}


	private void showWeather(final WeatherData data) {
		T.show(getActivity(), "loaded");
	*/
/*	String t = String.format("base\t%s\nclouds\t%s\ncod\t%sdt\t%s\nid\t%s\nmain\t%s\nname\t%s\nsunrise\t%s\nsunset\t%s\nwind\t%s\nweather\t%s",
				String.valueOf(data.base),
				String.valueOf(data.clouds.all),
				String.valueOf(data.cod),
				String.valueOf(data.dt),
				String.valueOf(data.id),
				String.valueOf(data.main.temp),
				String.valueOf(data.name),
				new SimpleDateFormat().format(new Date(data.sys.sunrise)),
				new SimpleDateFormat().format(new Date(data.sys.sunset)),
				String.valueOf(data.wind.speed),
				String.valueOf(data.weathers.get(0).description)
		);*//*

		String t=new Gson().toJson(data);
		text.setText(t);

	}


	@OnClick(R.id.button)
	void onButtonClick() {
		load();
	}


}
*/
