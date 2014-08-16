/*
package ds.vkplus.actionprovider;

import rx.Observable;

import java.util.List;

public class FiltersCache {

	private static FiltersCache instance;
	private List<FilterItem> commentsFilters;


	public static FiltersCache getInstance() {
		if (instance == null)
			instance = new FiltersCache();

		return instance;
	}


	public List<FilterItem> getCommentsFilters() {
		return commentsFilters;
	}


	public void setCommentsFilters(List<FilterItem> f) {
		commentsFilters = f;
	}


	public List<FilterSubItem> getActives(List<FilterItem> list) {
		return Observable.from(list).mergeMapIterable(FilterItem::getActiveFilters).toList().toBlocking().last();
	}
}
*/
