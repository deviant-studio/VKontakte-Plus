package ds.vkplus.eventbus

import com.squareup.otto.Bus

object EventBus {

	private val instance: Bus by lazy { Bus() }

	public fun post(o: Any) = instance.post(o)
	public fun register(o: Any) {
		try {
			instance.register(o)
		} catch(e: Exception) {
		}
	}

	public fun unregister(o: Any) {
		try {
			instance.unregister(o)
		} catch(e: Exception) {
		}
	}
}
