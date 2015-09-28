package ds.vkplus.ui.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import ds.vkplus.utils.L
import ds.vkplus.utils.Utils
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers
import uk.co.senab.photoview.PhotoView

import java.io.IOException
import java.util.ArrayList

public class PhotosActivity : AppCompatActivity() {
	
	@Bind(R.id.viewpager) lateinit val viewPager: ViewPager
	@Bind(R.id.toolbar) lateinit val toolbar: Toolbar

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photos)
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
		viewPager.adapter = PhotosAdapter(data)
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
			Utils.shareText(this, data.extra)
		}
		return super.onOptionsItemSelected(item)
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	class PhotosAdapter(private val data: List<PhotoData>) : PagerAdapter() {
		
		
		override fun getCount(): Int {
			return data.size()
		}
		
		
		public fun getItemAtPos(index: Int): PhotoData {
			return data.get(index)
		}
		
		
		override fun instantiateItem(container: ViewGroup, position: Int): View {
			val photoView = PhotoView(container.context)
			val url = data.get(position).url
			val big = data.get(position).extra
			
			observable<Bitmap> {
				try {
					it.onNext(Picasso.with(container.context).load(url).get())
					it.onNext(Picasso.with(container.context).load(big).get())
				} catch (e: IOException) {
					e.printStackTrace()
					it.onError(e)
				}
			}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({ photoView.setImageBitmap(it) })
			
			
			container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
			return photoView
		}
		
		
		override fun destroyItem(container: ViewGroup, position: Int, obj: Any) = container.removeView(obj as View)
		
		override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
	}
}
