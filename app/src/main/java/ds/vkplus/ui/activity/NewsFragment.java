package ds.vkplus.ui.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import ds.vkplus.App;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.eventbus.EventBus;
import ds.vkplus.eventbus.events.ClickEvent;
import ds.vkplus.network.RestService;
import ds.vkplus.network.model.Attachment;
import ds.vkplus.network.model.News;
import ds.vkplus.network.model.NewsResponse;
import ds.vkplus.ui.OnScrollBottomRecyclerViewListener;
import ds.vkplus.utils.L;
import ds.vkplus.utils.T;
import ds.vkplus.utils.Utils;
import rx.Subscriber;

public class NewsFragment extends BaseFragment {

	private static final int PAGE_SIZE = 10;

	@InjectView(R.id.list)
	RecyclerView recyclerView;

	@InjectView(android.R.id.empty)
	TextView empty;


	String token;
	String next;
	NewsRecyclerAdapter adapter;
	private RecyclerView.LayoutManager mLayoutManager;


	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_news, null);
	}


	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		ButterKnife.inject(this, view);

		recyclerView.setHasFixedSize(true);

		recyclerView.setItemAnimator(new DefaultItemAnimator());
		mLayoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setOnScrollListener(new OnScrollBottomRecyclerViewListener((LinearLayoutManager) mLayoutManager, success -> loadMoreNews(next)));

		super.onViewCreated(view, savedInstanceState);
	}


	private void loadNews() {
		loadMoreNews(null);
	}


	private void loadMoreNews(final String next) {
		rest.getNews(next, PAGE_SIZE)
				//.subscribe(this::fillView, e -> T.show(getActivity(), "error getting news"));
				.subscribe(new Subscriber<NewsResponse>() {

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
					public void onNext(final NewsResponse news) {
						if (news != null) {
							news.init();
							fillView(news);
						}

					}
				});
	}


	@Override
	protected void onLoggedIn(String token) {
		this.token = token;
		loadNews();
	}


	private void fillView(final NewsResponse news) {
		if (news.items.size() != 0)
			empty.setVisibility(View.GONE);

		next = news.next_from;
		L.v("next: " + news.next_from);
		if (adapter == null) {
			adapter = new NewsRecyclerAdapter(news);
			recyclerView.setAdapter(adapter);

		} else {
			adapter.add(news);
		}
	}


	@Subscribe
	public void onClickEvent(ClickEvent e) {
		//T.show(getActivity(), "clicked %s %s", e.id, e.viewId);
		News item = e.item;
		switch (e.viewId) {
			case R.id.comments:
				if (item.comments.count > 0) {
					Intent i = new Intent(getActivity(), CommentsActivity.class);
					i.putExtra(Constants.KEY_POST_ID, item.post_id);
					i.putExtra(Constants.KEY_OWNER_ID, item.source_id);
					startActivity(i);
				}
				break;

			case R.id.link:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(e.url)));
				break;
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.Holder> {

		private NewsResponse data;
		Picasso picasso;


		private NewsRecyclerAdapter(NewsResponse data) {
			this.data = data;
			picasso = Picasso.with(App.instance());
			setHasStableIds(true);
		}


		@Override
		public Holder onCreateViewHolder(final ViewGroup parent, final int i) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
			Holder h = new Holder(v);
			return h;
		}


		@Override
		public void onBindViewHolder(final Holder h, final int p) {
			//L.v("id "+getItemId(p));
			News item = data.items.get(p);

			Utils.toggleView(h.likes, item.likes.can_like > 0);
			Utils.toggleView(h.comments, item.comments.can_post > 0);
			Utils.toggleView(h.text, !TextUtils.isEmpty(item.text));
			h.date.setText(DateUtils.getRelativeTimeSpanString(item.date * 1000));
			h.comments.setText(String.valueOf(item.comments.count));
			h.likes.setText(String.valueOf(item.likes.count));
			h.reposts.setText(String.valueOf(item.reposts.count));
			if (item.producer != null) {
				h.title.setText(item.producer.getName());
				picasso.load(item.producer.getThumb()).into(h.avatar);
			}

			// crop text
			Utils.toggleView(h.expand, item.isExpanded != null && !item.isExpanded);
			h.text.setText(item.isExpanded != null && !item.isExpanded ? item.text.substring(0, NewsResponse.POST_LENGTH_THRESHOLD) : item.text);

			// fill signer if exist
			if (item.signer != null) {
				h.signer.setVisibility(View.VISIBLE);
				h.signer.setText(item.signer.getName());
			} else {
				h.signer.setVisibility(View.GONE);
			}

			h.likes.setChecked(item.likes.user_likes > 0);

			h.mainImage.setVisibility(View.GONE);
			h.linkContainer.setVisibility(View.GONE);

			if (item.attachments != null) {
				for (Attachment a : item.attachments) {
					String imageUrl = null;
					switch (a.type) {
						case Attachment.TYPE_PHOTO:
							imageUrl = a.photo.photo_604;
							break;
						case Attachment.TYPE_VIDEO:
							imageUrl = a.video.photo_640;
							break;
						case Attachment.TYPE_POSTED_PHOTO:
							imageUrl = a.posted_photo.photo_604;
							break;
						case Attachment.TYPE_LINK:
							//imageUrl = a.link.image_src;
							h.linkContainer.setVisibility(View.VISIBLE);
							h.linkPrimary.setText(a.link.title);
							h.linkSecondary.setText(a.link.url);
							break;
						default:
							h.mainImage.setImageResource(R.drawable.ic_launcher);

					}

					if (!TextUtils.isEmpty(imageUrl)) {
						h.mainImage.setVisibility(View.VISIBLE);
						picasso.load(imageUrl).into(h.mainImage);
					}
				}
			}

		}


		@Override
		public int getItemCount() {
			return data.items != null ? data.items.size() : 0;
		}


		public void add(final NewsResponse newData) {
			int total = getItemCount();
			if (data != null) {
				data.items.addAll(newData.items);
				data.groups.addAll(newData.groups);
				data.profiles.addAll(newData.profiles);
			} else
				data = newData;

			notifyItemRangeInserted(total, newData.items.size());

		}


		@Override
		public long getItemId(final int position) {
			return data.items.get(position).post_id;
		}


		public class Holder extends RecyclerView.ViewHolder {

			@InjectView(R.id.image)
			public ImageView mainImage;
			@InjectView(R.id.avatar)
			public ImageView avatar;
			@InjectView(R.id.text)
			public TextView text;
			@InjectView(R.id.title)
			public TextView title;
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


			public Holder(final View v) {
				super(v);
				ButterKnife.inject(this, v);

				comments.setOnClickListener(this::onClick);
				text.setOnClickListener(this::onTextClick);
				expand.setOnClickListener(this::onTextClick);
				likes.setOnClickListener(this::onClick);
				comments.setOnClickListener(this::onClick);
				reposts.setOnClickListener(this::onClick);
				mainImage.setOnClickListener(this::onClick);
				linkContainer.setOnClickListener(this::onLinkClick);

				overflow.setOnClickListener(view -> {
					ListPopupWindow popup = new ListPopupWindow(view.getContext());
					popup.setAnchorView(view);
					popup.setContentWidth(Utils.dp(view.getContext(), 200));

					popup.setAdapter(ArrayAdapter.createFromResource(view.getContext(), R.array.overflow_items, android.R.layout.simple_list_item_1));
					popup.setOnItemClickListener((parent, view2, position, id) -> {
						switch (position) {
							case 0: // debug
								L.v("item=" + RestService.get().gson.toJson(getItem()));
								break;
							case 1:

								break;
						}
						popup.dismiss();
					});
					popup.show();
				});
			}


			private void onLinkClick(final View view) {
				String url = linkSecondary.getText().toString();
				if (url.startsWith("http"))
					EventBus.post(new ClickEvent(url, view.getId()));
				else
					T.show(view.getContext(), "Not implemented yet");

			}


			private void onTextClick(final View view) {
				News i = getItem();
				if (i.isExpanded != null) {
					i.isExpanded = !i.isExpanded;
					notifyItemChanged(getPosition());
					//text.setMaxLines(i.isExpanded ? 0 : POST_MAX_LINES);
					//Utils.toggleView(expand, !i.isExpanded);
				}
			}


			private void onClick(final View view) {
				L.v("%s %s", getItemId(), getPosition());
				EventBus.post(new ClickEvent(getItem(), view.getId()));
			}


			private News getItem() {
				return data.items.get(getPosition());
			}

		}
	}
}
