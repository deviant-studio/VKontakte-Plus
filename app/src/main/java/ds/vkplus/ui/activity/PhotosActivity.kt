package ds.vkplus.ui.activity

import android.app.Activity
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.transition.*
import android.view.*
import android.widget.ImageView
import butterknife.Bind
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback
import ds.vkplus.Constants
import ds.vkplus.R
import ds.vkplus.db.DBHelper
import ds.vkplus.model.Attachment
import ds.vkplus.model.PhotoData
import ds.vkplus.ui.Croutons
import ds.vkplus.ui.view.HackyViewPager
import ds.vkplus.utils.L
import ds.vkplus.utils.Utils
import ds.vkplus.utils.postDelayed
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers
import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.IOException
import java.util.*

public class PhotosActivity : AppCompatActivity() {
	
	@Bind(R.id.viewpager) lateinit val viewPager: HackyViewPager
	@Bind(R.id.toolbar) lateinit val toolbar: Toolbar

	private var isReturning = false

	override fun onCreate(savedInstanceState: Bundle?) {
		window.sharedElementEnterTransition = TransitionInflater.from(this).inflateTransition(R.anim.image_transition)

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photos)
		supportPostponeEnterTransition()
		ButterKnife.bind(this)
		setSupportActionBar(toolbar)
		
		var attachments: Collection<Attachment>? = null
		val id = intent.getLongExtra(Constants.KEY_POST_ID, -1)
		L.v("post id=" + id)
		val post = DBHelper.instance.fetchNewsById(id.toInt())
		if (post != null) {
			attachments = post.attachments
		} else {
			val comment = DBHelper.instance.fetchCommentById(id.toInt())
			if (comment != null) {
				attachments = comment.attachments
			}
		}
		
		if (attachments == null) {
			Croutons.prepare().message(R.string.fail).callback(object : LifecycleCallback {
				override fun onDisplayed() {
				}
				
				
				override fun onRemoved() {
					finish()
				}
			}).show(this)
			return
		}
		
		val photoId = intent.getLongExtra(Constants.KEY_PHOTO_ID, -1)
		L.v("photo id=" + photoId)
		
		var currIndex = 0
		val data = ArrayList<PhotoData>()
		var count = 0
		for (a in attachments) {
			if (a.photo != null) {
				val pd = PhotoData(a.photo.thumb, a.photo.biggestPhoto, a.photo.id)
				data.add(pd)
				if (a.photo.id == photoId)
					currIndex = count
			}
			count++
		}
		val adapter = PhotosAdapter(data)
		viewPager.adapter = adapter
		viewPager.setCurrentItem(currIndex, false)
		/*viewPager.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
			override fun onPageSelected(position: Int) {
				adapter.sharedElement
			}
		})*/
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.photos, menu)
		return true
	}
	
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val id = item.itemId
		if (id == R.id.share) {
			val data = (viewPager.adapter as PhotosAdapter).getItemAtPos(viewPager.currentItem)
			Utils.shareText(this, data.extra!!)
		}
		return super.onOptionsItemSelected(item)
	}


	override fun finishAfterTransition() {
		isReturning = true
		val data = Intent()
		val item = (viewPager.adapter as PhotosAdapter).getItemAtPos(viewPager.currentItem)
		data.putExtra("id", item.id.toString())
		data.putExtra("position", viewPager.currentItem)
		setResult(Activity.RESULT_OK, data)
		super.finishAfterTransition()
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	inner class PhotosAdapter(private val data: List<PhotoData>) : PagerAdapter() {
		
		var animPostponed = true
		var animating = true
		val attachers = HashMap<View, PhotoViewAttacher>()
		val postponedViews = HashSet<ImageView>()
		var sharedElement: View? = null

		init {
			setupTransitions()
		}

		private fun setupTransitions() {
			setEnterSharedElementCallback(object : SharedElementCallback() {
				override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
					if (sharedElement != null) {
						names.clear()
						sharedElements.clear()
						names.add(sharedElement!!.transitionName)
						sharedElements.put(sharedElement!!.transitionName, sharedElement!!)
					}
				}

				override fun onSharedElementEnd(sharedElementNames: MutableList<String>, sharedElements: MutableList<View>, sharedElementSnapshots: MutableList<View>) {
					L.v("onSharedElementEnd")
					for (v in postponedViews) {
						attachers.getOrPut(v, provideAttacher(v))
					}
					postponedViews.clear()
					animating = false
				}

				override fun onSharedElementStart(sharedElementNames: MutableList<String>, sharedElements: MutableList<View>, sharedElementSnapshots: MutableList<View>) {
				}
			})
		}

		override fun getCount(): Int {
			return data.size()
		}
		
		
		public fun getItemAtPos(index: Int): PhotoData {
			return data.get(index)
		}
		
		
		override fun instantiateItem(container: ViewGroup, position: Int): View {
			val imageView = ImageView(container.context)
			val item = data[position]
			imageView.transitionName = item.id.toString()

			observable<Bitmap> {
				try {
					it.onNext(Picasso.with(container.context).load(item.url).get())
					it.onNext(Picasso.with(container.context).load(item.extra).get())
				} catch (e: IOException) {
					e.printStackTrace()
					it.onError(e)
				}
			}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({
					imageView.setImageBitmap(it)

					if (animating) {
						postponedViews.add(imageView)
					} else
						attachers.getOrPut(imageView, provideAttacher(imageView)).update()

					if (animPostponed) {
						animPostponed = false
						this@PhotosActivity.supportStartPostponedEnterTransition()
					}
				})
			
			
			container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
			return imageView
		}

		private fun provideAttacher(view: ImageView): () -> PhotoViewAttacher {
			return {
				val a = PhotoViewAttacher(view)
				a.onPhotoTapListener = PhotoViewAttacher.OnPhotoTapListener { view, f1, f2 ->
					sharedElement = view
					supportFinishAfterTransition()
				}
				a
			}
		}
		
		
		override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
			val view = obj as View
			container.removeView(view)
			attachers.remove(view)?.cleanup()
		}
		
		override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

		override fun setPrimaryItem(container: ViewGroup?, position: Int, o: Any?) {
			super.setPrimaryItem(container, position, o)
			sharedElement = o as ImageView
		}
	}

}