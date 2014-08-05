package ds.vkplus.network.model;

import ds.vkplus.utils.L;
import rx.Observable;

import java.util.List;


public class NewsResponse {

	public static final int POST_LENGTH_THRESHOLD = 128;

	public List<News> items;
	public List<Profile> profiles;
	public List<Group> groups;
	public String next_from;


	// should call it after each json deserialization
	public void init() {
		L.v("generation item<->producer relation");

		for (News item : items) {

			if (item.copy_history != null) {
				item.text = item.copy_history[0].text;
				item.attachments = item.copy_history[0].attachments;
			}

			item.producer = getProducerById(Math.abs(item.source_id));
			item.signer = getProducerById(Math.abs(item.signer_id));

			if (item.text.length() > POST_LENGTH_THRESHOLD) {
				item.isExpanded = Boolean.FALSE;
			}


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
