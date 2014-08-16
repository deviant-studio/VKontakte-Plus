package ds.vkplus.eventbus.events;

import ds.vkplus.model.Filter;

import java.util.List;

public class FilterEvent {

	public final List<Filter> filters;


	public FilterEvent(final List<Filter> filters) {
		this.filters = filters;

	}
}
