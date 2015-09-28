package ds.vkplus

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric
import kotlin.properties.Delegates

public class App : Application() {
	
	
	override fun onCreate() {
		super.onCreate()
		Fabric.with(this, Crashlytics())
		Stetho.initializeWithDefaults(this)
		instance = this
		
	}
	
	companion object {
		lateinit var instance: App
	}
}
