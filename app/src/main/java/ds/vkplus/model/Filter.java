package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DatabaseTable(daoClass = FiltersDao.class)
public class Filter extends BaseDaoEnabled {

	public static final int MODE_CHECK = 0;
	public static final int MODE_RADIO = 1;

	public static final int TYPE_COMMENTS = 0;
	public static final int TYPE_POSTS = 1;

	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public String title;
	@DatabaseField
	public String condition;

	@ForeignCollectionField(eager = true,maxEagerForeignCollectionLevel = 2)
	public Collection<Filter> subItems;// = new ArrayList<>();
	@DatabaseField(foreign = true/*, foreignAutoRefresh = true*/)
	public Filter parent;
	@DatabaseField
	public State state = State.UNCHECKED;
	@DatabaseField
	public int mode;
	@DatabaseField
	public int filterType;

	//@DatabaseField
	public boolean unfolded;

	private List<Filter> itemsList;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Filter(){}
	/**
	 * For Root Items
	 *
	 * @param t
	 * @param mode
	 */
	public Filter(String t, final int mode, int type) {
		title = t;
		filterType = type;
		this.mode = mode;
	}


	/**
	 * For subItems
	 * @param t
	 * @param c
	 * @param s
	 * @param type
	 * @param p
	 */
	public Filter(final String t, final String c, final State s, int type, Filter p) {
		title = t;
		condition = c;
		state = s;
		//parentId = p.id;
		parent=p;
		filterType = type;
		p.addSubItem(this);
	}

	public List<Filter> getSubItems() {
		if (itemsList == null)
			itemsList = new ArrayList<>(subItems);

		return itemsList;
	}


	public static enum State {
		CHECKED, HALF, UNCHECKED
	}





	private void addSubItem(final Filter filter) {
		if (subItems == null)
			subItems = new ArrayList<>();

		subItems.add(filter);

	}


}
