package ds.vkplus.model;

import java.util.List;


public class NewsResponse {

	public static final int POST_LENGTH_THRESHOLD = 128;

	public List<News> items;
	public List<Profile> profiles;
	public List<Group> groups;
	public String next_from;


}
