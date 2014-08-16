/*
package ds.vkplus.actionprovider;

import rx.Observable;

import java.util.ArrayList;
import java.util.List;

public class FilterItem extends FilterItemBase {

	public static final int MODE_CHECK = 0;
	public static final int MODE_RADIO = 1;

	private List<FilterSubItem> subItems = new ArrayList<>();
	private State state = State.UNCHECKED;
	public int mode;
	public boolean unfolded;


	public FilterItem(String t, final int mode) {
		title = t;
		this.mode = mode;
		id = generateId();
	}


	public List<FilterSubItem> getActiveFilters() {
		return Observable.from(subItems).filter(i -> i.getState()).toList().toBlocking().last();
	}


	public void add(final FilterSubItem sub) {
		sub.parent = this;
		subItems.add(sub);
		onSubItemStateChanged(sub);
	}


	*/
/**
	 * Call when sub item state changed (or sub item added)
	 *
	 * @param sub
	 *//*

	public void onSubItemStateChanged(final FilterSubItem sub) {
		switch (mode) {
			case MODE_CHECK:

				break;

			case MODE_RADIO:
				if (sub.getState())
					Observable.from(subItems).filter(i -> sub != i).doOnNext(i -> i.setState(false)).subscribe();
				break;

		}

		if (Observable.from(subItems).all(i -> i.getState()).toBlocking().last()) {
			state = State.CHECKED;
			unfolded = true;
		} else if (Observable.from(subItems).all(i -> !i.getState()).toBlocking().last()) {
			state = State.UNCHECKED;
			unfolded = false;
		} else {
			state = State.HALF;
			unfolded = true;
		}
	}


	public List<FilterSubItem> getSubItems() {
		return subItems;
	}


	public void setState(final int state) {
		this.state = State.values()[state];
		switch (this.state) {
			case CHECKED:
				Observable.from(subItems).doOnNext(sub -> sub.setState(true)).subscribe();
				break;
			case UNCHECKED:
				Observable.from(subItems).doOnNext(sub -> sub.setState(false)).subscribe();
				break;

		}
	}


	public State getState() {
		return state;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	static enum State {
		CHECKED, HALF, UNCHECKED
	}
}
*/
