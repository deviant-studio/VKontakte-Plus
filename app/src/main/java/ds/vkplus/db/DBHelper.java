package ds.vkplus.db;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;
import ds.vkplus.App;
import ds.vkplus.db.extras.AndroidDao;
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

	public static final Pattern NEXT_PATTERN = Pattern.compile("^(\\d+)/(-?\\d+)(?:_\\d+)?_(\\d+)_\\d+$");

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

		if (news.items == null || news.items.size() == 0)
			return;

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
							L.v("saving nested post...");
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

		if (parent != null) {
			item.parent = parent;
			item.post_id = item.id;
			item.source_id = item.owner_id != 0 ? item.owner_id : item.from_id;

		}

		switch (item.type) {
			case News.TYPE_POST:
				/*if (item.text.length() > NewsResponse.POST_LENGTH_THRESHOLD) {
					item.isExpanded = false;
				}*/

				//L.v("post text=%s isExpanded=%s", item.text, item.isExpanded);

				if (parent == null) {
					item.likesCount = item.likes.count;
					item.likesUserLikes = item.likes.user_likes > 0;
					item.likesCanLike = item.likes.can_like > 0;
					item.commentsCount = item.comments.count;
					item.commentsCanPost = item.comments.can_post > 0;
					item.repostsCount = item.reposts.count;
				}


				break;

			case News.TYPE_WALL_PHOTO:
			case News.TYPE_PHOTO:
				item.post_id = item.date;
				if (item.photos != null) {
					item.photosPersist = item.photos.items;
					for (Photo p : item.photosPersist) {
						p.news = item;    // important!
						getDao(Photo.class).createOrUpdate(p);
					}
				}
				break;

		}

		newsDao.createOrUpdate(item);


		//L.v("isExpanded after fetch=%s", newsDao.queryForSameId(item).isExpanded);

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
		} else {
			L.e("next doesnt match regex pattern");
		}
		data.postId = last.post_id;
		data.postDate = last.date;
		data.nextRaw = next;
		data.fetchDate = System.currentTimeMillis();
		data.total = fetchNewsCount();
		getDao(PostData.class).createOrUpdate(data);
	}


	public int fetchNewsCount() {
		try {
			return (int) newsDao.queryBuilder()
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
	public PostData fetchOldestNext() {
		try {
			Dao<PostData, ?> dao = getDao(PostData.class);
			QueryBuilder<PostData, ?> qb = dao.queryBuilder().orderBy("postDate", true);
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


	public <T extends BaseDaoEnabled> void saveEntities(final Collection<T> list, AndroidDao<T, Integer> dao) throws SQLException {
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
		L.v("fetchAllNews");
		return fetchNews(System.currentTimeMillis() / 1000);
		/*try {
			List<News> news = newsDao.queryBuilder()
			                         .orderBy("date", false)
			                         .where()
			                         .isNull("parent_id")
			                         .query();

			L.v("size="+news.size());
			return news;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;*/
	}


	public List<News> fetchNews(long byDate) {
		/*if (byDate == -1)
			return fetchAllNews(filters);*/
		try {

			final List<Filter> filters = filtersDao.fetchActiveFilters(Filter.TYPE_POSTS);
			QueryBuilder<News, Integer> b = newsDao.queryBuilder().orderBy("date", false);
			Where<News, Integer> where = b.where()
			                              .lt("date", byDate)
			                              .and()
			                              .isNull("parent_id");

			PreparedQuery query;
			if (filters != null && filters.size() != 0) {
				L.v("fetch news with filters");
				for (Filter filter : filters) {
					where.and();
					whereBuilder(where, filter.condition);
				}
			}

			List<News> news = where.query();

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
			getDao(cls).clearObjectCache();
			TableUtils.dropTable(connectionSource, cls, true);
			TableUtils.createTable(connectionSource, cls);
		}
	}


	public News fetchLatestPost() {
		try {
			return newsDao.queryBuilder()
			              .orderBy("date", false)
			              .where()
			              .isNull("parent_id")
			              .queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public News fetchNewsById(final int id) {
		try {
			return newsDao.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public Video fetchVideo(final Integer id) {
		Dao<Video, Integer> dao = getDao(Video.class);
		try {
			return dao.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public void saveVideo(final Video video) {
		Dao<Video, Integer> dao = getDao(Video.class);
		try {
			dao.update(video);
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}


	public void saveCommentsResponse(final CommentsList comments, final long postId) {
		try {
			saveEntities(comments.groups, getDao(Group.class));
			saveEntities(comments.profiles, getDao(Profile.class));
			saveComments(comments.items, postId);

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}


	public void saveComments(final List<Comment> list, final long postId) throws SQLException {
		if (list == null)
			return;
		AndroidDao<Comment, Integer> dao = getDao(Comment.class);
		dao.callBatchTasks(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				for (Comment c : list) {
					c.likesCount = c.likes.count;
					c.likesUserLikes = c.likes.user_likes > 0;
					c.postId = postId;
					Dao.CreateOrUpdateStatus status = dao.createOrUpdate(c);

					if (c.attachments != null && status.isCreated())
						for (Attachment a : c.attachments) {
							a.comment = c;    // important!
							saveAttachment(a);
						}
				}
				return null;
			}
		});

	}


	public Comment fetchCommentById(final int id) {
		try {
			return (Comment) ((BaseDaoImpl) getDao(Comment.class)).queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public List<Comment> fetchComments(final long postId, final long dateFrom, final long dateTo, final List<Filter> filters) {
		try {
			Dao<Comment, ?> dao = getDao(Comment.class);
			QueryBuilder b = dao.queryBuilder();
			//final List<Where> filtersWhere = new ArrayList<>();
			Where where = b.where();
			where.between("date", dateFrom, dateTo);
			where.and().eq("postId", postId);
			PreparedQuery query;
			if (filters != null && filters.size() != 0) {
				L.v("fetch comments with filters");
				for (Filter filter : filters) {
					where.and();
					whereBuilder(where, filter.condition);
				}
			} else {
				L.v("fetch comments with no filters");
			}
			L.v("query="+where.getStatement());
			query = where.prepare();


			return dao.query(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static final Pattern WHERE_PATTERN = Pattern.compile("(\\w+)\\s?([<>=]+|LIKE)\\s?(.+)");


	private void whereBuilder(final Where where, final String condition) throws SQLException {
		L.v("filter " + condition);
		Matcher m = WHERE_PATTERN.matcher(condition);
		if (m.matches()) {
			String field = m.group(1);
			String operator = m.group(2);
			String value = m.group(3);
			switch (operator) {
				case ">":
					where.gt(field, value);
					break;
				case "<":
					where.lt(field, value);
					break;
				case "=":
					if (value.equals("NULL"))
						where.isNull(field);
					else
						where.eq(field, value);
					break;
				case "<>":
					if (value.equals("NULL"))
						where.isNotNull(field);
					else
						where.ne(field, value);
					break;
				case "<=":
					where.le(field, value);
					break;
				case ">=":
					where.ge(field, value);
					break;
				case "LIKE":
					where.like(field, value);
					break;
			}
		} else {
			L.v("raw clause");
			where.raw(condition);
		}

	}


}
