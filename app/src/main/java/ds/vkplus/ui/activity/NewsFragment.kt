package ds.vkplus.ui.activity


import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.*
import butterknife.Bind
import butterknife.ButterKnife
import butterknife.bindView
import com.j256.ormlite.misc.BaseDaoEnabled
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import ds.vkplus.App
import ds.vkplus.Constants
import ds.vkplus.R
import ds.vkplus.actionprovider.FilterActionProvider
import ds.vkplus.db.DBHelper
import ds.vkplus.db.DBHelperBase
import ds.vkplus.eventbus.EventBus
import ds.vkplus.eventbus.events.ClickEvent
import ds.vkplus.eventbus.events.FilterEvent
import ds.vkplus.eventbus.events.UrlClickEvent
import ds.vkplus.model.*
import ds.vkplus.model.Filter
import ds.vkplus.network.RestService
import ds.vkplus.ui.CircleTransform
import ds.vkplus.ui.Croutons
import ds.vkplus.ui.OnScrollBottomRecyclerViewListener
import ds.vkplus.ui.view.FixedSizeImageView
import ds.vkplus.ui.view.FlowLayout
import ds.vkplus.ui.view.LayoutUtils
import ds.vkplus.utils.*
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.observable

import java.util.ArrayList

class NewsFragment : BaseFragment() {
	
	@Bind(R.id.list) lateinit var recyclerView: RecyclerView
	@Bind(android.R.id.empty) lateinit var emptyView: TextView
	
