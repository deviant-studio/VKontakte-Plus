package ds.vkplus.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ds.vkplus.Constants;
import ds.vkplus.R;
import ds.vkplus.network.model.CommentsList;
import ds.vkplus.network.model.News;
import ds.vkplus.utils.L;
import rx.Subscriber;

import java.util.List;

public class CommentsFragment extends BaseFragment {

	private static final int PAGE_SIZE = 10;
	private int offset;


	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_comments, container, false);
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
			    public void onNext(final CommentsList news) {
				    if (news != null) {
					    //news.init();
					    fillView(news);
				    }

			    }
		    });

	}


	private void fillView(final CommentsList news) {
		L.v("comments: " + rest.gson.toJson(news));

	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	private static class CommentsAdapter extends ArrayAdapter<News> {

		public CommentsAdapter(final Context context, final List<News> objects) {
			super(context, 0, objects);
		}


		@Override
		public View getView(final int position, View v, final ViewGroup parent) {
			if (v == null)
				v = LayoutInflater.from(getContext()).inflate(R.layout.row_post, parent, false);

			News item = getItem(position);
			((TextView) v.findViewById(R.id.text)).setText(item.text);

			return v;
		}
	}
}
