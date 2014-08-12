package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;

@DatabaseTable(daoClass = AndroidDao.class)
public class Album extends BaseDaoEnabled {

		/*id — идентификатор альбома;
		thumb — обложка альбома, содержит объект photo:
		owner_id — идентификатор владельца альбома;
		title — название альбома;
		description — описание альбома;
		created — дата создания альбома в формате unixtime;
		updated — дата последнего обновления альбома в формате unixtime;
		size — количество фотографий в альбоме.
*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long owner_id;
	@DatabaseField
	public long created;
	@DatabaseField
	public long updated;
	@DatabaseField(foreign = true)
	public Photo thumb;
	@DatabaseField
	public String title;
	@DatabaseField
	public String description;
	@DatabaseField
	public int size;


}