package ds.vkplus;

import android.app.Application;
import dagger.ObjectGraph;

public class App extends Application {

	private ObjectGraph graph;
	private static App instance;


	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		//VKSdk.initialize(listener, "APL4oJ9kK03xf8RBVJsX");

		graph = ObjectGraph.create(new DaggerModule());
	}


	public static void inject(Object object) {
		instance.graph.inject(object);
	}


	public static App instance() {
		return instance;
	}
}
