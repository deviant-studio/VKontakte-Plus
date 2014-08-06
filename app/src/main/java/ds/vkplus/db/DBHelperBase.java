package ds.vkplus.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;
import ds.vkplus.model.*;
import ds.vkplus.utils.L;

import java.sql.SQLException;

abstract public class DBHelperBase extends OrmLiteSqliteOpenHelper {

	protected final static String DATABASE_NAME = "database.db";
	protected final static int DATABASE_VERSION = 8;

	private static Class[] classes = {
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
			Video.class
	};

	protected AndroidBaseDaoImpl<News, Integer> newsDao;
	protected AndroidBaseDaoImpl<Attachment, Integer> attachmentsDao;
	protected AndroidBaseDaoImpl<Profile, Integer> profilesDao;
	protected AndroidBaseDaoImpl<Group, Integer> groupsDao;


	public DBHelperBase(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION/*, R.raw.ormlite_config*/);
		newsDao = getDao(News.class);
		attachmentsDao = getDao(Attachment.class);
		groupsDao = getDao(Group.class);
		profilesDao = getDao(Profile.class);
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
		try {
			for (Class cls : classes) {
				TableUtils.createTable(connectionSource, cls);
			}
		} catch (SQLException e) {
			L.e("Can't create database");
			e.printStackTrace();
		}
	}


	@Override
	public void onUpgrade(final SQLiteDatabase database, final ConnectionSource connectionSource, final int oldVersion, final int newVersion) {
		try {
			for (Class cls : classes) {
				TableUtils.dropTable(connectionSource, cls, true);
			}
		} catch (SQLException e) {
			L.e("Can't drop table");
			e.printStackTrace();
		}

		// recreating tables
		onCreate(database,connectionSource);
	}


}
