package ds.vkplus.db;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import ds.vkplus.App;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;
import ds.vkplus.model.*;
import ds.vkplus.utils.L;
import hugo.weaving.DebugLog;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBHelper extends DBHelperBase {

	public static final Pattern NEXT_PATTERN = Pattern.compile("^(\\d+)/([-]?\\d+)_(\\d+)_\\d+$");

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
	public void saveNewsResponse(final NewsResponse news) {

		try {
			L.v("saving profiles...");
			saveEntities(news.profiles, profilesDao);
			L.v("saving groups...");
			saveEntities(news.groups, groupsDao);

			newsDao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {

					L.v("saving news...");

					for (News item : news.items) {
						saveNews(item, null);

						if (item.copy_history != null) {
							L.v("saveng nested post...");
							for (News nested : item.copy_history) {
								saveNews(nested, item);
							}
						}
					}


					return null;
				}
			});

			L.v("saving next value...");
			saveNext(news.items.get(news.items.size() - 1), news.next_from);

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}


	private void saveNews(News item, News parent) throws SQLException {
		if (item.post_type != null)
			item.type = item.post_type;

		if (item.type.equals(News.TYPE_POST)) {
			if (item.text.length() > NewsResponse.POST_LENGTH_THRESHOLD) {
				item.isExpanded = Boolean.FALSE;
			}

			if (parent != null) {
				item.parent = parent;
				item.post_id=item.id;

			} else {
				item.likesCount = item.likes.count;
				item.likesUserLikes = item.likes.user_likes > 0;
				item.likesCanLike = item.likes.can_like > 0;
				item.commentsCount = item.comments.count;
				item.commentsCanPost = item.comments.can_post > 0;
				item.repostsCount = item.reposts.count;
			}
		} else if (item.type.equals(News.TYPE_WALL_PHOTO)) {
			item.post_id=item.date;
			item.photosPersist = item.photos.items;
			for (Photo p : item.photosPersist) {
				p.news = item;    // important!
				getDao(Photo.class).createOrUpdate(p);
			}
		} else if (item.type.equals(News.TYPE_PHOTO)) {
			// todo
		}

		newsDao.createOrUpdate(item);

		//save attachments
		if (item.attachments != null)
			for (Attachment a : item.attachments) {
				a.news = item;    // important!
				saveAttachment(a);
			}
	}


	private void saveNext(final News last, final String next) throws SQLException {
		final PostData data = new PostData();
		Matcher m = NEXT_PATTERN.matcher(next);
		if (m.matches()) {
			data.lastIndex = Integer.valueOf(m.group(1));
		}
		data.postId = last.post_id;
		data.postDate = last.date;
		data.nextRaw = next;
		data.fetchDate = System.currentTimeMillis() / 1000;
		data.total = fetchNewsCount();
		getDao(PostData.class).createOrUpdate(data);
	}


	public int fetchNewsCount() {
		try {
			return (int) newsDao.queryBuilder().where()
			                    .isNull("parent_id")
			                    .countOf();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}


	@DebugLog
	public PostData fetchLatestNext() {
		try {
			Dao<PostData, ?> dao = getDao(PostData.class);
			QueryBuilder<PostData, ?> qb = dao.queryBuilder().orderBy("fetchDate", false);
			return qb.queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	@DebugLog
	public PostData fetchNextByPostId(long postId) {
		try {
			L.v("fetchNextByPostId " + postId);
			Dao<PostData, ?> dao = getDao(PostData.class);
			return dao.queryBuilder().where().eq("postId", postId).queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	private void saveAttachment(final Attachment a) {
		if (a.getContent() == null)   // actually this shouldnt happen
			return;

		try {
			attachmentsDao.create(a);
			Dao dao = getDao(a.getContent().getClass());
			dao.createOrUpdate(a.getContent());
		} catch (SQLException e) {
			e.printStackTrace();
		}
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


	public List<News> fetchAllNews() {
		try {
			List<News> news = newsDao.queryBuilder()
			                         .orderBy("date", false)
			                         .where()
			                         .isNull("parent_id")
			                         .query();
			return news;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public List<News> fetchNews(long byDate) {
		if (byDate == -1)
			return fetchAllNews();

		try {
			List<News> news = newsDao.queryBuilder()
			                         .orderBy("date", false)
			                         .where()
			                         .lt("date", byDate)
			                         .isNull("parent_id")
			                         .and(2)
			                         .query();
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


	public void dropAll() throws SQLException {
		for (Class cls : classes) {
			TableUtils.dropTable(connectionSource, cls, true);
			TableUtils.createTable(connectionSource, cls);
		}
	}
}
