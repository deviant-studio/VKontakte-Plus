package ds.vkplus.network.model;

public class Group implements Producer{
	public long id;
	public int is_closed;
	public int is_admin;
	public int is_member;
	public String type;
	public String name;
	public String description;
	public String screen_name;
	public String photo_50;
	public String photo_100;
	public String photo_200;


	@Override
	public String getName() {
		return name;
	}


	@Override
	public String getThumb() {
		return photo_200;
	}


	@Override
	public long getId() {
		return id;
	}
}
