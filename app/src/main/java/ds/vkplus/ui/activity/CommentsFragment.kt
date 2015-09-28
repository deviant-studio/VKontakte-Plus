package ds.vkplus.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.*
import android.widget.*
import butterknife.Bind
import butterknife.ButterKnife
import com.j256.ormlite.misc.BaseDaoEnabled
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import ds.vkplus.Constants
import ds.vkplus.R
import ds.vkplus.actionprovider.FilterActionProvider
import ds.vkplus.db.DBHelper
import ds.vkplus.eventbus.events.FilterEvent
import ds.vkplus.eventbus.events.UrlClickEvent
import ds.vkplus.model.*
import ds.vkplus.model.Filter
import ds.vkplus.ui.CircleTransform
import ds.vkplus.ui.OnScrollBottomListener
import ds.vkplus.ui.view.FixedSizeImageView
import ds.vkplus.ui.view.FlowLayout
import ds.vkplus.utils.L
import ds.vkplus.utils.T
import ds.vkplus.utils.Utils
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

class CommentsFragment : BaseFragment(), AdapterView.OnItemClickListener {
	
	@Bind(android.R.id.list) lateinit val list: ListView
	@Bind(android.R.id.empty) lateinit val empty: TextView
	@Bind(R.id.content) lateinit val content: ViewGroup
	
	private var adapter: CommentsAdapter? = null
	private var subscriber: Subscriber<List<Comment>>? = null
	
