package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class Document  extends BaseDaoEnabled {
		/*id	идентификатор документа.
				положительное число
		owner_id	идентификатор пользователя, загрузившего документ.
		положительное число
		title	название документа.
				строка
		size	размер документа в байтах.
				положительное число
		ext	расширение документа.
				строка
		url	адрес документа, по которому его можно загрузить.
				строка
		photo_100	адрес изображения с размером 100x75px (если файл графический).
		строка
		photo_130	адрес изображения с размером 130x100px (если файл графический).
		строка*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long owner_id;
	@DatabaseField
	public String title;
	@DatabaseField
	public int size;
	@DatabaseField
	public String ext;
	@DatabaseField
	public String url;
	@DatabaseField
	public String photo_100;
	@DatabaseField
	public String photo_130;

}