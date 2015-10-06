package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.DBHelper;
import ds.vkplus.db.extras.AndroidDao;

import java.util.Collection;

@DatabaseTable(daoClass = AndroidDao.class)
public class News extends BaseDaoEnabled {

	/*post — новые записи со стен
	photo — новые фотографии
	photo_tag — новые отметки на фотографиях
	wall_photo — новые фотографии на стенах
	friend — новые друзья
	note — новые заметки*/

	public static final String TYPE_POST = "post";
	public static final String TYPE_PHOTO = "photo";
	public static final String TYPE_PHOTO_TAG = "photo_tag";
	public static final String TYPE_WALL_PHOTO = "wall_photo";
	public static final String TYPE_FRIEND = "friend";
	public static final String TYPE_NOTE = "note";

	@DatabaseField
	public String type;
	@DatabaseField
	public String post_type;
	@DatabaseField
	public String text;
	@DatabaseField
	public long source_id;
	@DatabaseField
	public long date;
	@DatabaseField(id = true, uniqueIndex = true)
	public long post_id;
	@DatabaseField
	public long signer_id;

	@ForeignCollectionField(eager = true/*, maxEagerLevel = 2*/)
	public Collection<News> copy_history;
	@ForeignCollectionField(eager = true)
	public Collection<Attachment> attachments;
	//@ForeignCollectionField(eager = true)
	public VKList<Photo> photos;


	@ForeignCollectionField(eager = true)
	public Collection<Photo> photosPersist;



	public PostSource post_source;
	public Comments comments;
	public Likes likes;
	public Reposts reposts;
	public long id; // used in nested posts
	public long owner_id;   // --
	public long from_id;

	// local stuff
	private Producer producer;
	private Producer signer;

	//@DatabaseField
	public boolean isExpanded;
	@DatabaseField
	public int commentsCount;
	@DatabaseField
	public boolean commentsCanPost;
	@DatabaseField
	public int repostsCount;
	@DatabaseField
	public boolean repostsUserReposted;
	@DatabaseField
	public String postSourceType;
	@DatabaseField
	public int likesCount;
	@DatabaseField
	public boolean likesUserLikes;
	@DatabaseField
	public boolean likesCanLike;

	@DatabaseField(foreign = true)
	public transient News parent;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	//@DebugLog
	public Producer getProducer() {
		if (producer == null) {
			producer = DBHelper.Companion.getInstance().getProducerById(source_id);
		}

		return producer;
	}


	//@DebugLog
	public Producer getSigner() {
		if (signer == null && signer_id != 0) {
			signer = DBHelper.Companion.getInstance().getProducerById(signer_id);
		}

		return signer;
	}


	@Override
	public String toString() {
		final String result = String.format("text=%s postId=%s", text, post_id);
		return result;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static class PostSource {

		public String type;
	}


	public static class Comments {

		public int count;
		public int can_post;
	}


	public static class Reposts {

		public int count;
		public int user_reposted;
	}


}
