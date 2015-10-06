package ds.vkplus

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric

public class App : Application() {
	
	
	override fun onCreate() {
		super.onCreate()
		instance = this
		Fabric.with(this, Crashlytics())
		if (BuildConfig.DEBUG) {
			//Stetho.initializeWithDefaults(this)
			//LeakCanary.install(this)
		}
	}
	
	companion object {
		lateinit var instance: App
	}
}
