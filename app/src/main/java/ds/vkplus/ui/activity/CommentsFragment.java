package ds.vkplus.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.model.Attachment;
import ds.vkplus.model.Comment;
import ds.vkplus.model.CommentsList;
import ds.vkplus.model.PhotoData;
import ds.vkplus.ui.OnScrollBottomListener;
import ds.vkplus.ui.view.FixedSizeImageView;
import ds.vkplus.ui.view.FlowLayout;
import ds.vkplus.utils.L;
import ds.vkplus.utils.T;
import ds.vkplus.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

	public static final Pattern REPLY_PATTERN = Pattern.compile("\\[id\\d+\\|(.+)\\]");

	@InjectView(android.R.id.list)
	ListView list;

	@InjectView(android.R.id.empty)
	TextView empty;

	@InjectView(R.id.content)
	ViewGroup content;

	private static final int PAGE_SIZE = 10;
	private int offset;
	private boolean loadMore = false;
	private CommentsAdapter adapter;


	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_comments, container, false);
	}


	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);
		content.setVisibility(View.GONE);

		list.setOnScrollListener(new OnScrollBottomListener(s -> {
			/*if (loadMore)
				loadComments();*/
		}));

		//loadComments();
		adapter = new CommentsAdapter(getActivity(), new ArrayList<>());
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		loadAllComments();
	}


	@Override
	protected void onRefresh() {

	}


	@Override
	protected void onLoggedIn(final String token) {

	}


	private void loadComments() {
		long postId = getActivity().getIntent().getLongExtra(Constants.KEY_POST_ID, 0);
		long ownerId = getActivity().getIntent().getLongExtra(Constants.KEY_OWNER_ID, 0);
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

	}


	private void loadAllComments() {
		long postId = getActivity().getIntent().getLongExtra(Constants.KEY_POST_ID, 0);
		long ownerId = getActivity().getIntent().getLongExtra(Constants.KEY_OWNER_ID, 0);
		rest.getAllComments(postId, ownerId)
		    .observeOn(AndroidSchedulers.mainThread())
		    .subscribe(new Subscriber<List<Comment>>() {

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
		    });
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
					long ownerId = getActivity().getIntent().getLongExtra(Constants.KEY_OWNER_ID, 0);
					rest.likeComment(item.id, ownerId, !item.likesUserLikes).subscribe(likes -> {
						//T.show(getActivity(), "done");
						item.likesCount = likes.likes;
						item.likesUserLikes = !item.likesUserLikes;
						adapter.notifyDataSetChanged();
					}, e -> T.show(getActivity(), getString(R.string.fail)));
					break;
				case 1: // reply
					T.show(getActivity(), "reply not implemented yet");
					break;
			}
			popup.dismiss();
		});
		popup.show();

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
							imageUrl = Observable.from(a.video.photo_640, a.video.photo_320, a.video.photo_130).toBlocking().first(pic -> pic != null);
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
							break;
						case Attachment.TYPE_PAGE:
							h.link.setVisibility(View.VISIBLE);
							h.linkPrimary.setText(a.page.title);
							h.linkSecondary.setText(getContext().getString(R.string.page));
							break;
						default: {

						}

					}

				}

				NewsFragment.NewsRecyclerAdapter.loadImages(h.flow, h.getViewsCache(), photos, getItemId(position));

				// clicks

			}


			return v;
		}


		@Override
		public long getItemId(final int position) {
			return getItem(position).id;
		}


		public static class Holder {

			@InjectView(R.id.text)
			public TextView text;
			@InjectView(R.id.likes)
			public CheckedTextView likes;
			@InjectView(R.id.date)
			public TextView date;
			@InjectView(R.id.link)
			public View link;
			@InjectView(R.id.link_primary)
			public TextView linkPrimary;
			@InjectView(R.id.link_secondary)
			public TextView linkSecondary;
			@InjectView(R.id.flow)
			public FlowLayout flow;
			@InjectView(R.id.title)
			public TextView title;
			@InjectView(R.id.icon)
			public ImageView avatar;

			private List<View> cache;


			public Holder(final View v) {
				ButterKnife.inject(this, v);

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
