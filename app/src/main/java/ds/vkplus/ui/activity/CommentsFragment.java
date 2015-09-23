package ds.vkplus.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.*;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.actionprovider.FilterActionProvider;
import ds.vkplus.db.DBHelper;
import ds.vkplus.eventbus.events.FilterEvent;
import ds.vkplus.eventbus.events.UrlClickEvent;
import ds.vkplus.model.*;
import ds.vkplus.model.Filter;
import ds.vkplus.ui.OnScrollBottomListener;
import ds.vkplus.ui.view.FixedSizeImageView;
import ds.vkplus.ui.view.FlowLayout;
import ds.vkplus.utils.L;
import ds.vkplus.utils.T;
import ds.vkplus.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

	public static final Pattern REPLY_PATTERN = Pattern.compile("\\[id\\d+\\|(.+)\\]");

	@Bind(android.R.id.list)
	ListView list;

	@Bind(android.R.id.empty)
	TextView empty;

	@Bind(R.id.content)
	ViewGroup content;

	private static final int PAGE_SIZE = 10;
	private int offset;
	private boolean loadMore = false;
	private CommentsAdapter adapter;
	private Subscriber<List<Comment>> subscriber;
	private long postId;
	private long ownerId;


	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_comments, container, false);
	}


	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
		setHasOptionsMenu(true);
		content.setVisibility(View.GONE);

		postId = getActivity().getIntent().getLongExtra(Constants.KEY_POST_ID, 0);
		ownerId = getActivity().getIntent().getLongExtra(Constants.KEY_OWNER_ID, 0);

		list.setOnScrollListener(new OnScrollBottomListener(s -> {
			/*if (loadMore)
				loadComments();*/
		}));

		initUI();
	}


	private void initUI() {
		adapter = new CommentsAdapter(getActivity(), new ArrayList<>());
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		loadAllComments();
	}


	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.comments, menu);
		FilterActionProvider p = (FilterActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.filter));
		p.init(Filter.TYPE_COMMENTS);
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	protected void onRefresh() {
		initUI();
	}


	@Override
	protected void onLoggedIn(final String token) {

	}


	@Subscribe
	public void onFilterEvent(FilterEvent e) {
		//T.show(getActivity(),"on Filter");
		//FiltersCache.getInstance().setCommentsFilters(e.filters);
		List<Filter> actives = DBHelper.instance().filtersDao.fetchActiveFilters(Filter.TYPE_COMMENTS);
		fetchFiltered(actives);
	}


	private void fetchFiltered(final List<Filter> actives) {
		//DBHelper.instance().fetchCommentsFiltered

		if (subscriber != null && !subscriber.isUnsubscribed()) {
			L.v("filtered while loading");
			subscriber.unsubscribe();
			initUI();
		} else {
			// just fetch whole table
			L.v("filtered in idle mode");
			adapter.clear();
			Observable.create((Subscriber<? super List<Comment>> subscriber) -> {
				subscriber.onNext(DBHelper.instance().fetchComments(postId, 0, System.currentTimeMillis() / 1000, actives));
				subscriber.onCompleted();
			}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::fillView2, e -> e.printStackTrace());
		}
	}


	/*private void loadComments() {
		rest.getComments(postId, ownerId, offset, PAGE_SIZE)
		    .subscribe(new Subscriber<CommentsList>() {

			    @Override
			    public void onStart() {
				    toggleProgress(true);
			    }


			    @Override
			    public void onCompleted() {
				    toggleProgress(false);
				    loadComments();
			    }


			    @Override
			    public void onError(final Throwable e) {
				    L.e("error catched in fragment");
				    toggleProgress(false);
				    e.printStackTrace();
			    }


			    @Override
			    public void onNext(final CommentsList comments) {
				    if (comments != null) {
					    //comments.init();
					    offset += comments.items.size();
					    L.v("count=" + comments.count);
					    loadMore = comments.count > offset;
					    fillView(comments);
				    }

			    }
		    });

	}*/


	private void loadAllComments() {

		subscriber = new Subscriber<List<Comment>>() {

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
				toggleProgress(false);
				e.printStackTrace();
			}


			@Override
			public void onNext(final List<Comment> comments) {
				if (comments != null) {
					L.i("got %s comments", comments.size());
					fillView2(comments);
				}

			}
		};

		rest.getAllComments(postId, ownerId)
		    .observeOn(AndroidSchedulers.mainThread())
		    .subscribe(subscriber);
	}


	private void fillView2(final List<Comment> comments) {
		adapter.addAll(comments);
		if (comments.size() != 0) {
			empty.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
		}
	}


	private void fillView(final CommentsList comments) {
		L.v("comments: " + rest.gson.toJson(comments));

		if (comments.items.size() != 0) {
			empty.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
		}

		if (adapter == null) {
			adapter = new CommentsAdapter(getActivity(), comments.items);
			list.setAdapter(adapter);

		} else {
			adapter.addAll(comments.items);
		}
	}


	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		Comment item = (Comment) parent.getAdapter().getItem(position);
		showPopup(item, view);
	}


	private void showPopup(final Comment item, final View anchor) {
		ListPopupWindow popup = new ListPopupWindow(getActivity());
		popup.setAnchorView(anchor);
		popup.setContentWidth(Utils.dp(getActivity(), 200));

		popup.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.comment_popup_items, android.R.layout.simple_list_item_1));
		popup.setOnItemClickListener((parent, view2, position, id) -> {
			switch (position) {
				case 0: // like
					rest.likeComment(item.id, ownerId, !item.likesUserLikes).subscribe(likes -> {
						//T.show(getActivity(), "done");
						item.likesCount = likes.likes;
						item.likesUserLikes = !item.likesUserLikes;
						adapter.notifyDataSetChanged();
					}, e -> T.show(getActivity(), getString(R.string.fail)));
					break;
				case 1: // share
					T.show(getActivity(), "share not implemented yet");
					break;

				case 2: // reply
					T.show(getActivity(), "reply not implemented yet");
					break;
			}
			popup.dismiss();
		});
		popup.show();

	}


	@Subscribe
	public void onUrlClickEvent(UrlClickEvent e) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(e.urls.get(0))));
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static class CommentsAdapter extends ArrayAdapter<Comment> {

		private final Picasso picasso;


		public CommentsAdapter(final Context context, List<Comment> data) {
			super(context, 0, data);
			picasso = Picasso.with(context);
			//data=object;
		}


		@Override
		public View getView(final int position, View v, final ViewGroup parent) {
			Holder h;
			if (v == null) {
				v = LayoutInflater.from(getContext()).inflate(R.layout.row_comment, parent, false);
				h = new Holder(v);
				v.setTag(h);
			} else {
				h = (Holder) v.getTag();
			}

			Comment item = getItem(position);
			h.title.setText(item.getProducer().getName());

			CharSequence text;
			Matcher m = REPLY_PATTERN.matcher(item.text);
			if (m.find()) {
				L.v("matches");
				String replace = m.replaceFirst("$1");
				L.v(replace);
				text = Utils.setSpanForSubstring(m.replaceFirst("$1"), m.group(1), getContext().getResources().getColor(R.color.gray1));
			} else
				text = item.text;

			h.text.setText(text);
			Utils.toggleView(h.text, !TextUtils.isEmpty(text));
			h.likes.setText(item.likesCount > 0 ? ("+" + String.valueOf(item.likesCount)) : "");
			h.likes.setChecked(item.likesUserLikes);
			h.date.setText(DateUtils.getRelativeTimeSpanString(item.date * 1000));
			picasso.load(item.getProducer().getThumb()).into(h.avatar);

			h.link.setVisibility(View.GONE);
			h.flow.setVisibility(View.GONE);

			if (item.attachments != null) {
				List<PhotoData> photos = new ArrayList<>();
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
							imageUrl = Observable.just(a.video.photo_640, a.video.photo_320, a.video.photo_130).toBlocking().first(pic -> pic != null);
							PhotoData pd = new PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_VIDEO, a.video.id);
							photos.add(pd);
							break;
						case Attachment.TYPE_POSTED_PHOTO:
							imageUrl = a.posted_photo.photo_604;
							photos.add(new PhotoData(imageUrl, 0, 0, PhotoData.TYPE_PHOTO, a.posted_photo.id));
							break;
						case Attachment.TYPE_LINK:
							h.link.setVisibility(View.VISIBLE);
							h.linkPrimary.setText(a.link.title);
							h.linkSecondary.setText(a.link.url);
							h.link.setOnClickListener(clicked -> Utils.openURL(a.link.url));
							break;
						case Attachment.TYPE_DOC:
							imageUrl = a.doc.photo_130;
							PhotoData pd2 = new PhotoData(imageUrl, 1600, 1200, PhotoData.TYPE_LINK, 0);
							pd2.extra = a.doc.url;
							photos.add(pd2);
							break;
						case Attachment.TYPE_PAGE:
							h.link.setVisibility(View.VISIBLE);
							h.linkPrimary.setText(a.page.title);
							h.linkSecondary.setText(getContext().getString(R.string.page));
							//h.link.setOnClickListener(clicked->Utils.openURL(a.page.url));
							break;
						default: {

						}

					}

				}

				int size=-1;
				/*DisplayMetrics displayMetrics = App.instance().getResources().getDisplayMetrics();
				size = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) - Utils.dp(App.instance(), 28) / 8;*/
				NewsFragment.NewsRecyclerAdapter.loadImages(size, h.flow, h.getViewsCache(), photos, getItemId(position),true);

				// clicks

			}


			return v;
		}


		@Override
		public long getItemId(final int position) {
			return getItem(position).id;
		}


		public static class Holder {

			@Bind(R.id.text)
			public TextView text;
			@Bind(R.id.likes)
			public CheckedTextView likes;
			@Bind(R.id.date)
			public TextView date;
			@Bind(R.id.link)
			public View link;
			@Bind(R.id.link_primary)
			public TextView linkPrimary;
			@Bind(R.id.link_secondary)
			public TextView linkSecondary;
			@Bind(R.id.flow)
			public FlowLayout flow;
			@Bind(R.id.title)
			public TextView title;
			@Bind(R.id.icon)
			public ImageView avatar;

			private List<View> cache;


			public Holder(final View v) {
				ButterKnife.bind(this, v);

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
}
