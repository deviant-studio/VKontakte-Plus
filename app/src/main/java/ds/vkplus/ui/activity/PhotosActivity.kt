package ds.vkplus.ui.activity

import android.app.Activity
import android.app.SharedElementCallback
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.bindView
import ds.vkplus.Constants
import ds.vkplus.R
import ds.vkplus.db.DBHelper
import ds.vkplus.model.Attachment
import ds.vkplus.model.PhotoData
import ds.vkplus.ui.crouton
import ds.vkplus.ui.view.HackyViewPager
import ds.vkplus.utils.L
import ds.vkplus.utils.Utils
import ds.vkplus.utils.loadImageBlocking
import ds.vkplus.utils.postDelayed
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.IOException
import java.util.*

class PhotosActivity : AppCompatActivity() {
	val viewPager: HackyViewPager by bindView(R.id.viewpager)
	val toolbar: Toolbar by bindView(R.id.toolbar)

	private var isReturning = false

	override fun onCreate(savedInstanceState: Bundle?) {
		if (Build.VERSION.SDK_INT >= 21)
			window.sharedElementEnterTransition = TransitionInflater.from(this).inflateTransition(R.transition.image_transition)

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photos)
		supportPostponeEnterTransition()
		setSupportActionBar(toolbar)
		
		var attachments: Collection<Attachment>? = null
		val id = intent.getLongExtra(Constants.KEY_POST_ID, -1)
		L.v("post id=" + id)
		val post = DBHelper.instance.fetchNewsById(id.toInt())
		if (post != null) {
			attachments = post.attachments as kotlin.collections.Collection<Attachment>
		} else {
			val comment = DBHelper.instance.fetchCommentById(id.toInt())
			if (comment != null) {
				attachments = comment.attachments
			}
		}
		
		if (attachments == null) {
			crouton(getString(R.string.fail))
			postDelayed(1000, { finish() })
			return
		}
		
		val photoId = intent.getLongExtra(Constants.KEY_PHOTO_ID, -1)
		L.v("photo id=" + photoId)
		
		var currIndex = 0
		val data = ArrayList<PhotoData>()
		var count = 0
		for (a in attachments) {
			if (a.photo != null) {
				val pd = PhotoData(a.photo.thumb!!, a.photo.biggestPhoto!!, a.photo.id)
				data.add(pd)
				if (a.photo.id == photoId)
					currIndex = count
			}
			count++
		}
		val adapter = PhotosAdapter(data)
		viewPager.adapter = adapter
		viewPager.setCurrentItem(currIndex, false)
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
		(viewPager.adapter as PhotosAdapter).cleanUpAll()
		isReturning = true
		val data = Intent()
		val item = (viewPager.adapter as PhotosAdapter).getItemAtPos(viewPager.currentItem)
		data.putExtra("id", item.id.toString())
		data.putExtra("position", viewPager.currentItem)
		setResult(Activity.RESULT_OK, data)
		super.finishAfterTransition()
	}

	override fun onDestroy() {
		super.onDestroy()
		viewPager.adapter = null
		//Utils.clearPicassoCache()
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	inner class PhotosAdapter(private val data: List<PhotoData>) : PagerAdapter() {
		
		var animPostponed = true
		var animating = true
		val attachers = HashMap<View, PhotoViewAttacher>()
		val postponedViews = HashMap<ImageView, Bitmap>()
		var sharedElement: View? = null

		init {
			if (Build.VERSION.SDK_INT >= 21)
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
			})

			window.sharedElementEnterTransition.addListener(object : TransitionListener() {
				override fun onTransitionEnd(transition: Transition?) {
					L.v("onTransitionEnd")
					for (entry in postponedViews) {
						entry.key.setImageBitmap(entry.value)
						attachers.getOrPut(entry.key, provideAttacher(entry.key)).update()
					}
					postponedViews.clear()
					animating = false
				}
			})
		}

		override fun getCount(): Int {
			return data.size
		}
		
		
		fun getItemAtPos(index: Int): PhotoData {
			return data[index]
		}
		
		
		override fun instantiateItem(container: ViewGroup, position: Int): View {
			L.v("instantiateItem $position")
			val imageView = ImageView(container.context)
			val item = data[position]
			ViewCompat.setTransitionName(imageView, item.id.toString())

			observable<Pair<Int, Bitmap>> {
				try {
					it.onNext(1 to loadImageBlocking(item.url))
					it.onNext(2 to loadImageBlocking(item.extra!!))
				} catch (e: IOException) {
					e.printStackTrace()
					it.onError(e)
				}
			}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({

					if (animating && Build.VERSION.SDK_INT >= 21) {
						L.v("animating...")
						if (it.first == 1) {
							L.v("url 1-$position")
							imageView.setImageBitmap(it.second)
							postponedViews.put(imageView, it.second)    // test this!
						} else {
							L.v("url 2-$position")
							postponedViews.put(imageView, it.second)
						}
					} else {
						L.v("after animating.")
						imageView.setImageBitmap(it.second)
						attachers.getOrPut(imageView, provideAttacher(imageView)).update()
					}

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

		fun cleanUpAll() {
			for (i in attachers) {
				i.value.cleanup()
			}
			//sharedElement = null
			//postponedViews.clear()
		}
		
		override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

		override fun setPrimaryItem(container: ViewGroup?, position: Int, o: Any?) {
			super.setPrimaryItem(container, position, o)
			if (o != null)
				sharedElement = o as ImageView
		}
	}

}

open class TransitionListener : Transition.TransitionListener {
	override fun onTransitionEnd(transition: Transition?) {
	}

	override fun onTransitionResume(transition: Transition?) {
	}

	override fun onTransitionPause(transition: Transition?) {
	}

	override fun onTransitionCancel(transition: Transition?) {
	}

	override fun onTransitionStart(transition: Transition?) {
	}

}
