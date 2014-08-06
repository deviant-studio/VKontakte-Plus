package ds.vkplus.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.model.Attachment;
import ds.vkplus.model.Comment;
import ds.vkplus.model.CommentsList;
import ds.vkplus.ui.OnScrollBottomListener;
import ds.vkplus.utils.L;
import ds.vkplus.utils.Utils;
import rx.Subscriber;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentsFragment extends BaseFragment {

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
			if (loadMore)
				loadComments();
		}));
	}


	@Override
	protected void onLoggedIn(final String token) {
		loadComments();
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
					    comments.init();
					    offset += comments.items.size();
					    L.v("count=" + comments.count);
					    loadMore = comments.count > offset;
					    fillView(comments);
				    }

			    }
		    });

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


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	private static class CommentsAdapter extends ArrayAdapter<Comment> {

		private final Picasso picasso;


		public CommentsAdapter(final Context context, List<Comment> data) {
			super(context, 0, data);
			picasso = Picasso.with(context);
			//data=object;
		}


		@Override
		public View getView(final int position, View v, final ViewGroup parent) {
			if (v == null)
				v = LayoutInflater.from(getContext()).inflate(R.layout.row_comment, parent, false);

			Comment item = getItem(position);
			((TextView) v.findViewById(R.id.title)).setText(item.producer.getName());
			CharSequence text;
			Matcher m = REPLY_PATTERN.matcher(item.text);
			if (m.find()) {
				L.v("matches");
				String replace = m.replaceFirst("$1");
				L.v(replace);
				text = Utils.setSpanForSubstring(m.replaceFirst("$1"), m.group(1), getContext().getResources().getColor(R.color.gray1));
			} else
				text = item.text;
			((TextView) v.findViewById(R.id.text)).setText(text);
			((TextView) v.findViewById(R.id.likes)).setText(item.likes.count > 0 ? ("+" + String.valueOf(item.likes.count)) : "");
			((TextView) v.findViewById(R.id.date)).setText(DateUtils.getRelativeTimeSpanString(item.date * 1000));
			picasso.load(item.producer.getThumb()).into((ImageView) v.findViewById(R.id.icon));

			View linkContainer = v.findViewById(R.id.link);
			linkContainer.setVisibility(View.GONE);
			if (item.attachments != null)
				for (Attachment attachment : item.attachments) {
					if (attachment.type.equals(Attachment.TYPE_LINK)) {
						linkContainer.setVisibility(View.VISIBLE);
					} else if (attachment.type.equals(Attachment.TYPE_PAGE)) {
						linkContainer.setVisibility(View.VISIBLE);
						((TextView) linkContainer.findViewById(R.id.link_primary)).setText(attachment.page.title);
						((TextView) linkContainer.findViewById(R.id.link_secondary)).setText(getContext().getString(R.string.page));
					}
				}

			return v;
		}
	}
}
