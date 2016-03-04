package ds.vkplus.db

import android.content.Context
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.misc.BaseDaoEnabled
import com.j256.ormlite.stmt.PreparedQuery
import com.j256.ormlite.stmt.Where
import com.j256.ormlite.table.TableUtils
import ds.vkplus.App
import ds.vkplus.Constants
import ds.vkplus.auth.AccountHelper
import ds.vkplus.db.extras.AndroidDao
import ds.vkplus.model.*
import ds.vkplus.utils.L
import java.sql.SQLException
import java.util.regex.Pattern

class DBHelper(context: Context) : DBHelperBase(context) {


	fun saveNewsResponse(news: NewsResponse) {

		if (news.items == null || news.items.size == 0)
			return

		try {
			L.v("saving profiles...")
			saveEntities(news.profiles, profilesDao)
			L.v("saving groups...")
			saveEntities(news.groups, groupsDao)

			newsDao.callBatchTasks {
				L.v("saving news...")
				for (item in news.items) {
					saveNews(item, null)
					if (item.copy_history != null) {
						L.v("saving nested post...")
						for (nested in item.copy_history!!) {
							saveNews(nested, item)
						}
					}
				}
			}

			L.v("saving next value... " + news.next_from)
			saveNext(news.items[news.items.size - 1], news.next_from)
		} catch (e: SQLException) {
			e.printStackTrace()
		}


	}


	private fun saveNews(item: News, parent: News?) {
		if (item.post_type != null)
			item.type = item.post_type

		if (parent != null) {
			item.parent = parent
			item.post_id = item.id
			item.source_id = if (item.owner_id != 0L) item.owner_id else item.from_id

		}

		when (item.type) {
			News.TYPE_POST ->

				if (parent == null) {
					item.likesCount = item.likes.count
					item.likesUserLikes = item.likes.user_likes > 0
					item.likesCanLike = item.likes.can_like > 0
					item.commentsCount = item.comments.count
					item.commentsCanPost = item.comments.can_post > 0
					item.repostsCount = item.reposts.count
				}

			News.TYPE_WALL_PHOTO, News.TYPE_PHOTO -> {
				item.post_id = item.date
				if (item.photos != null) {
					item.photosPersist = item.photos!!.items as java.util.Collection<Photo>
					for (p in item.photosPersist!!) {
						p.news = item    // important!
						getDao(Photo::class.java).createOrUpdate(p)
					}
				}
			}
		}

		newsDao.createOrUpdate(item)

		//save attachments
		if (item.attachments != null)
			for (a in item.attachments!!) {
				a!!.news = item    // important!
				saveAttachment(a)
			}
	}


	private fun saveNext(last: News, next: String) {
		val data = PostData()
		val m = NEXT_PATTERN.matcher(next)
		if (m.matches()) {
			data.lastIndex = Integer.valueOf(m.group(1))!!
		} else {
			L.e("next doesnt match regex pattern")
		}
		data.postId = last.post_id
		data.postDate = last.date
		data.nextRaw = next
		data.fetchDate = System.currentTimeMillis()
		data.total = fetchNewsCount()
		getDao(PostData::class.java).createOrUpdate(data)
	}


	private fun saveFakeNext(news: News) {
		val oldest = fetchOldestNext()
		val m = NEXT_PATTERN.matcher(oldest!!.nextRaw)
		if (m.matches()) {
			val date = m.group(3)
			val fake_next = "${oldest.lastIndex}/${news.post_id}_${date}_5"
			L.v("fake next=" + fake_next)
			saveNext(news, fake_next)
		} else {
			L.e("oldest next doesnt match regex pattern. double fail. this is not possible!")
		}
	}


	fun fetchNewsCount(): Int {
		try {
			return newsDao.queryBuilder().countOf().toInt()
		} catch (e: SQLException) {
			e.printStackTrace()
			return 0
		}

	}


