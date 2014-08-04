package ds.vkplus.network.model;

import java.util.List;

public class News {

	public String type;
	public String post_type;
	public String text;
	public long source_id;
	public long date;
	public long post_id;
	public News[] copy_history;

	public List<Attachment> attachments;
	public PostSource post_source;
	public Comments comments;
	public Likes likes;
	public Reposts reposts;

	public Producer producer;



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


	public static class Likes {

		public int count;
		public int user_likes;
		public int can_like;
		public int can_publish;
	}

}