	private var adapter: NewsRecyclerAdapter? = null
	private var mLayoutManager: RecyclerView.LayoutManager? = null
	private val postsChecker: Observable<Int>? = null
	private val postsCountSubscriber: Subscriber<Int>? = null
	private var newPostsCount: Int = 0
	private var currentSourceId: String? = null
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.list_news, null)
	}


	override fun onViewCreated(view: View?, b: Bundle?) {
		ButterKnife.bind(this, view)
		setHasOptionsMenu(true)
		
		//view.setNestedScrollingEnabled(true);
		
		initList()
		
		currentSourceId = DBHelper.instance.fetchCurrentGroupFilterId()
		
		L.v("current group filter=" + currentSourceId)
		
		loadNews()
		
		//runCountChecker();
		
		/*if (b != null)
			lastPosition = b.getInt("position");*/
		
		val ab = (activity as AppCompatActivity).supportActionBar
		ab.setDisplayShowTitleEnabled(true)
		ab.setTitle(R.string.news)
		//ab.setDisplayUseLogoEnabled(true);
		
		super.onViewCreated(view, b)
	}
	
	
	override fun onDestroy() {
		super.onDestroy()
		
		if (postsCountSubscriber != null && !postsCountSubscriber.isUnsubscribed)
			postsCountSubscriber.unsubscribe()
	}
	

	private fun initList() {
		recyclerView.setHasFixedSize(true)
		mLayoutManager = LinearLayoutManager(activity)
		recyclerView.layoutManager = mLayoutManager
		val scrollListener = OnScrollBottomRecyclerViewListener(mLayoutManager as LinearLayoutManager) { lastPos -> loadMoreNews(getOldestNext()) }
		recyclerView.setOnScrollListener(scrollListener)
		if (adapter == null) {
			adapter = NewsRecyclerAdapter(ArrayList<News>(), scrollListener, recyclerView)
			recyclerView.adapter = adapter
		} else
			adapter!!.setItems(ArrayList<News>())

	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.news, menu)
		
		val p = MenuItemCompat.getActionProvider(menu.findItem(R.id.filter)) as FilterActionProvider
		p.init(Filter.TYPE_POSTS)
	}
	

	@Subscribe
	fun onFilterEvent(e: FilterEvent) {
		val f = e.filter
		if (f != null && f.parent != null && f.parent.title == DBHelperBase.FILTER_BY_GROUP) {
			L.v("group filter selected")
			currentSourceId = f.condition
			newPostsCount = 0
			//onRefresh();
			initList()
			
		} //else
		
		loadNews()
	}
	
	
	override fun onRefresh() {
		toggleProgress(true)
		if (newPostsCount == 0 || currentSourceId != null) {
			observable<Boolean> {
				try {
					DBHelper.instance.dropAll()
					it.onNext(true)
					it.onCompleted()
				} catch (e: Exception) {
					e.printStackTrace()
					it.onError(e)
				}
			}
				.compose(rest.applySchedulers())
				.subscribe({ loadNews() }) { Croutons.prepare().message("Failed to drop database").show(activity) }
		} else {
			loadNews()
		}
	}
	
	
	private fun getOldestNext(): PostData {
		return DBHelper.instance.fetchOldestNext()!!
	}
	
	
	private fun loadNews() {
		loadMoreNews(null)
	}
	
	
	private fun loadMoreNews(nextData: PostData?) {
		if (nextData == null/* || nextData.nextRaw.isEmpty()*/)
			initList()
		
		L.v("next=%s", if (nextData != null) nextData.nextRaw else "null")
		if (nextData != null && nextData.nextRaw == "") {
			L.e("empty next. the end of news")
			return
		}

		toggleProgress(true)
		rest.getNews2(nextData, currentSourceId, PAGE_SIZE).subscribe({
			if (it != null) {
				//news.init();
				fillView(it)

				if (nextData == null) {
					adapter?.previousPostition = newPostsCount
					if (newPostsCount != 0) {
						recyclerView.scrollToPosition(newPostsCount - 1)
						newPostsCount = 0
					}
				}
			}
		}, {
			L.e("error catched in fragment")
			it.printStackTrace()
			Croutons.prepare().message("Loading Error").show(activity)
			toggleProgress(false)

		})
	}
	
	
	override fun onLoggedIn(token: String) {
		//this.token = token;
		//loadNews();
	}
	
	
	private fun fillView(news: List<News>) {
		if (news.size() != 0) {
			emptyView.visibility = View.GONE
			toggleProgress(false)
		} else {
			L.w("load more because result is empty")
			loadMoreNews(getOldestNext())
		}
		//PostData nextData = getLatestNext();
		//L.v("next: " + nextData.nextRaw);
		adapter?.addToEnd(news)
		
	}
	
	
	@Subscribe
	fun onClickEvent(e: ClickEvent) {
		val item = e.item
		when (e.viewId) {
			R.id.comments -> {
				val i = Intent(activity, CommentsActivity::class.java)
				
				i.putExtra(Constants.KEY_POST_ID, item.post_id)
				i.putExtra(Constants.KEY_OWNER_ID, item.source_id)
				startActivity(i)
			}
			
			R.id.link -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(e.url)))
			
			R.id.likes -> {
			}
		}//}
	}
	
	
	@Subscribe
	fun onUrlClickEvent(e: UrlClickEvent) {
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(e.urls.get(0))))
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	class NewsRecyclerAdapter(
		var data: MutableList<News>?,
		private var scrollListener: OnScrollBottomRecyclerViewListener,
		private val recycler: RecyclerView
	) : RecyclerView.Adapter<NewsRecyclerAdapter.Holder>() {
		
		private val displaySize: Point
		private val picasso: Picasso
		private var initTime: Long = 0
		var previousPostition: Int = 0
			set(v) {
				$previousPostition = v
				resetInitTime()
			}
		
		protected var animDuration: Long = 0
		protected var interpolator: Interpolator
		private val circleTransform = CircleTransform()
		
		
		init {
			picasso = Picasso.with(App.instance)
			setHasStableIds(true)
			// anim init
			resetInitTime()
			previousPostition = -1
			interpolator = DecelerateInterpolator()
			val wm = App.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager
			displaySize = Point()
			wm.defaultDisplay.getSize(displaySize)
			//
		}
		
		
		fun resetInitTime() {
			initTime = System.currentTimeMillis()
		}
		
		
		fun resetBottomScroller() {
			scrollListener.prevLast = 0
		}
		
		
		override fun onCreateViewHolder(parent: ViewGroup, i: Int): Holder {
			val v = LayoutInflater.from(parent.context).inflate(R.layout.row_post, parent, false)
			val h = Holder(v, this)
			return h
		}
		
		
		override fun onBindViewHolder(h: Holder, p: Int) {
			L.v("id " + getItemId(p))
			val item = data!!.get(p)
			//L.v("attachments " + (item.attachments != null ? item.attachments.size() : -1));
			
			h.dateView.text = DateUtils.getRelativeTimeSpanString(item.date * 1000)
			h.comments.text = item.commentsCount.toString()
			h.likesView.text = item.likesCount.toString()
			h.reposts.text = item.repostsCount.toString()
			if (item.producer != null) {
				h.titleView.text = item.producer.name
				picasso.load(item.producer.thumb).transform(circleTransform).into(h.avatarView)
			}
			
			
			// fill signer if exist
			if (item.signer != null) {
				h.signer.visibility = View.VISIBLE
				h.signer.text = item.signer.name
			} else {
				h.signer.visibility = View.GONE
			}
			
			h.likesView.isChecked = item.likesUserLikes
			
			h.linkContainer.visibility = View.GONE
			
			// fill repost data
			if (item.copy_history != null && item.copy_history.size() > 0) {
				h.repostHeader.visibility = View.VISIBLE
				val repost = item.copy_history.iterator().next()
				h.repostTitle.text = repost.producer.name
				h.repostDate.text = DateUtils.getRelativeTimeSpanString(repost.date * 1000)
				picasso.load(repost.producer.thumb).transform(circleTransform).into(h.repostAvatar)
				item.text = repost.text
				item.attachments = repost.attachments
			} else {
				h.repostHeader.visibility = View.GONE
			}
			
			// crop text
			val text = item.text
			val collapsed = text != null && text.length() > NewsResponse.POST_LENGTH_THRESHOLD + 20 && !item.isExpanded
			h.expand.toggle(collapsed)
			h.textView.text = if (collapsed) text!!.substring(0, NewsResponse.POST_LENGTH_THRESHOLD) else text
			//}
			
			h.textView.toggle(!TextUtils.isEmpty(item.text))
			h.comments.toggle(item.commentsCanPost)
			
			val photos = ArrayList<PhotoData>()
			
			if (item.attachments != null) {
				for (a in item.attachments) {
					L.v("attahcment type=" + a.type)
					if (a.getContent<BaseDaoEnabled<*, *>>() == null) {
						L.e("attachment content is null!")
					}
					var imageUrl: String? = null
					when (a.type) {
						Attachment.TYPE_PHOTO -> if (a.photo != null) {
							imageUrl = a.photo.photo_604
							photos.add(PhotoData(imageUrl, a.photo.width, a.photo.height, PhotoData.TYPE_PHOTO, a.photo.id))
						}
						
						Attachment.TYPE_VIDEO -> {
							imageUrl = Observable.just(a.video.photo_640, a.video.photo_320, a.video.photo_130).toBlocking().first { pic -> pic != null }
							val pd = PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_VIDEO, a.video.id)
							photos.add(pd)
						}
						
						Attachment.TYPE_POSTED_PHOTO -> {
							imageUrl = a.posted_photo.photo_604
							photos.add(PhotoData(imageUrl, 0, 0, PhotoData.TYPE_PHOTO, a.posted_photo.id))
						}
						
						Attachment.TYPE_LINK -> {
							imageUrl = a.link.image_src
							if (imageUrl != null && photos.size() == 0) {
								val pd2 = PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_LINK, 0)
								pd2.extra = a.link.url
								photos.add(pd2)
							}
							h.linkContainer.visibility = View.VISIBLE
							h.linkPrimaryView.text = a.link.title
							h.linkSecondaryView.text = a.link.url
						}
						
						Attachment.TYPE_DOC -> {
							imageUrl = a.doc.photo_130
							val pd2 = PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_LINK, 0)
							pd2.extra = a.doc.url
							photos.add(pd2)
						}
						
						Attachment.TYPE_PAGE -> {
							h.linkContainer.visibility = View.VISIBLE
							h.linkPrimaryView.text = a.page.title
							h.linkSecondaryView.text = h.linkContainer.context.getString(R.string.page)
						}
						else -> {
							
						}
					}
					
					
				}
				
			}
			
			if (item.photosPersist != null)
				for (photo in item.photosPersist) {
					photos.add(PhotoData(photo.photo_604, photo.width, photo.height, PhotoData.TYPE_PHOTO, photo.id))
				}
			
			if (photos.size() != 0/* && !isScrolling()*/) {
				h.flowView.visibility = View.VISIBLE
				val displayMetrics = App.instance.resources.displayMetrics
				val size = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) - Utils.dp(App.instance, 28)
				loadImages(size, h.flowView, h.getViewsCache(), photos, getItemId(p), true)
			} else
				h.flowView.visibility = View.GONE
			
			// animation
			val v = h.card
			if (initTime + 500 < System.currentTimeMillis() && p > previousPostition && isScrolling()) {
				val speed = v.context.toDips(scrollListener.speed.toFloat()).toInt()
				Utils.dp(v.context, scrollListener.speed)
				
				animDuration = (ANIM_DEFAULT_SPEED / (Math.max(speed, 0) / 10f + 1)).toLong()
				
				L.v("speed=%s", speed)
				v.translationY = Utils.dp(v.context, 200).toFloat()
				val a = v.animate().translationY(0f).setDuration(animDuration).setInterpolator(interpolator)
				
				a.start()
			} else {
				//v.setTranslationY(0);   // return to default
			}
			
			previousPostition = p
			
		}
		
		
		private fun isScrolling(): Boolean = recycler.scrollState == RecyclerView.SCROLL_STATE_SETTLING || recycler.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
		
		
		override fun getItemCount(): Int = data?.size() ?: 0
		
		
		fun addToEnd(newData: List<News>) {
			val total = itemCount
			if (data != null) {
				if (data!!.size() < 3)
					initTime = System.currentTimeMillis()
				data!!.addAll(newData)
			} else
				data = ArrayList(newData)
			
			notifyItemRangeInserted(total, newData.size())
			//resetBottomScroller();
			
			// clean up a little bit
			if (data!!.size() > MAX_SIZE && newData.size() < MAX_SIZE) {
				//data.removeAll(data.subList(0, newData.size()));
				val i = data!!.iterator()
				var c = 0
				while (i.hasNext() && c < newData.size()) {
					i.next()
					i.remove()
					c++
				}
				notifyItemRangeRemoved(0, newData.size())
			}
			
			
		}
		
		
		override fun getItemId(position: Int): Long {
			return data!!.get(position).post_id
		}
		
		
		fun getItem(i: Int): News {
			return data!!.get(i)
		}
		
		
		fun setItems(list: MutableList<News>) {
			data = list
			notifyDataSetChanged()
		}
		
		companion object {
			
			private val MAX_SIZE = 200
			protected val ANIM_DEFAULT_SPEED: Long = 1000L
			
			
			fun loadImages(size: Int, flow: ViewGroup, imagesIterator: Iterator<View>, photos: List<PhotoData>, itemId: Long, loadPhotos: Boolean) {
				//L.v("item id "+itemId);
				flow.removeAllViews()
				Utils.toggleView(flow, photos.size() != 0)
				LayoutUtils.processThumbs(size, size, photos)
				
				if (photos.size() != 0) {
					for (photo in photos) {
						Utils.startTimer()
						val img: FixedSizeImageView
						if (imagesIterator.hasNext())
							img = imagesIterator.next() as FixedSizeImageView
						else {
							L.e("cache is empty. creating view manually...")
							img = FixedSizeImageView(flow.context)
						}
						img.displayW = photo.width
						img.displayH = photo.height
						val scaleType = ImageView.ScaleType.CENTER_CROP// : ImageView.ScaleType.FIT_CENTER;
						img.scaleType = scaleType
						img.toggleVideoIcon(photo.type == PhotoData.TYPE_VIDEO)
						//L.v("w=%s h=%s b=%s f=%s", photo.width, photo.height, photo.breakAfter, photo.floating);
						val lp = FlowLayout.LayoutParams(Utils.dp(App.instance, 2), Utils.dp(App.instance, /*photo.paddingBottom ? 10 : */2))
						if (photo.breakAfter || photo.floating) {
							lp.breakAfter = photo.breakAfter
							lp.floating = photo.floating
						}
						
						img.layoutParams = lp
						flow.addView(img)
						if (loadPhotos)
							Picasso.with(flow.context).load(photo.url).placeholder(img.placeholder).into(img)
						else
							img.setImageDrawable(img.placeholder)
						
						img.setOnClickListener { v ->
							if (photo.type == PhotoData.TYPE_PHOTO) {
								val i = Intent(v.context, PhotosActivity::class.java)
								i.putExtra(Constants.KEY_POST_ID, itemId)
								i.putExtra(Constants.KEY_PHOTO_ID, photo.id)
								v.context.startActivity(i)
							} else if (photo.type == PhotoData.TYPE_VIDEO) {
								openVideo(v.context, photo.id)
							} else if (photo.type == PhotoData.TYPE_LINK) {
								L.v("link click")
								val url = photo.extra
								if (url.startsWith("http"))
									EventBus.post(UrlClickEvent(listOf(url)))
							}
						}
						
						Utils.stopTimer("image load")
					}
					
				}
			}
			
			
			private fun openVideo(c: Context, id: Long) {
				val v = DBHelper.instance.fetchVideo(id.toInt())
				val videoRequest = RestService.instance.getVideo(v!!)
				videoRequest.subscribe({ result -> //T.show(App.instance(), "Success");
					c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result)))
				}, { e ->
					T.show(App.instance, "Fail!")
					e.printStackTrace()
				})
				
			}
		}

		class Holder(v: View, private val adapter: NewsRecyclerAdapter) : RecyclerView.ViewHolder(v) {

			@Bind(R.id.date) lateinit val dateView: TextView
			@Bind(R.id.flow) lateinit var flowView: ViewGroup
			@Bind(R.id.avatar) lateinit val avatarView: ImageView
			@Bind(R.id.link_primary) lateinit val linkPrimaryView: TextView
			@Bind(R.id.link_secondary) lateinit val linkSecondaryView: TextView
			@Bind(R.id.text) lateinit val textView: TextView
			@Bind(R.id.likes) lateinit val likesView: CheckedTextView
			@Bind(R.id.title) lateinit val titleView: TextView
			@Bind(R.id.repost_avatar) lateinit val repostAvatar: ImageView      // +
			@Bind(R.id.repost_title) lateinit val repostTitle: TextView         // +
			@Bind(R.id.repost_date) lateinit val repostDate: TextView           // +
			@Bind(R.id.comments) lateinit val comments: CheckedTextView         // +
			@Bind(R.id.reposts) lateinit val reposts: CheckedTextView           // +
			@Bind(R.id.signer) lateinit val signer: TextView                    // +
			@Bind(R.id.expand) lateinit val expand: TextView                    // +
			@Bind(R.id.link) lateinit val linkContainer: ViewGroup              // +
			@Bind(R.id.overflow) lateinit val overflow: ImageView               // +
			@Bind(R.id.header) lateinit val header: ViewGroup                   // +
			@Bind(R.id.repost_header) lateinit val repostHeader: ViewGroup      // +
			@Bind(R.id.card) lateinit val card: ViewGroup                       // +


			private var cache: MutableList<View>? = null


			init {
				ButterKnife.bind(this, v)
				initViews()
			}

			private fun initViews() {
				comments.setOnClickListener { onClick(it) }
				textView.setOnClickListener { onTextClick(it) }
				expand.setOnClickListener { onTextClick(it) }
				likesView.setOnClickListener { onLikeClick(it) }
				reposts.setOnClickListener { onClick(it) }
				linkContainer.setOnClickListener { onLinkClick(it) }
				titleView.setOnClickListener { title -> L.v("data size=%s pos=%s", adapter.data!!.size(), position) }

				overflow.setOnClickListener { view ->
					val popup = ListPopupWindow(view.context)
					popup.anchorView = view
					popup.setContentWidth(Utils.dp(view.context, 200))

					popup.setAdapter(ArrayAdapter.createFromResource(view.context, R.array.overflow_items, android.R.layout.simple_list_item_1))
					popup.setOnItemClickListener { parent, view2, position, id ->
						when (position) {
							0 // share
							-> Utils.shareText(view.context, Utils.getPostUrl(getItem()))
							1 // copy to clipboard
							-> {
								Toast.makeText(view.context, "Copied to Clipboard", 0).show()
								Utils.copyNoteUrlToClipboard(view.context, getItem().text)
							}
							2// debug
							-> L.v("item=" + getItem().toString())
						}
						popup.dismiss()
					}
					popup.show()
				}
			}


			private fun onLikeClick(view: View) {
				val item = getItem()
				RestService.instance.likePost(item.post_id, item.source_id, !item.likesUserLikes).subscribe({
					item.likesCount = it.likes
					item.likesUserLikes = !item.likesUserLikes
					adapter.notifyItemChanged(position)
				}, { err -> T.show(view.context, view.context.getString(R.string.fail)) })

			}


			private fun onLinkClick(view: View) {
				val url = linkSecondaryView.text.toString()
				if (url.startsWith("http"))
					EventBus.post(UrlClickEvent(listOf(url)))
				else
					T.show(view.context, "Not implemented yet")

			}


			private fun onTextClick(view: View) {
				val i = getItem()
				L.v("text click %s isExpand=%s", position, i.isExpanded)
				L.v("notify")
				i.isExpanded = !i.isExpanded
				adapter.notifyItemChanged(position)
			}


			private fun onClick(view: View) {
				L.v("%s %s", itemId, position)
				EventBus.post(ClickEvent(getItem(), view.id))
			}


			private fun getItem(): News {
				return adapter.data!!.get(position)
			}


			fun getViewsCache(): Iterator<View> {
				if (cache == null) {
					cache = ArrayList<View>()
					for (i in 0..10) {
						cache!!.add(FixedSizeImageView(flowView.context))
					}
				}
				return cache!!.iterator()
			}
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	companion object {
		private val PAGE_SIZE = 50
	}
}