	val postId: Long by lazy { activity.intent.getLongExtra(Constants.KEY_POST_ID, 0) }
	val ownerId: Long by lazy { activity.intent.getLongExtra(Constants.KEY_OWNER_ID, 0) }
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.list_comments, container, false)
	}
	
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		ButterKnife.bind(this, view)
		setHasOptionsMenu(true)
		content.visibility = View.GONE
		
		list.setOnScrollListener(OnScrollBottomListener { /*todo*/ })
		
		initUI()
	}
	
	
	private fun initUI() {
		adapter = CommentsAdapter(activity, ArrayList<Comment>())
		list.adapter = adapter
		list.onItemClickListener = this
		loadAllComments()
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.comments, menu)
		val p = MenuItemCompat.getActionProvider(menu.findItem(R.id.filter)) as FilterActionProvider
		p.init(Filter.TYPE_COMMENTS)
	}
	
	
	override fun onRefresh() {
		initUI()
	}
	
	
	override fun onLoggedIn(token: String) {
		
	}
	
	
	@Subscribe
	fun onFilterEvent(e: FilterEvent) {
		val actives = DBHelper.instance.filtersDao.fetchActiveFilters(Filter.TYPE_COMMENTS)
		fetchFiltered(actives)
	}
	
	
	private fun fetchFiltered(actives: List<Filter>) {

		if (subscriber != null && !subscriber!!.isUnsubscribed) {
			L.v("filtered while loading")
			subscriber!!.unsubscribe()
			initUI()
		} else {
			// just fetch whole table
			L.v("filtered in idle mode")
			adapter!!.clear()
			Observable.create { subscriber: Subscriber<in List<Comment>> ->
				subscriber.onNext(DBHelper.instance.fetchComments(postId, 0, System.currentTimeMillis() / 1000, actives))
				subscriber.onCompleted()
			}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({ fillView2(it) },
					{ e -> e.printStackTrace() })
		}
	}
	
	
	private fun loadAllComments() {
		
		subscriber = object : Subscriber<List<Comment>>() {
			
			override fun onStart() {
				toggleProgress(true)
			}
			
			
			override fun onCompleted() {
				toggleProgress(false)
			}
			
			
			override fun onError(e: Throwable) {
				L.e("error catched in fragment")
				toggleProgress(false)
				e.printStackTrace()
			}
			
			
			override fun onNext(comments: List<Comment>?) {
				if (comments != null) {
					L.i("got %s comments", comments.size())
					fillView2(comments)
				}
				
			}
		}
		
		rest.getAllComments(postId, ownerId).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber)
	}
	
	
	private fun fillView2(comments: List<Comment>) {
		adapter!!.addAll(comments)
		if (comments.size() != 0) {
			empty.visibility = View.GONE
			content.visibility = View.VISIBLE
		}
	}
	
	
	/*	private fun fillView(comments: CommentsList) {
			L.v("comments: " + rest.gson.toJson(comments))

			if (comments.items.size() != 0) {
				empty.visibility = View.GONE
				content.visibility = View.VISIBLE
			}

			if (adapter == null) {
				adapter = CommentsAdapter(activity, comments.items)
				list.adapter = adapter

			} else {
				adapter!!.addAll(comments.items)
			}
		}*/
	
	
	override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
		val item = parent.adapter.getItem(position) as Comment
		showPopup(item, view)
	}
	
	
	private fun showPopup(item: Comment, anchor: View) {
		val popup = ListPopupWindow(activity)
		popup.anchorView = anchor
		popup.setContentWidth(Utils.dp(activity, 200))
		
		popup.setAdapter(ArrayAdapter.createFromResource(activity, R.array.comment_popup_items, android.R.layout.simple_list_item_1))
		popup.setOnItemClickListener { parent, view2, position, id ->
			when (position) {
			// like
				0 -> rest.likeComment(item.id, ownerId, !item.likesUserLikes)
					.subscribe({
						item.likesCount = it.likes
						item.likesUserLikes = !item.likesUserLikes
						adapter!!.notifyDataSetChanged()
					}, {
						T.show(activity, getString(R.string.fail))
					})
			// share
				1 -> T.show(activity, "share not implemented yet")
			// reply
				2 -> T.show(activity, "reply not implemented yet")
			}
			popup.dismiss()
		}
		popup.show()
		
	}
	
	
	@Subscribe
	fun onUrlClickEvent(e: UrlClickEvent) {
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(e.urls.get(0))))
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	class CommentsAdapter(context: Context, data: List<Comment>) : ArrayAdapter<Comment>(context, 0, data) {
		
		private val picasso: Picasso
		private val circleTransform = CircleTransform()
		
		
		init {
			picasso = Picasso.with(context)
		}
		
		
		override fun getView(position: Int, v: View?, parent: ViewGroup): View {
			var v = v
			val h: Holder
			if (v == null) {
				v = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false)
				h = Holder(v)
				v!!.tag = h
			} else {
				h = v.tag as Holder
			}
			
			val item = getItem(position)
			h.title.text = item.producer.name
			
			val text: CharSequence
			val m = REPLY_PATTERN.matcher(item.text)
			if (m.find()) {
				L.v("matches")
				val replace = m.replaceFirst("$1")
				L.v(replace)
				text = Utils.setSpanForSubstring(m.replaceFirst("$1"), m.group(1), context.resources.getColor(R.color.gray1))
			} else
				text = item.text
			
			h.text.text = text
			Utils.toggleView(h.text, !TextUtils.isEmpty(text))
			h.likes.text = if (item.likesCount > 0) ("+${item.likesCount}") else ""
			h.likes.isChecked = item.likesUserLikes
			h.date.text = DateUtils.getRelativeTimeSpanString(item.date * 1000)
			picasso.load(item.producer.thumb).transform(circleTransform).into(h.avatar)
			
			h.link.visibility = View.GONE
			h.flow.visibility = View.GONE
			
			if (item.attachments != null) {
				val photos = ArrayList<PhotoData>()
				for (a in item.attachments) {
					L.v("attahcment type=" + a.type)
					if (a.getContent<BaseDaoEnabled<*, *>>() == null) {
						L.e("attachment content is null!")
					}
					var imageUrl: String? = null
					when (a.type) {
						Attachment.TYPE_PHOTO -> {
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
							h.link.visibility = View.VISIBLE
							h.linkPrimary.text = a.link.title
							h.linkSecondary.text = a.link.url
							h.link.setOnClickListener { clicked -> Utils.openURL(a.link.url) }
						}
						Attachment.TYPE_DOC -> {
							imageUrl = a.doc.photo_130
							val pd2 = PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_LINK, 0)
							pd2.extra = a.doc.url
							photos.add(pd2)
						}
						Attachment.TYPE_PAGE -> {
							h.link.visibility = View.VISIBLE
							h.linkPrimary.text = a.page.title
							h.linkSecondary.text = context.getString(R.string.page)
						}
						else -> {
							
						}
					}//h.link.setOnClickListener(clicked->Utils.openURL(a.page.url));
					
				}
				
				val size = -1
				/*DisplayMetrics displayMetrics = App.instance().getResources().getDisplayMetrics();
				size = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) - Utils.dp(App.instance(), 28) / 8;*/
				NewsFragment.NewsRecyclerAdapter.loadImages(size, h.flow, h.getViewsCache(), photos, getItemId(position), true)
				
				// clicks
				
			}
			
			
			return v
		}
		
		
		override fun getItemId(position: Int): Long {
			return getItem(position).id
		}
		
		
		class Holder(v: View) {
			
			@Bind(R.id.text) lateinit val text: TextView
			@Bind(R.id.likes) lateinit val likes: CheckedTextView
			@Bind(R.id.date) lateinit val date: TextView
			@Bind(R.id.link) lateinit val link: View
			@Bind(R.id.link_primary) lateinit val linkPrimary: TextView
			@Bind(R.id.link_secondary) lateinit val linkSecondary: TextView
			@Bind(R.id.flow) lateinit val flow: FlowLayout
			@Bind(R.id.title) lateinit val title: TextView
			@Bind(R.id.icon) lateinit val avatar: ImageView
			
			private var cache: MutableList<View>? = null
			
			
			init {
				ButterKnife.bind(this, v)
			}
			
			
			fun getViewsCache(): Iterator<View> {
				if (cache == null) {
					cache = ArrayList<View>()
					for (i in 0..10) {
						cache!!.add(FixedSizeImageView(flow.context))
					}
				}
				return cache!!.iterator()
			}
		}
	}
	
	companion object {
		val REPLY_PATTERN: Pattern = Pattern.compile("""\[id\d+\|(.+)\]""")
	}
}
