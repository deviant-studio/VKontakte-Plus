package ds.vkplus.eventbus;

import com.squareup.otto.Bus;

public class EventBus {

	static Bus instance;


	public static void post(Object o) {
		get().post(o);
	}


	public static void register(Object o) {
		get().register(o);
	}


	public static void unregister(Object o) {
		get().unregister(o);
	}


	private static Bus get() {
		if (instance == null)
			instance = new Bus();

		return instance;
	}

}
