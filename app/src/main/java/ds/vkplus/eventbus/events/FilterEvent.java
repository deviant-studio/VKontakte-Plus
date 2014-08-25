package ds.vkplus.eventbus.events;

import ds.vkplus.model.Filter;

public class FilterEvent {

	public final Filter filter;


	public FilterEvent(Filter filter) {
		this.filter = filter;

	}
}
