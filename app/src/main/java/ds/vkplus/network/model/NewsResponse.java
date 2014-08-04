package ds.vkplus.network.model;

import ds.vkplus.utils.L;
import rx.Observable;

import java.util.List;


public class NewsResponse {

	public List<News> items;
	public List<Profile> profiles;
	public List<Group> groups;
	public String next_from;


	// should call it after each json deserialization
	public void init() {
		L.v("generation item<->producer relation");
		for (News item : items) {
			item.producer = getProducerById(Math.abs(item.source_id));
		}
	}


	private Producer getProducerById(final long id) {
		final Producer[] result = new Producer[1];
		Observable.concat(Observable.from(groups), Observable.from(profiles))
		          .first(i -> i.getId() == id)
		          .subscribe(i -> result[0] = i, e -> L.e("producer not found!"));
		return result[0];
	}

}
