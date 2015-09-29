package ds.vkplus.model;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import ds.vkplus.db.extras.AndroidDao;
import ds.vkplus.utils.L;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.sql.SQLException;
import java.util.List;

import static ds.vkplus.model.Filter.State.*;

public class FiltersDao extends AndroidDao<Filter, Integer> {

	public FiltersDao(final Class dataClass) throws SQLException {
		super(dataClass);
	}


	public FiltersDao(final ConnectionSource connectionSource, final Class<Filter> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}


	public FiltersDao(final ConnectionSource connectionSource, final DatabaseTableConfig<Filter> tableConfig) throws SQLException {
		super(connectionSource, tableConfig);
	}


	public List<Filter> fetchActiveFilters(final int type) {
		try {
			return queryBuilder().where()
			                     .eq("state", CHECKED)
			                     .and()
			                     .isNotNull("parent_id")
			                     .and()
			                     .eq("filterType", type)
			                     .and()
			                     .isNotNull("condition")
			                     .query();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Call when sub item state changed (or sub item added)
	 *
	 * @param sub
	 */
	public void onSubItemStateChanged(final Filter sub) throws SQLException {
		Filter parent = sub.parent;
		switch (parent.mode) {

			case Filter.MODE_RADIO:
				parent.unfolded = true;
				Observable.from(parent.subItems)
				          .filter(new Func1<Filter, Boolean>() {
					          @Override
					          public Boolean call(final Filter i) {return sub != i;}
				          })
				          .subscribe(new Action1<Filter>() {
					          @Override
					          public void call(final Filter i) {
						          L.INSTANCE$.v("filter=" + i.title);
						          i.state = UNCHECKED;
						          try {
							          update(i);
						          } catch (SQLException e) {
							          e.printStackTrace();
						          }
					          }
				          });
				break;

			case Filter.MODE_CHECK:
				if (Observable.from(parent.subItems).all(new Func1<Filter, Boolean>() {
					@Override
					public Boolean call(final Filter i) {return i.state == CHECKED;}
				}).toBlocking().last()) {
					parent.state = CHECKED;
					parent.unfolded = true;
				} else if (Observable.from(parent.subItems).all(new Func1<Filter, Boolean>() {
					@Override
					public Boolean call(final Filter i) {return i.state == UNCHECKED;}
				}).toBlocking().last()) {
					parent.state = UNCHECKED;
					parent.unfolded = false;
				} else {
					parent.state = HALF;
					parent.unfolded = true;
				}

				break;
		}

		update(parent);

	}


	public void toggleState(Filter item) throws SQLException {
		final Filter.State s = item.state == CHECKED ? UNCHECKED : CHECKED;
		if (item.parent != null) {
			item.state = item.parent.mode == Filter.MODE_RADIO ? CHECKED : s;
			update(item);
			onSubItemStateChanged(item);
		} else {
			item.state = s;
			Observable.from(item.subItems)
			          .subscribe(new Action1<Filter>() {
				          @Override
				          public void call(final Filter sub) {
					          sub.state = s;
					          try {
						          update(sub);
					          } catch (SQLException e) {
						          e.printStackTrace();
					          }
				          }
			          });

			update(item);
		}



	}


	public List<Filter> fetchFilters(final int filterType) {
		try {
			return queryBuilder().where()
			                     .eq("filterType", filterType)
			                     .and()
			                     .isNull("parent_id")
			                     .query();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public void deleteAll() throws SQLException {
		delete(queryForAll());
		clearObjectCache();
	}
}
