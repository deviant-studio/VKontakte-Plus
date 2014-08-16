/*
package ds.vkplus.actionprovider;

public class FilterSubItem extends FilterItemBase {

	private boolean state;
	public FilterItem parent;


	public FilterSubItem(final String t, final String c, final boolean s) {
		title = t;
		condition = c;
		state = s;
		id = generateId();
	}


	public void setState(boolean state) {
		this.state = state;
	}


	public void toggleState() {
		setState(parent.mode == FilterItem.MODE_RADIO || !state);
		parent.onSubItemStateChanged(this);
	}


	public boolean getState() {
		return state;
	}
}
*/
