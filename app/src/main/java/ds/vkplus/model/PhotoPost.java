package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class PhotoPost extends BaseDaoEnabled {

	/*id — идентификатор фотографии
	owner_id — идентификатор владельца фотографии
	album_id — идентификатор альбома
	src — адрес изображения для предпросмотра
	src_big — адрес полноразмерного изображения*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long album_id;
	@DatabaseField
	public long owner_id;
	@DatabaseField
	public String src;
	@DatabaseField
	public String src_big;

	@DatabaseField(foreign = true)
	public News news;

}