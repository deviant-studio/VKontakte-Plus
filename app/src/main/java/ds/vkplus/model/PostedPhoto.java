package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class PostedPhoto  extends BaseDaoEnabled {


		/*id — идентификатор фотографии;
		owner_id — идентификатор владельца фотографии;
		photo_130 — адрес фотографии для предпросмотра;
		photo_604 — адрес полноразмерной фотографии.*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long owner_id;
	@DatabaseField
	public String photo_130;
	@DatabaseField
	public String photo_604;
}