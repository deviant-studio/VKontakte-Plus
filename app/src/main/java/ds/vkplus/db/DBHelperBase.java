package ds.vkplus.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ds.vkplus.db.extras.AndroidDao;
import ds.vkplus.model.*;
import ds.vkplus.utils.L;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import static ds.vkplus.model.Filter.State.CHECKED;
import static ds.vkplus.model.Filter.State.UNCHECKED;
import static ds.vkplus.model.Filter.TYPE_COMMENTS;
import static ds.vkplus.model.Filter.TYPE_POSTS;

abstract public class DBHelperBase extends OrmLiteSqliteOpenHelper {

	protected final static String DATABASE_NAME = "database.db";
	protected final static int DATABASE_VERSION = 55;

	public static final String FILTER_BY_GROUP = "By Group";
	public static final String FILTER_BY_ME = "Only mine";

	protected static Class[] classes = {
			News.class,
			Comment.class,
			Attachment.class,
			Link.class,
			Album.class,
			Audio.class,
			Document.class,
			Group.class,
			Page.class,
			Photo.class,
			Poll.class,
			PostedPhoto.class,
			Profile.class,
			Video.class,
			PostData.class,
			PhotoPost.class,
			//Filter.class
	};

	protected AndroidDao<News, Integer> newsDao;
	protected AndroidDao<Attachment, Integer> attachmentsDao;
	protected AndroidDao<Profile, Integer> profilesDao;
	protected AndroidDao<Group, Integer> groupsDao;
	public FiltersDao filtersDao;


	public DBHelperBase(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION/*, R.raw.ormlite_config*/);
		newsDao = getDao(News.class);
		attachmentsDao = getDao(Attachment.class);
		groupsDao = getDao(Group.class);
		profilesDao = getDao(Profile.class);
		filtersDao = getDao(Filter.class);
	}


/*	public DBHelperBase(final Context context, final String databaseName, final Object o, final int databaseVersion) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION*//*, R.raw.ormlite_config*//*);
	}*/


	@Override
	public <D extends Dao<T, ?>, T> D getDao(final Class<T> clazz) {
		final D dao;
		try {
			dao = super.getDao(clazz);
			dao.setObjectCache(true);
			return dao;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public void onCreate(final SQLiteDatabase database, final ConnectionSource connectionSource) {
		L.INSTANCE$.v("onCreate DB");
		try {
			for (Class cls : classes) {
				TableUtils.createTable(connectionSource, cls);
			}
			TableUtils.createTable(connectionSource, Filter.class);
			generateFilters();
		} catch (SQLException e) {
			L.INSTANCE$.e("Can't create database");
			e.printStackTrace();
		}
	}


	protected void generateFilters() throws SQLException {
		L.INSTANCE$.v("generating filters");
		filtersDao.callBatchTasks(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				// comments
				Filter byLikesCount = new Filter("By Likes", Filter.MODE_RADIO, TYPE_COMMENTS);
				filtersDao.create(byLikesCount);
				filtersDao.create(new Filter("All", null, CHECKED, TYPE_COMMENTS, byLikesCount));
				filtersDao.create(new Filter("1 Like", "likesCount>=1", UNCHECKED, TYPE_COMMENTS, byLikesCount));
				filtersDao.create(new Filter("5 Likes", "likesCount>=5", UNCHECKED, TYPE_COMMENTS, byLikesCount));
				filtersDao.create(new Filter("10 Likes", "likesCount>=10", UNCHECKED, TYPE_COMMENTS, byLikesCount));
				filtersDao.create(new Filter("25 Likes", "likesCount>=25", UNCHECKED, TYPE_COMMENTS, byLikesCount));
				filtersDao.create(new Filter("50 Likes", "likesCount>=50", UNCHECKED, TYPE_COMMENTS, byLikesCount));

				Filter byContent = new Filter("By Content", Filter.MODE_CHECK, TYPE_COMMENTS);
				filtersDao.create(byContent);
				filtersDao.create(new Filter("Has attachments", "EXISTS (SELECT * FROM attachment b WHERE b.comment_id=id)", UNCHECKED, TYPE_COMMENTS, byContent));
				filtersDao.create(new Filter("Replies", "reply_to_comment <> 0", UNCHECKED, TYPE_COMMENTS, byContent));
				//filtersDao.create(new Filter("Only mine", "from_id=" + myId, UNCHECKED, TYPE_COMMENTS, byContent));

				// posts
				byLikesCount = new Filter("By Likes", Filter.MODE_RADIO, TYPE_POSTS);
				filtersDao.create(byLikesCount);
				filtersDao.create(new Filter("All", null, CHECKED, TYPE_POSTS, byLikesCount));
				filtersDao.create(new Filter("10 Like", "likesCount>=10", UNCHECKED, TYPE_POSTS, byLikesCount));
				filtersDao.create(new Filter("100 Likes", "likesCount>=100", UNCHECKED, TYPE_POSTS, byLikesCount));
				filtersDao.create(new Filter("500 Likes", "likesCount>=500", UNCHECKED, TYPE_POSTS, byLikesCount));
				filtersDao.create(new Filter("1000 Likes", "likesCount>=1000", UNCHECKED, TYPE_POSTS, byLikesCount));

				Filter byGroup = new Filter(FILTER_BY_GROUP, Filter.MODE_RADIO, TYPE_POSTS);
				filtersDao.create(byGroup);
				filtersDao.create(new Filter("All", null, CHECKED, TYPE_POSTS, byGroup));

				return null;
			}
		});

	}


	public void refreshOnlyMineCommentFilter(long myId) {
		try {
			Filter parent = filtersDao.queryBuilder()
			                          .where()
			                          .eq("filterType", 0)
			                          .and()
			                          .eq("title", "By Content")
			                          .queryForFirst();
			String name = FILTER_BY_ME;
			Filter byMe = filtersDao.queryBuilder()
			                        .where()
			                        .eq("filterType", 0)
			                        .and()
			                        .eq("title", name)
			                        .queryForFirst();
			if (byMe != null) {
				byMe.condition = "from_id=" + myId;
				filtersDao.update(byMe);
			} else
				filtersDao.create(new Filter(name, "from_id=" + myId, UNCHECKED, TYPE_COMMENTS, parent));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}





	@Override
	public void onUpgrade(final SQLiteDatabase database, final ConnectionSource connectionSource, final int oldVersion, final int newVersion) {
		L.INSTANCE$.v("onUpgrade table");
		try {
			for (Class cls : classes) {
				TableUtils.dropTable(connectionSource, cls, true);
			}
			TableUtils.dropTable(connectionSource, Filter.class, true);
		} catch (SQLException e) {
			L.INSTANCE$.e("Can't drop table");
			e.printStackTrace();
		}

		// recreating tables
		onCreate(database, connectionSource);
	}


}
