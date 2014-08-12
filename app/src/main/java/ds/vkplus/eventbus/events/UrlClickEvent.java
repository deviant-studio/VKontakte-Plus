package ds.vkplus.eventbus.events;

import java.util.List;

public class UrlClickEvent {

	public List<String> urls;


	public UrlClickEvent(List<String> urls) {
		this.urls = urls;
	}

}
