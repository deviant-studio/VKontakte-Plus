package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class Profile extends BaseDaoEnabled implements Producer {

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public int sex;
	@DatabaseField
	public int online;
	@DatabaseField
	public String first_name;
	@DatabaseField
	public String last_name;
	@DatabaseField
	public String screen_name;
	@DatabaseField
	public String photo_50;
	@DatabaseField
	public String photo_100;


	public Profile() {
	}


	@Override
	public String getName() {
		return first_name + " " + last_name;
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
