package ds.vkplus.eventbus.events

import ds.vkplus.model.News

class ClickEvent {
	var id: Long = 0
	lateinit var item: News
	var viewId: Int = 0
	lateinit var url: String
	
	
	constructor(id: Long, viewId: Int) {
		this.id = id
		this.viewId = viewId
	}
	
	constructor(item: News, viewId: Int) {
		this.item = item
		this.viewId = viewId
	}
	
	constructor(url: String, viewId: Int) {
		this.url = url
		this.viewId = viewId
	}
}
