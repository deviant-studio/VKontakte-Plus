package ds.vkplus.ui.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.db.DBHelper;
import ds.vkplus.model.Attachment;
import ds.vkplus.model.Comment;
import ds.vkplus.model.News;
import ds.vkplus.model.PhotoData;
import ds.vkplus.ui.Croutons;
import ds.vkplus.utils.L;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PhotosActivity extends AppCompatActivity {

	@Bind(R.id.viewpager)
	ViewPager viewPager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);
		ButterKnife.bind(this);

		Collection<Attachment> attachments = null;
		long id = getIntent().getLongExtra(Constants.KEY_POST_ID, -1);
		L.v("post id=" + id);
		News post = DBHelper.instance().fetchNewsById((int) id);
		if (post != null) {
			attachments = post.attachments;
		} else {
			Comment comment = DBHelper.instance().fetchCommentById((int) id);
			if (comment != null) {
				attachments = comment.attachments;
			}
		}

		if (attachments == null) {
			Croutons.prepare().message(R.string.fail).callback(new LifecycleCallback() {
				@Override
				public void onDisplayed() { }


				@Override
				public void onRemoved() {
					finish();
				}
			}).show(this);
			return;
		}

		long photoId = getIntent().getLongExtra(Constants.KEY_PHOTO_ID, -1);
		L.v("photo id=" + photoId);

		int currIndex = 0;
		final List<PhotoData> data = new ArrayList<>();
		int count = 0;
		for (Attachment a : attachments) {
			if (a.photo != null) {
				PhotoData pd = new PhotoData(a.photo.getThumb(), a.photo.getBiggestPhoto(), a.photo.id);
				data.add(pd);
				if (a.photo.id == photoId)
					currIndex = count;
			}
			count++;
		}
		viewPager.setAdapter(new PhotosAdapter(this, data));
		viewPager.setCurrentItem(currIndex, false);
	}


/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photos, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	static class PhotosAdapter extends PagerAdapter {

		private List<PhotoData> data;


		public PhotosAdapter(final Context c, final List<PhotoData> data) {
			this.data = data;
		}


		@Override
		public int getCount() {
			return data.size();
		}


		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			//photoView.setBackgroundColor(Color.BLACK);
			String url = data.get(position).url;
			final String big = data.get(position).extra;

			Observable.create((Subscriber<? super Bitmap> s) -> {
				try {
					s.onNext(Picasso.with(container.getContext()).load(url).get());
					s.onNext(Picasso.with(container.getContext()).load(big).get());
				} catch (IOException e) {
					e.printStackTrace();
					s.onError(e);
				}
			})
			          .subscribeOn(Schedulers.io())
			          .observeOn(AndroidSchedulers.mainThread())
			          .subscribe(photoView::setImageBitmap);


			container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			return photoView;
		}


		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}


		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
