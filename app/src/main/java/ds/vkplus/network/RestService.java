package ds.vkplus.network;

import android.os.NetworkOnMainThreadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ds.vkplus.auth.AccountHelper;
import ds.vkplus.db.DBHelper;
import ds.vkplus.db.extras.AndroidDao;
import ds.vkplus.exception.VKException;
import ds.vkplus.model.*;
import ds.vkplus.utils.L;
import ds.vkplus.utils.Utils;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
		gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.PRIVATE).create();
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
			request.addQueryParam("access_token", AccountHelper.getInstance().peekToken());
			request.addQueryParam("v", API_VERSION);
		};
	}


/*	public Observable<String> login(Activity a) {
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
	}*/


	public <T> Observable<T> work(Observable.OnSubscribe<T> onSubscribe) {
		return Observable.create(onSubscribe)
		                 .subscribeOn(Schedulers.io())
		                 .observeOn(AndroidSchedulers.mainThread());
	}


/*	private <K> Observable<K> networker(final Observable<ApiResponse<K>> request) {
		ReplaySubject<K> result = ReplaySubject.create();
		request.subscribe(new NetworkerSubscriber<>(request, result));
		return result;
	}*/


	private <K> Observable<K> networker(final Observable<ApiResponse<K>> request) {

		return request
				.map(obj -> {
					if (obj.error != null && obj.error.error_code != VKException.CODE_NETWORK) {
						throw new VKException(obj.error);
					} else {
						return obj.response;
					}
				})
				.retry((count, e) -> {
					L.v("onRetry " + count);
					if (e instanceof VKException) {
						L.e("vk exception: " + ((VKException) e).message);
						if (((VKException) e).code == VKException.CODE_TOKEN_OBSOLETED) {
							try {
								AccountHelper.getInstance().refreshToken();
								return true;
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
					return count < 2;
				})
				.onErrorResumeNext(t -> Observable.empty())
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());

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


	public Observable<List<News>> getNews2(PostData nextData, String sourceId, int count) {

		if (nextData == null && db.fetchNewsCount() != 0) {
			L.w("next is null! fetching from database");
			// check db here
			return work(subscriber -> {

				subscriber.onNext(db.fetchAllNews());
				subscriber.onCompleted();
			});

		}

		//String filters = null;
		String filters = "post,photo,photo_tag,friend,note";
		ReplaySubject<List<News>> result = ReplaySubject.create();
		Observable<NewsResponse> newsObserver = networker(restApi.getNews2(filters, sourceId, nextData != null ? nextData.nextRaw : null, count, null, null));
		newsObserver.subscribeOn(Schedulers.io())
		            .observeOn(Schedulers.io())
		            .subscribe(news -> {
			            L.v("is main thread=" + Utils.isMainThread());
			            db.saveNewsResponse(news);
			            final List<News> allNews = db.fetchNews(nextData != null ? nextData.postDate : System.currentTimeMillis() / 1000);
			            result.onNext(allNews);
			            result.onCompleted();
		            }, result::onError);

		return result.observeOn(AndroidSchedulers.mainThread());
	}


/*	public Observable<CommentsList> getComments(final long postId, final long ownerId, final int offset, final int count) {
		return networker(
				restApi.getComments(postId, ownerId, offset, count))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}*/


	public Observable<List<Comment>> getAllComments(final long postId, final long ownerId) {

		Observable.OnSubscribe<CommentsList> onSubscribe = subscriber -> {
			boolean needMore = true;
			int offset = 0;
			int count = 10;
			do {
				ApiResponse<CommentsList> list = restApi.getCommentsRaw(postId, ownerId, offset, count);
				if (list.error == null) {
					if (list.response.items.size() > 0) {
						subscriber.onNext(list.response);
						needMore = true;
						offset += count;
					} else
						needMore = false;
				} else {
					subscriber.onError(new VKException(list.error));
					needMore = false;
				}

				try {
					synchronized (subscriber) {
						subscriber.wait(500);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (needMore && !subscriber.isUnsubscribed());
			subscriber.onCompleted();
		};

		ReplaySubject<List<Comment>> result = ReplaySubject.create();
		Observable<CommentsList> o = Observable.create(onSubscribe)
		                                       .subscribeOn(Schedulers.io());
		//.delay(commentsList -> Observable.from(commentsList).delay(1, TimeUnit.SECONDS));
		o.subscribe(new Subscriber<CommentsList>() {
			@Override
			public void onCompleted() {
				result.onCompleted();
			}


			@Override
			public void onError(final Throwable e) {
				e.printStackTrace();
				result.onError(e);
			}


			@Override
			public void onNext(final CommentsList comments) {
				L.v("is main thread=" + Utils.isMainThread());
				db.saveCommentsResponse(comments, postId);
				long dateFrom = comments.items.get(0).date;
				long dateTo = comments.items.get(comments.items.size() - 1).date;
				final List<Filter> filters = db.filtersDao.fetchActiveFilters(Filter.TYPE_COMMENTS);
				List<Comment> fetched = db.fetchComments(postId, dateFrom, dateTo, filters);
				result.onNext(fetched);
			}
		});

		return result;
	}


	/*public Observable<List<Comment>> getAllComments2(final long postId, final long ownerId) {
		boolean needMore = true;
		int offset = 0;
		int count = 10;
		Observable request = networker(restApi.getComments(postId, ownerId, offset, count));

		restApi.getComments().map()
	}*/


	/*public Observable<Integer> getNewPostsCount() {
		L.v("start getting count");
		int interval = Utils.isWifi() ? 10 : 120;
		Observable<Integer> intervals =
				Observable.interval(interval, TimeUnit.SECONDS)
				          .map(i -> db.fetchLatestPost())
				          .flatMap(latest -> networker(restApi.getNews2(null, null, null, 50, latest.date + 1, 0)))
				          .map(newsResponse -> newsResponse.items.size())
				          .onErrorReturn(throwable -> 0)
				          .subscribeOn(Schedulers.io())
				          .observeOn(AndroidSchedulers.mainThread());
		return intervals;

	}*/


	/**
	 * Saves latest posts to db and returns posts count
	 *
	 * @return
	 */
	public Observable<Integer> getFreshNews() {
		L.v("start getting count");
		String filters = "post,photo,photo_tag,friend,note";
		int interval = Utils.isWifi() ? 10 : 120;
		Observable<Integer> intervals =
				Observable.interval(interval, TimeUnit.SECONDS)
				          .map(i -> db.fetchLatestPost())
				          .flatMap(latest -> networker(
						          restApi.getNews2(filters, null, null, 100, latest != null ? latest.date + 1 : System.currentTimeMillis() / 1000, null)))
						.observeOn(Schedulers.io())
								//.subscribeOn(Schedulers.io())
						.map(newsResponse -> {
							L.v("is main thread=" + Utils.isMainThread());
							db.saveNewsResponse(newsResponse);
							return newsResponse.items.size();
						})
						.onErrorReturn(throwable -> {
							throwable.printStackTrace();
							return 0;
						})
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread());
		return intervals;

	}


	public Observable<String> getVideo(final Video v) {
		Observable<String> result;
		if (v.player != null) {
			L.v("found cached video");
			result = Observable.from(v.player);
		} else {
			L.v("fething video data");
			String video = v.owner_id + "_" + v.id + "_" + v.access_key;
			result = networker(restApi.getVideo(video, 0)).doOnNext(next -> db.saveVideo(next.items.get(0))).map(obj -> obj.items.get(0).player);
		}
		return result;

	}


	public Observable<LikeResponse> likePost(final long id, final long ownerId, final boolean doLike) {
		return like(id, ownerId, doLike, "post");
	}


	public Observable<LikeResponse> likeComment(final long id, final long ownerId, final boolean doLike) {
		return like(id, ownerId, doLike, "comment");
	}


	private Observable<LikeResponse> like(final long id, final long ownerId, boolean doLike, String type) {
		return networker(doLike ? restApi.like(id, ownerId, type) : restApi.unlike(id, ownerId, type)).subscribeOn(Schedulers.io())
		                                                                                              .observeOn(AndroidSchedulers.mainThread());
	}


	public Observable<VKList<Group>> getGroups() {

		Observable<VKList<Group>> result = networker(restApi.getGroups())
				.observeOn(Schedulers.io())
				.map(groups -> {
					try {
						if (Utils.isMainThread()) {
							throw new NetworkOnMainThreadException();
						}
						AndroidDao<Group, Integer> dao = db.getDao(Group.class);
						db.saveEntities(groups.items, dao);
						List<Group> myGroups = db.fetchMyGroups();
						db.refreshGroupsFilter(myGroups);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					return groups;
				})
				.observeOn(AndroidSchedulers.mainThread());

		return result;

	}


	public Observable<String> dummyRequestWithError(ApiResponse.Error e) {
		ApiResponse<String> fake = generateFakeRequest(e);
		return networker(Observable.from(fake));
	}


	public Observable<String> dummyRequest(String m) {
		ApiResponse<String> fake = generateFakeRequest(m);
		return networker(Observable.from(fake));
	}


	private ApiResponse<String> generateFakeRequest(final String m) {
		ApiResponse<String> fake = new ApiResponse<>();
		fake.response = m;
		return fake;
	}


	private ApiResponse<String> generateFakeRequest(final ApiResponse.Error e) {
		ApiResponse<String> fake = new ApiResponse<>();
		fake.error = e;
		return fake;
	}
}
