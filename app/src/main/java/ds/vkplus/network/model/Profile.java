package ds.vkplus.network.model;

public class Profile implements Producer{

	public long id;
	public int sex;
	public int online;
	public String first_name;
	public String last_name;
	public String screen_name;
	public String photo_50;
	public String photo_100;


	@Override
	public String getName() {
		return first_name+" "+last_name;
	}


	@Override
	public String getThumb() {
		return photo_100;
	}


	@Override
	public long getId() {
		return id;
	}
}
