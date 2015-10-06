package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;

@DatabaseTable(daoClass = AndroidDao.class)
public class Group extends BaseDaoEnabled implements Producer{

	@DatabaseField(id=true)
	public long id;
	@DatabaseField
	public int is_closed;
	@DatabaseField
	public int is_admin;
	@DatabaseField
	public int is_member;
	@DatabaseField
	public String type;
	@DatabaseField
	public String name;
	@DatabaseField
	public String description;
	@DatabaseField
	public String screen_name;
	@DatabaseField
	public String photo_50;
	@DatabaseField
	public String photo_100;
	@DatabaseField
	public String photo_200;


	public Group() {
	}


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
