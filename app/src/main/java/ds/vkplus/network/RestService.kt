package ds.vkplus.network

import android.os.SystemClock
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ds.vkplus.auth.AccountHelper
import ds.vkplus.db.DBHelper
import ds.vkplus.exception.VKException
import ds.vkplus.model.*
import ds.vkplus.utils.L
import ds.vkplus.utils.Utils
import retrofit.RestAdapter
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.functions.Func1
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers
import rx.subjects.ReplaySubject
import java.lang.reflect.Modifier

class RestService {
	
	var restApi: IRestApi
	var gson: Gson
	private val db = DBHelper.instance
	
	private val token: String? = null
	
	
	init {
		gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.PRIVATE).create()
		val restAdapter = RestAdapter.Builder()
			.setEndpoint(BASE_URL)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setRequestInterceptor({
				it.addQueryParam("access_token", AccountHelper.instance.peekToken())
				it.addQueryParam("v", API_VERSION)
			}).build()
		
		restApi = restAdapter.create(IRestApi::class.java)
	}
	

	fun <T> applySchedulers(): Observable.Transformer<in T, out T> = Observable.Transformer() {
		it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
	}
	
	private fun <K: Any> networker(request: Observable<ApiResponse<K>>): Observable<K> {
		
		return request.map({
			if (it.error != null && it.error.error_code != VKException.CODE_NETWORK) {
				throw VKException(it.error)
			} else {
				it.response
			}
		})
			.retry({ count: Int, e: Throwable ->
				L.v("onRetry " + count)
				if (e is VKException) {
					L.e("vk exception: " + e.message)
					if (e.code == VKException.CODE_TOKEN_OBSOLETED) {
						try {
							AccountHelper.instance.refreshToken()
							return@retry true
						} catch (e1: InterruptedException) {
							e1.printStackTrace()
						}
					}
				}

				SystemClock.sleep(1000)

				count < 2
			})
			.onErrorResumeNext({
				Observable.empty<K>()
			})
			.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
		
	}
	
	
	fun getNews2(nextData: PostData?, sourceId: String?, count: Int): Observable<List<News>> {
		
		if (nextData == null && db.fetchNewsCount() != 0) {
			L.w("next is null! fetching from database")
			// check db here
			return observable<List<News>> {
				it.onNext(db.fetchAllNews())
				it.onCompleted()
			}.compose(applySchedulers())
			
		}
		
		//String filters = null;
		val filters = "post,photo,photo_tag,friend,note"
		val result = ReplaySubject.create<List<News>>()
		val newsObserver = networker(restApi.getNews2(filters, sourceId, nextData?.nextRaw, count, null, null))
		newsObserver
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.io())
			.subscribe(object : Action1<NewsResponse> {
				override fun call(news: NewsResponse) {
					L.v("is main thread=" + Utils.isMainThread())
					db.saveNewsResponse(news)
					val allNews = db.fetchNews(if (nextData != null) nextData.postDate else System.currentTimeMillis() / 1000)
					result.onNext(allNews)
					result.onCompleted()
				}
			}, Action1<Throwable> { result.onError(it) })
		
		return result.observeOn(AndroidSchedulers.mainThread())
	}
	
	
	fun getAllComments(postId: Long, ownerId: Long): Observable<List<Comment>> {
		
		val onSubscribe: Observable.OnSubscribe<CommentsList> = object : Observable.OnSubscribe<CommentsList> {
			override fun call(subscriber: Subscriber<in CommentsList>) {
				var needMore = true
				var offset = 0
				val count = 10
				do {
					val list = restApi.getCommentsRaw(postId, ownerId, offset, count)
					if (list.error == null) {
						if (list.response.items.size > 0) {
							subscriber.onNext(list.response)
							needMore = true
							offset += count
						} else
							needMore = false
					} else {
						subscriber.onError(VKException(list.error))
						needMore = false
					}

				} while (needMore && !subscriber.isUnsubscribed)
				subscriber.onCompleted()
			}
		}

		val result = ReplaySubject.create<List<Comment>>()
		val o = Observable.create(onSubscribe).subscribeOn(Schedulers.io())
		o.subscribe({
			L.v("is main thread=" + Utils.isMainThread())
			db.saveCommentsResponse(it, postId)
			val dateFrom = it.items[0].date
			val dateTo = it.items[it.items.size - 1].date
			val filters = db.filtersDao.fetchActiveFilters(Filter.TYPE_COMMENTS)
			val fetched = db.fetchComments(postId, dateFrom, dateTo, filters)
			result.onNext(fetched)
		}, {
			it.printStackTrace()
			result.onError(it)
		}, {
			result.onCompleted()
		})
		
		return result
	}
	
	
	fun getVideo(v: Video): Observable<String> {
		val result: Observable<String>
		if (v.player != null) {
			L.v("found cached video")
			result = Observable.just(v.player)
		} else {
			L.v("fething video data")
			val video = "${v.owner_id}_${v.id}_${v.access_key}"
			result = networker(restApi.getVideo(video, 0)).doOnNext(object : Action1<VideosList> {
				override fun call(next: VideosList) {
					db.saveVideo(next.items.get(0))
				}
			}).map(object : Func1<VideosList, String> {
				override fun call(obj: VideosList): String {
					return obj.items.get(0).player
				}
			})
		}
		return result
		
	}
	
	
	fun likePost(id: Long, ownerId: Long, doLike: Boolean): Observable<LikeResponse> {
		return like(id, ownerId, doLike, "post")
	}
	
	
	fun likeComment(id: Long, ownerId: Long, doLike: Boolean): Observable<LikeResponse> {
		return like(id, ownerId, doLike, "comment")
	}
	
	
	private fun like(id: Long, ownerId: Long, doLike: Boolean, type: String): Observable<LikeResponse> {
		return networker(if (doLike) restApi.like(id, ownerId, type) else restApi.unlike(id, ownerId, type)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
	}
	
	
	fun getGroups(): Observable<VKList<Group>> {
		L.v("get groups")
		val result = networker(restApi.getGroups())
			.observeOn(Schedulers.io())
			.doOnNext({
				if (Utils.isMainThread()) throw IllegalThreadStateException()
				val dao = db.groupsDao
				db.saveEntities(it.items, dao)
				val myGroups = db.fetchMyGroups()
				db.refreshGroupsFilter(myGroups!!)
			}).observeOn(AndroidSchedulers.mainThread())
		
		return result
		
	}
	
	
	fun dummyRequestWithError(e: ApiResponse.Error): Observable<String> {
		val fake = generateFakeRequest(e)
		return networker(Observable.just(fake))
	}
	
	
	fun dummyRequest(m: String): Observable<String> {
		val fake = generateFakeRequest(m)
		return networker(Observable.just(fake))
	}
	
	
	private fun generateFakeRequest(m: String): ApiResponse<String> {
		val fake = ApiResponse<String>()
		fake.response = m
		return fake
	}
	
	
	private fun generateFakeRequest(e: ApiResponse.Error): ApiResponse<String> {
		val fake = ApiResponse<String>()
		fake.error = e
		return fake
	}
	
	fun postComment(ownerId: Long, postId: Long, text: String, replyTo: Long? = null): Observable<CommentId> {
		return networker(restApi.postComment(ownerId, postId, text, replyTo))

	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	companion object {
		
		val BASE_URL: String = "https://api.vk.com/method"
		val API_VERSION: String = "5.24"
		val instance: RestService by lazy { RestService() }

	}
}
