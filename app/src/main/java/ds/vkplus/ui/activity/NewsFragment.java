package ds.vkplus.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import ds.vkplus.App;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.db.DBHelper;
import ds.vkplus.eventbus.EventBus;
import ds.vkplus.eventbus.events.ClickEvent;
import ds.vkplus.eventbus.events.UrlClickEvent;
import ds.vkplus.model.*;
import ds.vkplus.network.RestService;
import ds.vkplus.ui.Croutons;
import ds.vkplus.ui.OnScrollBottomRecyclerViewListener;
import ds.vkplus.ui.view.FixedSizeImageView;
import ds.vkplus.ui.view.FlowLayout;
import ds.vkplus.ui.view.LayoutUtils;
import ds.vkplus.utils.L;
import ds.vkplus.utils.T;
import ds.vkplus.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NewsFragment extends BaseFragment {

	private static final int PAGE_SIZE = 100;

	@InjectView(R.id.list)
	RecyclerView recyclerView;

	@InjectView(android.R.id.empty)
	TextView empty;


	//String token;
	//String next;
	private NewsRecyclerAdapter adapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private Observable<Integer> postsChecker;
	private Subscriber<Integer> postsCountSubscriber;
	//private int newPosts;



	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_news, null);
	}


	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		ButterKnife.inject(this, view);
		setHasOptionsMenu(true);

		initList();

		loadNews();

		runCountChecker();

		super.onViewCreated(view, savedInstanceState);
	}




	private void runCountChecker() {
		postsCountSubscriber = new Subscriber<Integer>() {
			@Override
			public void onCompleted() { }


			@Override
			public void onError(final Throwable e) {
				L.e("failed to get count");
				e.printStackTrace();
			}


			@Override
			public void onNext(final Integer count) {
				L.v("new posts: " + count);
				//newPosts = count;
				refreshButton.setNotificationsCount(count);
				//getActivity().invalidateOptionsMenu();
			}
		};
		postsChecker = rest.work(subscriber -> {
			L.v("postsChecker call");
				rest.getNewPostsCount().subscribe(val -> {
					subscriber.onNext(val);
					subscriber.onCompleted();
				}, e -> {
					L.e("failed to get count");
					e.printStackTrace();
					subscriber.onError(e);
				});
		});
		AndroidObservable.bindFragment(this, postsChecker);
		postsChecker.repeat().observeOn(AndroidSchedulers.mainThread()).subscribe(postsCountSubscriber);

	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		if (!postsCountSubscriber.isUnsubscribed())
			postsCountSubscriber.unsubscribe();
	}


	private void initList() {
		recyclerView.setHasFixedSize(true);
		//recyclerView.setItemAnimator(new SlideInFromLeftItemAnimator(getView()));
		mLayoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(mLayoutManager);
		RecyclerView.OnScrollListener scrollListener = new OnScrollBottomRecyclerViewListener((LinearLayoutManager) mLayoutManager,
				lastPos -> loadMoreNews(getBottomNext(lastPos)));
		recyclerView.setOnScrollListener(scrollListener);
		if (adapter == null) {
			adapter = new NewsRecyclerAdapter(new ArrayList<>(), (OnScrollBottomRecyclerViewListener) scrollListener);
			recyclerView.setAdapter(adapter);
		} else
			adapter.setItems(new ArrayList<>());

	}


	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.news, menu);
		/*MenuItem refresh = menu.findItem(R.id.refresh);
		refresh.setVisible(newPosts > 0);*/
	}


	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			/*case R.id.force_refresh:
				onRefresh();
				break;*/
			case R.id.action_settings:

				break;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onRefresh() {
		toggleProgress(true);
		initList();
		rest.work(subscriber -> {
			try {
				DBHelper.instance().dropAll();
				subscriber.onNext(true);
				subscriber.onCompleted();
			} catch (Exception e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		})
		    .subscribe(res -> {
			    //toggleProgress(false);
			    loadNews();
		    }, e -> Croutons.prepare().message("Failed to drop database").show(getActivity()));
	}


	private PostData getBottomNext(int pos) {
		L.v("next pos=" + pos);
		long postId = adapter.getItemId(pos);
		return DBHelper.instance().fetchNextByPostId(postId);
	}


	private PostData getLatestNext() {
		return DBHelper.instance().fetchLatestNext();
		//return Prefs.get().getString(Constants.KEY_NEXT, null);
	}


	private void loadNews() {
		loadMoreNews(null);
	}


	private void loadMoreNews(final PostData nextData) {
		rest.getNews2(nextData, PAGE_SIZE)
				//.subscribe(this::fillView, e -> T.show(getActivity(), "error getting news"));
				.subscribe(new Subscriber<List<News>>() {

					@Override
					public void onStart() {
						toggleProgress(true);
					}


					@Override
					public void onCompleted() {
						toggleProgress(false);
					}


					@Override
					public void onError(final Throwable e) {
						L.e("error catched in fragment");
						e.printStackTrace();
						Croutons.prepare().message("Loading Error").show(getActivity());
						toggleProgress(false);
					}


					@Override
					public void onNext(final List<News> news) {
						if (news != null) {
							//news.init();
							fillView(news);
						}

					}
				});
	}


	@Override
	protected void onLoggedIn(String token) {
		//this.token = token;
		//loadNews();
	}


	private void fillView(final List<News> news) {
		if (news.size() != 0)
			empty.setVisibility(View.GONE);

		PostData nextData = getLatestNext();
		L.v("next: " + nextData.nextRaw);
		adapter.addToEnd(news);

		// need invalidate refresh button
		//newPosts = 0;
		//getActivity().invalidateOptionsMenu();
	}


	@Subscribe
	public void onClickEvent(ClickEvent e) {
		//T.show(getActivity(), "clicked %s %s", e.id, e.viewId);
		News item = e.item;
		switch (e.viewId) {
			case R.id.comments:
				if (item.commentsCount > 0) {
					Intent i = new Intent(getActivity(), CommentsActivity.class);
					i.putExtra(Constants.KEY_POST_ID, item.post_id);
					i.putExtra(Constants.KEY_OWNER_ID, item.source_id);
					startActivity(i);
				}
				break;

			case R.id.link:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(e.url)));
				break;

			case R.id.likes:

				break;
		}
	}


	@Subscribe
	public void onUrlClickEvent(UrlClickEvent e) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(e.urls.get(0))));
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static class NewsRecyclerAdapter extends RecyclerView.Adapter<Holder> {

		private Point displaySize;
		private List<News> data;
		private Picasso picasso;
		private long initTime;
		protected static final long ANIM_DEFAULT_SPEED = 1000L;
		protected int previousPostition;
		protected OnScrollBottomRecyclerViewListener scrollListener;
		protected long animDuration;
		protected Interpolator interpolator;


		private NewsRecyclerAdapter(List<News> data, OnScrollBottomRecyclerViewListener sl) {
			this.data = data;
			picasso = Picasso.with(App.instance());
			setHasStableIds(true);

			// anim init
			initTime = System.currentTimeMillis();
			scrollListener = sl;
			previousPostition = -1;
			interpolator = new DecelerateInterpolator();
			WindowManager wm = (WindowManager) App.instance().getSystemService(Context.WINDOW_SERVICE);
			displaySize = new Point();
			wm.getDefaultDisplay().getSize(displaySize);
			//
		}


		@Override
		public Holder onCreateViewHolder(final ViewGroup parent, final int i) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
			Holder h = new Holder(v, this);
			return h;
		}


		@Override
		public void onBindViewHolder(final Holder h, final int p) {
			L.v("id " + getItemId(p));
			News item = data.get(p);
			//L.v("attachments " + (item.attachments != null ? item.attachments.size() : -1));

			h.date.setText(DateUtils.getRelativeTimeSpanString(item.date * 1000));
			h.comments.setText(String.valueOf(item.commentsCount));
			h.likes.setText(String.valueOf(item.likesCount));
			h.reposts.setText(String.valueOf(item.repostsCount));
			if (item.getProducer() != null) {
				h.title.setText(item.getProducer().getName());
				picasso.load(item.getProducer().getThumb()).into(h.avatar);
			}


			// fill signer if exist
			if (item.getSigner() != null) {
				h.signer.setVisibility(View.VISIBLE);
				h.signer.setText(item.getSigner().getName());
			} else {
				h.signer.setVisibility(View.GONE);
			}

			h.likes.setChecked(item.likesUserLikes);

			h.linkContainer.setVisibility(View.GONE);

			// fill repost data
			if (item.copy_history != null && item.copy_history.size() > 0) {
				h.repostHeader.setVisibility(View.VISIBLE);
				News repost = item.copy_history.iterator().next();
				h.repostTitle.setText(repost.getProducer().getName());
				h.repostDate.setText(DateUtils.getRelativeTimeSpanString(repost.date * 1000));
				picasso.load(repost.getProducer().getThumb()).into(h.repostAvatar);
				item.text = repost.text;
				//item.isExpanded = repost.isExpanded;
				//L.v("repost: "+repost.text);
				item.attachments = repost.attachments;
			} else {
				h.repostHeader.setVisibility(View.GONE);
			}

			// crop text
			String text = item.text;
			boolean collapsed = text != null && text.length() > NewsResponse.POST_LENGTH_THRESHOLD + 20 && !item.isExpanded;
			Utils.toggleView(h.expand, collapsed);
			h.text.setText(collapsed ? text.substring(0, NewsResponse.POST_LENGTH_THRESHOLD) : text);
			//}

			Utils.toggleView(h.text, !TextUtils.isEmpty(item.text));
			Utils.toggleView(h.likes, item.likesCanLike);
			Utils.toggleView(h.comments, item.commentsCanPost);

			List<PhotoData> photos = new ArrayList<>();

			if (item.attachments != null) {
				for (Attachment a : item.attachments) {
					L.v("attahcment type=" + a.type);
					if (a.getContent() == null) {
						L.e("attachment content is null!");
					}
					String imageUrl = null;
					switch (a.type) {
						case Attachment.TYPE_PHOTO:
							imageUrl = a.photo.photo_604;
							photos.add(new PhotoData(imageUrl, a.photo.width, a.photo.height, PhotoData.TYPE_PHOTO, a.photo.id));
							break;

						case Attachment.TYPE_VIDEO:
							imageUrl = Observable.from(a.video.photo_640, a.video.photo_320, a.video.photo_130).toBlocking().first(pic -> pic != null);
							PhotoData pd = new PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_VIDEO, a.video.id);
							photos.add(pd);
							break;

						case Attachment.TYPE_POSTED_PHOTO:
							imageUrl = a.posted_photo.photo_604;
							photos.add(new PhotoData(imageUrl, 0, 0, PhotoData.TYPE_PHOTO, a.posted_photo.id));
							break;

						case Attachment.TYPE_LINK:
							imageUrl = a.link.image_src;
							if (imageUrl != null && photos.size() == 0) {
								PhotoData pd2 = new PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_LINK, 0);
								pd2.urlBigger = a.link.url;
								photos.add(pd2);
							}
							h.linkContainer.setVisibility(View.VISIBLE);
							h.linkPrimary.setText(a.link.title);
							h.linkSecondary.setText(a.link.url);
							break;

						case Attachment.TYPE_PAGE:
							h.linkContainer.setVisibility(View.VISIBLE);
							h.linkPrimary.setText(a.page.title);
							h.linkSecondary.setText(h.linkContainer.getContext().getString(R.string.page));
							break;
						default: {

						}

					}


				}

			}

			for (Photo photo : item.photosPersist) {
				photos.add(new PhotoData(photo.photo_604, photo.width, photo.height, PhotoData.TYPE_PHOTO, photo.id));
			}

			if (photos.size() != 0) {
				h.flow.setVisibility(View.VISIBLE);
				loadImages(h.flow, h.getViewsCache(), photos, getItemId(p));
			} else
				h.flow.setVisibility(View.GONE);

			// animation
			View v = h.card;
			if (initTime + 500 < System.currentTimeMillis() && p > previousPostition) {
				int speed = (int) toDips(v.getContext(), scrollListener.getSpeed());
				Utils.dp(v.getContext(), scrollListener.getSpeed());

				animDuration = (long) (ANIM_DEFAULT_SPEED / (speed / 10f + 1));

				L.v("speed=%s", speed);
				v.setTranslationY(Utils.dp(v.getContext(), 200));
				/*v.setRotationX(40.0F);
				v.setScaleX(0.8F);
				v.setScaleY(0.55F);
*/
				ViewPropertyAnimator a = v.animate()
						.translationY(0)
				                          /*.rotationX(0.0F)
				                          .rotationY(0.0F)
				                          .scaleX(1.0F)
				                          .scaleY(1.0F)*/
						.setDuration(animDuration)
						.setInterpolator(interpolator);
					/*if (Build.VERSION.SDK_INT >= 16)
						a.withLayer();*/

				a.start();
			}

			previousPostition = p;

		}


		public static void loadImages(final FlowLayout flow, Iterator<View> imagesIterator, final List<PhotoData> photos, long itemId) {
			//L.v("item id "+itemId);
			flow.removeAllViews();
			Utils.toggleView(flow, photos.size() != 0);
			DisplayMetrics displayMetrics = App.instance().getResources().getDisplayMetrics();
			int n = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) - Utils.dp(App.instance(), 28);
			LayoutUtils.processThumbs(n, n, photos);

			if (photos.size() != 0) {
				for (PhotoData photo : photos) {
					FixedSizeImageView img;
					if (imagesIterator.hasNext())
						img = (FixedSizeImageView) imagesIterator.next();
					else {
						L.e("cache is empty. creating view manually...");
						img = new FixedSizeImageView(flow.getContext());
					}
					img.displayW = photo.width;
					img.displayH = photo.height;
					ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;// : ImageView.ScaleType.FIT_CENTER;
					img.setScaleType(scaleType);
					img.toggleVideoIcon(photo.type == PhotoData.TYPE_VIDEO);
					//L.v("w=%s h=%s b=%s f=%s", photo.width, photo.height, photo.breakAfter, photo.floating);
					FlowLayout.LayoutParams lp = new FlowLayout.LayoutParams(Utils.dp(App.instance(), 2), Utils.dp(App.instance(), /*photo.paddingBottom ? 10 : */2));
					if (photo.breakAfter || photo.floating) {
						lp.breakAfter = photo.breakAfter;
						lp.floating = photo.floating;
					}

					img.setLayoutParams(lp);
					flow.addView(img);
					Picasso.with(flow.getContext()).load(photo.url).placeholder(img.placeholder).into(img);
					img.setOnClickListener(v -> {
						if (photo.type == PhotoData.TYPE_PHOTO) {
							Intent i = new Intent(v.getContext(), PhotosActivity.class);
							i.putExtra(Constants.KEY_POST_ID, itemId);
							i.putExtra(Constants.KEY_PHOTO_ID, photo.id);
							v.getContext().startActivity(i);
						} else if (photo.type == PhotoData.TYPE_VIDEO) {
							openVideo(v.getContext(), photo.id);
						} else if (photo.type == PhotoData.TYPE_LINK) {
							L.v("link click");
							String url = photo.urlBigger;
							if (url.startsWith("http"))
								EventBus.post(new UrlClickEvent(Collections.singletonList(url)));
						}
					});
				}

			}
		}


		private static void openVideo(Context c, final long id) {
			Video v = DBHelper.instance().fetchVideo((int) id);
			Observable<String> videoRequest = RestService.get().getVideo(v);
			videoRequest.subscribe(result -> {
				//T.show(App.instance(), "Success");
				c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
			}, e -> {
				T.show(App.instance(), "Fail!");
				e.printStackTrace();
			});

		}


		private float toDips(Context ctx, float px) {
			return px / ctx.getResources().getDisplayMetrics().density;
		}


		@Override
		public int getItemCount() {
			return data != null ? data.size() : 0;
		}


		public void addToEnd(final List<News> newData) {
			int total = getItemCount();
			if (data != null) {
				if (data.size() == 0)
					initTime = System.currentTimeMillis();
				data.addAll(newData);
			} else
				data = new ArrayList<>(newData);

			notifyItemRangeInserted(total, newData.size());

		}


		@Override
		public long getItemId(final int position) {
			return data.get(position).post_id;
		}


		public News getItem(final int i) {
			return data.get(i);
		}


		public void setItems(final List<News> list) {
			data = list;
			notifyDataSetChanged();
		}


	}


	public static class Holder extends RecyclerView.ViewHolder {

		/*@InjectView(R.id.image)
		public FillImageView mainImage;*/
		@InjectView(R.id.avatar)
		public ImageView avatar;
		@InjectView(R.id.repost_avatar)
		public ImageView repostAvatar;
		@InjectView(R.id.text)
		public TextView text;
		@InjectView(R.id.title)
		public TextView title;
		@InjectView(R.id.repost_title)
		public TextView repostTitle;
		@InjectView(R.id.repost_date)
		public TextView repostDate;
		@InjectView(R.id.likes)
		public CheckedTextView likes;
		@InjectView(R.id.comments)
		public CheckedTextView comments;
		@InjectView(R.id.reposts)
		public CheckedTextView reposts;
		@InjectView(R.id.date)
		public TextView date;
		@InjectView(R.id.signer)
		public TextView signer;
		@InjectView(R.id.link_primary)
		public TextView linkPrimary;
		@InjectView(R.id.link_secondary)
		public TextView linkSecondary;
		@InjectView(R.id.expand)
		public TextView expand;
		@InjectView(R.id.link)
		public View linkContainer;
		@InjectView(R.id.overflow)
		public ImageView overflow;
		@InjectView(R.id.header)
		public View header;
		@InjectView(R.id.repost_header)
		public View repostHeader;
		@InjectView(R.id.card)
		public View card;
		@InjectView(R.id.flow)
		public FlowLayout flow;

		private NewsRecyclerAdapter adapter;
		private List<View> cache;


		public Holder(final View v, NewsRecyclerAdapter a) {
			super(v);
			ButterKnife.inject(this, v);
			adapter = a;

			comments.setOnClickListener(this::onClick);
			text.setOnClickListener(this::onTextClick);
			expand.setOnClickListener(this::onTextClick);
			likes.setOnClickListener(this::onLikeClick);
			comments.setOnClickListener(this::onClick);
			reposts.setOnClickListener(this::onClick);
			//mainImage.setOnClickListener(this::onClick);
			linkContainer.setOnClickListener(this::onLinkClick);
			title.setOnClickListener(title -> {
				L.v("data size=%s pos=%s", adapter.data.size(), getPosition());
			});

			overflow.setOnClickListener(view -> {
				ListPopupWindow popup = new ListPopupWindow(view.getContext());
				popup.setAnchorView(view);
				popup.setContentWidth(Utils.dp(view.getContext(), 200));

				popup.setAdapter(ArrayAdapter.createFromResource(view.getContext(), R.array.overflow_items, android.R.layout.simple_list_item_1));
				popup.setOnItemClickListener((parent, view2, position, id) -> {
					switch (position) {
						case 0: // debug
							L.v("item=" + getItem().toString());
							break;
						case 1:

							break;
					}
					popup.dismiss();
				});
				popup.show();
			});
		}


		private void onLikeClick(final View view) {
			News item = getItem();
			RestService.get().likePost(item.post_id, item.source_id, !item.likesUserLikes).subscribe(likes -> {
				//T.show(getActivity(), "done");
				item.likesCount = likes.likes;
				item.likesUserLikes = !item.likesUserLikes;
				adapter.notifyItemChanged(getPosition());
			}, err -> T.show(view.getContext(), view.getContext().getString(R.string.fail)));

		}


		private void onLinkClick(final View view) {
			String url = linkSecondary.getText().toString();
			if (url.startsWith("http"))
				EventBus.post(new UrlClickEvent(Collections.singletonList(url)));
			else
				T.show(view.getContext(), "Not implemented yet");

		}


		private void onTextClick(final View view) {

			News i = getItem();
			L.v("text click %s isExpand=%s", getPosition(), i.isExpanded);
			//L.v(RestService.get().gson.toJson(i));
			//if (i.isExpanded != null) {
			L.v("notify");
			i.isExpanded = !i.isExpanded;
			adapter.notifyItemChanged(getPosition());
			//notifyDataSetChanged();
			//onBindViewHolder(this, getPosition());
			//}
		}


		private void onClick(final View view) {
			L.v("%s %s", getItemId(), getPosition());
			EventBus.post(new ClickEvent(getItem(), view.getId()));
		}


		private News getItem() {
			return adapter.data.get(getPosition());
		}


		public Iterator<View> getViewsCache() {
			if (cache == null) {
				cache = new ArrayList<>();
				for (int i = 0; i <= 10; i++) {
					cache.add(new FixedSizeImageView(flow.getContext()));
				}
			}
			return cache.iterator();
		}
	}
}
