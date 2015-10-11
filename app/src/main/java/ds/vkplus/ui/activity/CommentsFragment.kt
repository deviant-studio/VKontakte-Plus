package ds.vkplus.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.*
import android.widget.*
import com.j256.ormlite.misc.BaseDaoEnabled
import com.squareup.otto.Subscribe
import ds.vkplus.Constants
import ds.vkplus.R
import ds.vkplus.actionprovider.FilterActionProvider
import ds.vkplus.db.DBHelper
import ds.vkplus.eventbus.events.FilterEvent
import ds.vkplus.eventbus.events.UrlClickEvent
import ds.vkplus.model.Attachment
import ds.vkplus.model.Comment
import ds.vkplus.model.Filter
import ds.vkplus.model.PhotoData
import ds.vkplus.ui.CircleTransform
import ds.vkplus.ui.CroutonStyles
import ds.vkplus.ui.OnScrollBottomListener
import ds.vkplus.ui.crouton
import ds.vkplus.ui.view.FixedSizeImageView
import ds.vkplus.ui.view.FlowLayout
import ds.vkplus.utils.*
import kotterknife.ViewContainer
import kotterknife.bindView
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.regex.Pattern

class CommentsFragment : BaseFragment(), AdapterView.OnItemClickListener {
	/*
	@Bind(android.R.id.list) lateinit var list: ListView
	@Bind(android.R.id.empty) lateinit var empty: TextView
	@Bind(R.id.comment_edit) lateinit var commentField: EditText
*/

	val list: ListView by bindView(android.R.id.list)
	val empty: TextView by bindView(android.R.id.empty)
	val commentField: EditText by bindView(R.id.comment_edit)
	val sendButton: View by bindView(R.id.send)
	val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)


	private var adapter: CommentsAdapter? = null
	private var subscriber: Subscriber<List<Comment>>? = null
	
	val postId: Long by lazy { activity.intent.getLongExtra(Constants.KEY_POST_ID, 0) }
	val ownerId: Long by lazy { activity.intent.getLongExtra(Constants.KEY_OWNER_ID, 0) }
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.list_comments, container, false)
	}
	
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		//ButterKnife.bind(this, view)
		setHasOptionsMenu(true)

		actionBar.setDisplayHomeAsUpEnabled(true)
		actionBar.title="Comments"
		swipeRefresh.setColorSchemeColors(activity.resources.getColor(R.color.app_color))
		swipeRefresh.isEnabled=false

		initUI()
	}
	
	
	private fun initUI() {
		adapter = CommentsAdapter(activity, ArrayList<Comment>())
		list.adapter = adapter
		list.onItemClickListener = this
		sendButton.setOnClickListener { onCommentClick() }
		loadAllComments()
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.comments, menu)
		val p = MenuItemCompat.getActionProvider(menu.findItem(R.id.filter)) as FilterActionProvider
		p.init(Filter.TYPE_COMMENTS)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId){
			android.R.id.home -> activity.finish()
		}
		return super.onOptionsItemSelected(item)
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

	fun onCommentClick() {
		val text = commentField.text.toString()
		if (text.isEmpty()) return

		commentField.isEnabled = false
		sendButton.isEnabled = false
		swipeRefresh.isRefreshing = true
		fun onFinish() {
			commentField.isEnabled = true
			sendButton.isEnabled = true
			swipeRefresh.isRefreshing = false
		}
		rest.postComment(ownerId, postId, text)
			.subscribe ({
				//loadAllComments()
				commentField.setText("")
				val comment = Comment()
				comment.id = it.comment_id
				comment.from_id = DBHelper.instance.getMyId()
				comment.text = text
				comment.date = System.currentTimeMillis()
				comment.postId = postId
				DBHelper.instance.saveComments(listOf(comment), postId)
				adapter?.add(comment)
				activity.crouton("Posted")
				list.smoothScrollToPosition(adapter!!.count - 1)
			}, {
				it.printStackTrace()
				activity.crouton("Failed to post comment", CroutonStyles.ERROR)
				onFinish()
			}, {
				onFinish()
			})
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
		}
	}
	
	
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
		
		private val circleTransform = CircleTransform()
		

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
			loadRoundImage(item.producer.thumb, h.avatar)
			
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
							photos.add(PhotoData(imageUrl!!, a.photo.width, a.photo.height, PhotoData.TYPE_PHOTO, a.photo.id))
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
		
		
		class Holder(val v: View) : ViewContainer(v) {

			val text: TextView by bindView(R.id.text)
			val likes: CheckedTextView by bindView(R.id.likes)
			val date: TextView by bindView(R.id.date)
			val link: View by bindView(R.id.link)
			val linkPrimary: TextView by bindView(R.id.link_primary)
			val linkSecondary: TextView by bindView(R.id.link_secondary)
			val flow: FlowLayout by bindView(R.id.flow)
			val title: TextView by bindView(R.id.title)
			val avatar: ImageView by bindView(R.id.icon)

			private var cache: MutableList<View>? = null
			

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
