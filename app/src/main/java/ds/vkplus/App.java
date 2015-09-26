package ds.vkplus;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class App extends Application {

	private static App instance;


	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		instance = this;

	}



	public static App instance() {
		return instance;
	}
}
