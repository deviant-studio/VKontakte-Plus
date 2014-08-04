package ds.vkplus.eventbus.events;

import ds.vkplus.network.model.News;

public class ClickEvent {
	public long id;
	public News item;
	public int viewId;


	public ClickEvent(final long id, final int viewId) {
		this.id = id;
		this.viewId = viewId;
	}

	public ClickEvent(final News item, final int viewId) {
		this.item = item;
		this.viewId = viewId;
	}
}
