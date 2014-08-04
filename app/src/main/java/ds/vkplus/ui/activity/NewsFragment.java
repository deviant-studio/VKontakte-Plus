package ds.vkplus.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import ds.vkplus.App;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.eventbus.EventBus;
import ds.vkplus.eventbus.events.ClickEvent;
import ds.vkplus.network.model.Attachment;
import ds.vkplus.network.model.News;
import ds.vkplus.network.model.NewsResponse;
import ds.vkplus.ui.OnScrollBottomRecyclerViewListener;
import ds.vkplus.utils.L;
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

		// use a linear layout manager
		//((LinearLayoutManager) mLayoutManager).
		//recyclerView.setItemAnimator(new DefaultItemAnimator());
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
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	private static class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.Holder> {

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
			if (item.copy_history != null) {
				item.text = item.copy_history[0].text;
				item.attachments = item.copy_history[0].attachments;
			}
			h.text.setText(item.text);
			h.text.setVisibility(TextUtils.isEmpty(item.text) ? View.GONE : View.VISIBLE);
			h.date.setText(DateUtils.getRelativeTimeSpanString(item.date * 1000));
			h.comments.setText(String.valueOf(item.comments.count));
			h.likes.setText(String.valueOf(item.likes.count));
			h.reposts.setText(String.valueOf(item.reposts.count));
			if (item.producer != null) {
				h.title.setText(item.producer.getName());
				picasso.load(item.producer.getThumb()).into(h.avatar);
			}

			if (item.attachments != null) {
				h.mainImage.setVisibility(View.VISIBLE);
				Attachment a = item.attachments.get(0);
				switch (a.type) {
					case Attachment.TYPE_PHOTO:
						picasso.load(a.photo.photo_604).into(h.mainImage);
						break;
					case Attachment.TYPE_VIDEO:
						picasso.load(a.video.photo_640).into(h.mainImage);
						break;
					case Attachment.TYPE_POSTED_PHOTO:
						picasso.load(a.posted_photo.photo_604).into(h.mainImage);
						break;
					case Attachment.TYPE_LINK:
						picasso.load(a.link.image_src).into(h.mainImage);
						h.text.setText(a.link.description);
						break;
					default:
						h.mainImage.setImageResource(R.drawable.ic_launcher);

				}
			} else
				h.mainImage.setVisibility(View.GONE);
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


		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


		public class Holder extends RecyclerView.ViewHolder {

			public ImageView mainImage;
			public ImageView avatar;
			public TextView text;
			public TextView title;
			public TextView likes;
			public TextView comments;
			public TextView reposts;
			public TextView date;


			public Holder(final View v) {
				super(v);
				text = (TextView) v.findViewById(R.id.text);
				title = (TextView) v.findViewById(R.id.title);
				likes = (TextView) v.findViewById(R.id.likes);
				comments = (TextView) v.findViewById(R.id.comments);
				reposts = (TextView) v.findViewById(R.id.reposts);
				date = (TextView) v.findViewById(R.id.date);
				mainImage = (ImageView) v.findViewById(R.id.image);
				avatar = (ImageView) v.findViewById(R.id.avatar);

				comments.setOnClickListener(this::onClick);
				text.setOnClickListener(this::onClick);
				likes.setOnClickListener(this::onClick);
				comments.setOnClickListener(this::onClick);
				reposts.setOnClickListener(this::onClick);
				mainImage.setOnClickListener(this::onClick);
			}


			private void onClick(final View view) {
				L.v("%s %s", getItemId(), getPosition());
				EventBus.post(new ClickEvent(data.items.get(getPosition()), view.getId()));
			}

		}
	}
}
