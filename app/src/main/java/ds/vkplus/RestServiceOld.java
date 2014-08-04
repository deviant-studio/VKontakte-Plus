package ds.vkplus;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import ds.vkplus.utils.L;
import rx.Observable;
import rx.subjects.ReplaySubject;

public class RestServiceOld extends Service {

	private IBinder binder = new RestBinder();


	public class RestBinder extends Binder {

		public RestServiceOld getService() {
			return RestServiceOld.this;
		}
	}



	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		L.v("onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	/*	public <T> void run(*//*PublishSubject<T> result,*//* Observable<T> o) {
		o.subscribe(o);
		Observer a;
		Subscriber s;
	}*/


	public <T> Observable<T> run(Observable<T> o/*, Subscriber<T> result*/) {
		L.v("run");
		ReplaySubject<T> result = ReplaySubject.create();
		o.subscribe(result);
		return result;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		L.v("onCreate");
//		EventBus.getDefault().register(this);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		L.v("onDestroy");
//		EventBus.getDefault().unregister(this);
	}


	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}
