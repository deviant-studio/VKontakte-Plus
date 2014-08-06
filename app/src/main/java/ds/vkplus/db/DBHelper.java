package ds.vkplus.db;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.BaseDaoEnabled;
import ds.vkplus.App;
import ds.vkplus.Constants;
import ds.vkplus.Prefs;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;
import ds.vkplus.model.*;
import ds.vkplus.utils.L;
import hugo.weaving.DebugLog;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class DBHelper extends DBHelperBase {

	//private static DBHelper instance;


	public DBHelper(final Context context) {
		super(context);
	}


	public static DBHelper instance() {
		/*if (instance == null)
			instance = OpenHelperManager.getHelper(App.instance(), DBHelper.class);

		return instance;*/
		return OpenHelperManager.getHelper(App.instance(), DBHelper.class);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@DebugLog
	public void saveNews(final NewsResponse news) {

		try {
			L.v("saving profiles...");
			saveEntities(news.profiles, profilesDao);
			L.v("saving groups...");
			saveEntities(news.groups, groupsDao);

			newsDao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {

					L.v("saving news...");
					Prefs.get().edit().putString(Constants.KEY_NEXT, news.next_from).commit();
					for (News item : news.items) {
						if (item.text.length() > NewsResponse.POST_LENGTH_THRESHOLD) {
							item.isExpanded = Boolean.FALSE;
						}

						item.likesCount = item.likes.count;
						item.likesUserLikes = item.likes.user_likes > 0;
						item.likesCanLike = item.likes.can_like > 0;
						item.commentsCount = item.comments.count;
						item.commentsCanPost = item.comments.can_post > 0;
						item.repostsCount = item.reposts.count;
						newsDao.createOrUpdate(item);

						//saveEntities(item.attachments, attachmentsDao);
						if (item.attachments != null)
							for (Attachment a : item.attachments) {
								saveAttachment(a);
							}
					}
					return null;
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}


	private void saveAttachment(final Attachment a) throws SQLException {
		attachmentsDao.create(a);
		Dao dao=getDao(a.getContent().getClass());
		dao.createOrUpdate(a.getContent());
	}


	private <T extends BaseDaoEnabled> void saveEntities(final Collection<T> list, AndroidBaseDaoImpl<T, Integer> dao) throws SQLException {
		if (list == null || dao == null)
			return;

		dao.callBatchTasks(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				for (T e : list) {
					dao.createOrUpdate(e);
				}
				return null;
			}
		});

	}


	public List<News> fetchNews() {
		try {
			List<News> news = newsDao.queryForAll();
			return news;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Producer getProducerById(final long id) throws SQLException {
		Producer p = groupsDao.queryBuilder().where().eq("id", Math.abs(id)).queryForFirst();
		if (p == null)
			p = profilesDao.queryBuilder().where().eq("id", Math.abs(id)).queryForFirst();
		if (p == null) {
			L.e("cant attach producer");
			p = new UnknownProducer();
		}

		return p;
	}
}
