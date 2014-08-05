package ds.vkplus.network.model;

import ds.vkplus.utils.L;
import rx.Observable;

import java.util.List;

public class CommentsList extends VKList<Comment> {

	public List<Profile> profiles;
	public List<Group> groups;


	public void init() {
		L.v("generation item<->producer relation");
		Observable.from(items).subscribe(i -> i.producer = getProducerById(Math.abs(i.from_id)));
	}


	private Producer getProducerById(final long id) {
		final Producer[] result = new Producer[1];
		Observable.concat(Observable.from(groups), Observable.from(profiles))
		          .first(i -> i.getId() == id)
		          .subscribe(i -> result[0] = i, e -> L.e("producer not found!"));
		return result[0];
	}

}
