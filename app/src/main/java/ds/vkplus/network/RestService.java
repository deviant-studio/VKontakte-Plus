package ds.vkplus.network;

import android.app.Activity;
import com.google.gson.Gson;
import ds.vkplus.auth.AccountHelper;
import ds.vkplus.db.DBHelper;
import ds.vkplus.exception.VKException;
import ds.vkplus.model.*;
import ds.vkplus.utils.L;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

import java.util.List;

public class RestService {

	public static final String BASE_URL = "https://api.vk.com/method";
	public static final String API_VERSION = "5.24";

	IRestApi restApi;
	static RestService instance;
	public Gson gson;
	private DBHelper db = DBHelper.instance();

	/*	Observable<String> login = work(s -> {
			try {
				String token = AccountHelper.getInstance().getToken();
				L.v("auth: token=" + token);
				s.onNext(token);
				s.onCompleted();
			} catch (Throwable e) {
				e.printStackTrace();
				s.onError(e);
			}
		});*/

	/*Observable<String> relogin = work(s -> {
		try {
			String token = AccountHelper.getInstance().refreshToken();
			L.v("auth: token=" + token);
			s.onNext(token);
			s.onCompleted();
		} catch (Throwable e) {
			e.printStackTrace();
			s.onError(e);
		}
	});*/


	private String token;


	public RestService() {
		gson = new Gson();
		final RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint(BASE_URL)
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setRequestInterceptor(getInterceptor())
						//.setErrorHandler(getErrorHandler())
				.build();

		restApi = restAdapter.create(IRestApi.class);
	}


	private RequestInterceptor getInterceptor() {
		return request -> {
			request.addQueryParam("access_token", token);
			request.addQueryParam("v", API_VERSION);
		};
	}


	public Observable<String> login(Activity a) {
		return work(s -> {
			try {
				token = AccountHelper.getInstance().getToken(a);
				L.v("auth: token=" + token);
				s.onNext(token);
				s.onCompleted();
			} catch (Throwable e) {
				s.onError(e);
			}
		});
		//return login;
	}


/*	public <T> Observable<T> runWithAuth(Activity a, Observable<T> o) {
		final ReplaySubject<T> result = ReplaySubject.create();
		login(a).doOnCompleted(() -> o.subscribe(result))
		        .doOnError(Throwable::printStackTrace);
		return result;
	}*/


	public <T> Observable<T> work(Observable.OnSubscribe<T> onSubscribe) {
		return Observable.create(onSubscribe)
		                 .subscribeOn(Schedulers.io())
		                 .observeOn(AndroidSchedulers.mainThread());
	}


	private <K> Observable<K> networker(Observable<ApiResponse<K>> request) {
		ReplaySubject<K> result = ReplaySubject.create();
		Observable<K> helper = ReplaySubject.create((Subscriber<? super K> subscriber) -> {
			L.v("start helper observable");
			request.doOnNext(response -> {
				if (response.error != null) {
					L.w("found error");
					switch (response.error.error_code) {
						case VKException.CODE_TOKEN_OBSOLETED:
							try {
								token = AccountHelper.getInstance().refreshToken();
								L.v("done refresh token. repeating request...");
								request.retry();
								return;
							} catch (InterruptedException e) {
								e.printStackTrace();
								subscriber.onError(new VKException());
							}
							break;
						default:
							subscriber.onError(new VKException(response.error));
					}
					subscriber.onError(new VKException(response.error));
				} else {
					L.v("found response");
					subscriber.onNext(response.response);
					subscriber.onCompleted();
				}
			}).doOnError(Throwable::printStackTrace)
			       .subscribe();

		});

		helper.subscribeOn(Schedulers.io())
		      .observeOn(AndroidSchedulers.mainThread())
		      .subscribe(result);

		return result/*helper*/;

	}


	public static RestService get() {
		if (instance == null) {
			instance = new RestService();
		}
		return instance;
	}


/*	public Observable getNews(String next, int count) {
		String filters = "post";
		return restApi.getNews(filters, null, next, count)
		              .subscribeOn(Schedulers.io())
		              .observeOn(AndroidSchedulers.mainThread());

	}*/


/*	public Observable<News> getNews2(String next, int count) {
		String filters = "post";
		return networker(restApi.getNews2(filters, null, next, count), News.class);
	}*/


	/*public Observable<NewsResponse> getNews(String next, int count) {
		String filters = "post";
		Observable<NewsResponse> o = networker(restApi.getNews2(filters, null, next, count));
		//o.toBlocking().first();
		return o;
	}*/


	public Observable<List<News>> getNews2(PostData nextData, int count) {
		if (nextData == null && db.fetchNewsCount() != 0) {
			// check db here
			return work(subscriber -> {
				subscriber.onNext(db.fetchAllNews());
				subscriber.onCompleted();
			});

		}

		String filters = null;//"post";
		ReplaySubject<List<News>> result = ReplaySubject.create();
		Observable<NewsResponse> newsObserver = networker(restApi.getNews2(filters, null, nextData != null ? nextData.nextRaw : null, count));
		newsObserver.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(news -> {
			db.saveNewsResponse(news);
			final List<News> allNews = db.fetchNews(nextData != null ? nextData.postDate : -1);
			result.onNext(allNews);
			result.onCompleted();
		}, result::onError);

		return result;
	}


	public Observable<CommentsList> getComments(final long postId, final long ownerId, final int offset, final int count) {
		return networker(restApi.getComments(postId, ownerId, offset, count));
	}
}