	fun fetchLatestNext(): PostData? {
		try {
			val dao = getDao(PostData::class.java)
			val qb = dao.queryBuilder().orderBy("fetchDate", false)
			return qb.queryForFirst()
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	fun fetchOldestNext(): PostData? {
		try {
			val dao = getDao(PostData::class.java)
			val qb = dao.queryBuilder().orderBy("postDate", true)
			return qb.queryForFirst()
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	fun fetchNextByPostId(postId: Long): PostData? {
		try {
			L.v("fetchNextByPostId " + postId)
			val dao = getDao(PostData::class.java)
			return dao.queryBuilder().where().eq("postId", postId).queryForFirst()
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	private fun saveAttachment(a: Attachment) {
		val content = a.getContent<BaseDaoEnabled<Any, Any>>() ?: return // actually this shouldnt happen

		if (!isAttachmentExist(a))
			try {
				attachmentsDao.create(a)
				val dao = getDao(content.javaClass)
				dao.createOrUpdate(content)
			} catch (e: SQLException) {
				e.printStackTrace()
			}

	}


	private fun isAttachmentExist(a: Attachment): Boolean {
		if (a.type == Attachment.TYPE_PHOTO) {
			try {
				return attachmentsDao
					.queryBuilder()
					.where()
					.eq("news_id", a.news.id)
					.and()
					.eq("photo_id", a.photo.id)
					.countOf() > 0L
			} catch (e: Exception) {
				e.printStackTrace()
			}

		}
		return false
	}


	fun <T : BaseDaoEnabled<Any, Any>> saveEntities(list: Collection<T>?, dao: AndroidDao<T, Int>?) {
		if (list == null || dao == null)
			return

		dao.callBatchTasks {
			for (e in list) {
				dao.createOrUpdate(e)
			}
		}

	}


	fun fetchAllNews(): List<News>? {
		L.v("fetchAllNews")
		return fetchNews(System.currentTimeMillis() / 1000)
	}


	fun fetchNews(byDate: Long): List<News>? {
		try {

			val filters = filtersDao.fetchActiveFilters(Filter.TYPE_POSTS)
			val b = newsDao.queryBuilder().orderBy("date", false)
			val where = b
				.where()
				.lt("date", byDate)
				.and()
				.isNull("parent_id")

			val query: PreparedQuery<Any>
			if (filters != null && filters.size != 0) {
				L.v("fetch news with filters")
				for (filter in filters) {
					whereBuilder(where, filter.condition)
				}

			}
			L.v("query=" + where.statement)
			val news = where.query()

			return news
		} catch (e: SQLException) {
			e.printStackTrace()
		}

		return null
	}


	fun getProducerById(id: Long): Producer {
		var p: Producer? = groupsDao.queryBuilder().where().eq("id", Math.abs(id)).queryForFirst()
		if (p == null)
			p = profilesDao.queryBuilder().where().eq("id", Math.abs(id)).queryForFirst()
		if (p == null) {
			L.e("cant attach producer")
			p = UnknownProducer()
		}

		return p
	}


	fun dropAll() {
		for (cls in DBHelperBase.classes) {
			getDao(cls).clearObjectCache()
			TableUtils.dropTable<Any, Long>(connectionSource, cls, true)
			TableUtils.createTable(connectionSource, cls)
		}
	}


	fun fetchLatestPost(): News? {
		try {
			return newsDao.queryBuilder().orderBy("date", false).where().isNull("parent_id").queryForFirst()
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	fun fetchNewsById(id: Int): News? {
		try {
			return newsDao.queryForId(id)
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	fun fetchVideo(id: Int?): Video? {
		val dao = getDao<Dao<Video, Int>, Video>(Video::class.java)
		try {
			return dao.queryForId(id)
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	fun saveVideo(video: Video) {
		val dao = getDao<Dao<Video, Int>, Video>(Video::class.java)
		try {
			dao.update(video)
		} catch (e: SQLException) {
			e.printStackTrace()
		}


	}


	fun saveCommentsResponse(comments: CommentsList, postId: Long) {
		try {
			saveEntities(comments.groups, getDao<AndroidDao<Group, Int>, Group>(Group::class.java))
			saveEntities(comments.profiles, getDao<AndroidDao<Profile, Int>, Profile>(Profile::class.java))
			saveComments(comments.items, postId)

		} catch (e: SQLException) {
			e.printStackTrace()
		}


	}


	fun saveComments(list: List<Comment>?, postId: Long) {
		if (list == null)
			return
		val dao = getDao<AndroidDao<Comment, Int>, Comment>(Comment::class.java)
		dao.callBatchTasks {
			for (c in list) {
				c.likesCount = c.likes?.count ?: 0
				c.likesUserLikes = c.likes?.user_likes ?: 0 > 0
				c.postId = postId
				val status = dao.createOrUpdate(c)

				if (c.attachments != null && status.isCreated)
					for (a in c.attachments) {
						a.comment = c    // important!
						saveAttachment(a)
					}
			}
		}

	}


	fun fetchCommentById(id: Int): Comment? {
		try {
			return (getDao(Comment::class.java) as BaseDaoImpl<Any, Any>).queryForId(id) as Comment
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	fun fetchComments(postId: Long?, dateFrom: Long, dateTo: Long, filters: List<Filter>?): List<Comment>? {
		try {
			val dao = getDao(Comment::class.java)
			val b = dao.queryBuilder()
			//final List<Where> filtersWhere = new ArrayList<>();
			val where = b.where()
			where.between("date", dateFrom, dateTo)
			where.and().eq("postId", postId)
			val query: PreparedQuery<*>
			if (filters != null && filters.size != 0) {
				L.v("fetch comments with filters")
				for (filter in filters) {
					//where.and();
					whereBuilder(where, filter.condition)
				}
			} else {
				L.v("fetch comments with no filters")
			}
			L.v("query=" + where.statement)
			where.or().eq("from_id",getMyId())
			query = where.prepare()


			return dao.query(query)
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	private fun whereBuilder(where: Where<*, *>, condition: String) {
		//L.v("filter " + condition);
		val m = WHERE_PATTERN.matcher(condition)
		if (m.matches()) {
			where.and()
			val field = m.group(1)
			val operator = m.group(2)
			val value = m.group(3)
			when (operator) {
				">" -> where.gt(field, value)
				"<" -> where.lt(field, value)
				"=" ->
					if (value == "NULL")
						where.isNull(field)
					else
						where.eq(field, value)
				"<>" ->
					if (value == "NULL")
						where.isNotNull(field)
					else
						where.ne(field, value)
				"<=" -> where.le(field, value)
				">=" -> where.ge(field, value)
				"LIKE" -> where.like(field, value)
				else -> {
				}
			}
		} else {
			L.v("raw clause")

			// bypass groups filter
			if (condition.startsWith("-")) {
				L.w("skipped filter because of group filter")
				where.and().eq("source_id", condition)
				return
			}
			where.raw(condition)
		}

	}


	fun fetchMyGroups(): List<Group>? {
		try {
			return getDao(Group::class.java).queryBuilder().where().eq("is_member", 1).query()
		} catch (e: SQLException) {
			e.printStackTrace()
			return null
		}

	}


	fun fetchCurrentGroupFilterId(): String? {
		try {
			val parent = getDao(Filter::class.java).queryBuilder().where().eq("title", DBHelperBase.FILTER_BY_GROUP).queryForFirst()
			if (parent != null)
				return getDao(Filter::class.java).queryBuilder().where().eq("state", Filter.State.CHECKED).and().eq("parent_id", parent.id).and().isNotNull("condition").queryForFirst().condition
		} catch (e: Exception) {
			e.printStackTrace()
		}

		return null
	}


	fun refreshGroupsFilter(myGroups: List<Group>) {
		L.v("groups: save %s groups", myGroups.size)
		try {
			val parent = filtersDao.queryBuilder().where().eq("filterType", Filter.TYPE_POSTS).and().eq("title", DBHelperBase.FILTER_BY_GROUP).queryForFirst() ?: return

			//filtersDao.deleteAll();
			val cacheds = filtersDao.queryBuilder().where().eq("parent_id", parent.id).and().eq("state", Filter.State.UNCHECKED).query()

			L.v("groups: delete %s filters", cacheds.size)
			filtersDao.delete(cacheds)
			filtersDao.clearObjectCache()

			for (group in myGroups) {

				Filter(group.name, (-group.id).toString(), Filter.State.UNCHECKED, Filter.TYPE_POSTS, parent)
				L.v("group %s saved", group.name)
			}


			parent.update()

		} catch (e: SQLException) {
			e.printStackTrace()
		}

	}
	
	fun getMyId(): Long {
		with (AccountHelper.instance) {
			return am.getUserData(getAccount(), Constants.KEY_USERID).toLong()
		}
	}

	companion object {

		val NEXT_PATTERN: Pattern = Pattern.compile("""^(\d+)/(-?\d+)(?:_\d+)?_(\d+)_\d+$""")
		val WHERE_PATTERN: Pattern = Pattern.compile("""(\w+)\s?([<>=]+|LIKE)\s?(.+)""")
		val instance by lazy { OpenHelperManager.getHelper(App.instance, DBHelper::class.java) }

	}
}
