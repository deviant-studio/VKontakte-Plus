package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.DBHelper;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

import java.sql.SQLException;
import java.util.Collection;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class News extends BaseDaoEnabled {

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

	public News[] copy_history;
	@ForeignCollectionField(eager = true)
	public Collection<Attachment> attachments;

	public PostSource post_source;
	public Comments comments;
	public Likes likes;
	public Reposts reposts;

	// local stuff
	private Producer producer;
	public Producer signer;
	public Boolean isExpanded;  // 3-state flag

	@DatabaseField
	public boolean nestedPost;
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


	public Producer getProducer() {
		if (producer == null) {
			try {
				producer = DBHelper.instance().getProducerById(source_id);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return producer;
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
