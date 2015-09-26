package ds.vkplus;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import io.fabric.sdk.android.Fabric;

public class App extends Application {

	private static App instance;


	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		Stetho.initializeWithDefaults(this);
		instance = this;

	}



	public static App instance() {
		return instance;
	}
}
