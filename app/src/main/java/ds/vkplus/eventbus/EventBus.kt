package ds.vkplus.eventbus

import com.squareup.otto.Bus

object EventBus {

    private val instance: Bus by lazy { Bus() }

    public fun post(o: Any) = instance.post(o)
    public fun register(o: Any) = instance.register(o)
    public fun unregister(o: Any) = instance.unregister(o)

}
