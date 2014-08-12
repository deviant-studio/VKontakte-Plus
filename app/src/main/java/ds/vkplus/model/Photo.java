package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;
import rx.Observable;


@DatabaseTable(daoClass = AndroidDao.class)
public class Photo extends BaseDaoEnabled {
		/*id 	идентификатор фотографии.
				положительное число
		album_id 	идентификатор альбома, в котором находится фотография.
		int (числовое значение)
		owner_id 	идентификатор владельца фотографии.
		int (числовое значение)
		user_id 	идентификатор пользователя, загрузившего фото (если фотография размещена в сообществе). Для фотографий, размещенных от имени сообщества, user_id=100.
		положительное число
		photo_75 	url копии фотографии с максимальным размером 75x75px.
				строка
		photo_130 	url копии фотографии с максимальным размером 130x130px.
				строка
		photo_604 	url копии фотографии с максимальным размером 604x604px.
				строка
		photo_807 	url копии фотографии с максимальным размером 807x807px.
				строка
		photo_1280 	url копии фотографии с максимальным размером 1280x1024px.
				строка
		photo_2560 	url копии фотографии с максимальным размером 2560x2048px.
				строка
		width 	ширина оригинала фотографии в пикселах.
		положительное число
		height 	высота оригинала фотографии в пикселах.
		положительное число
		text 	текст описания фотографии.
		строка
		date 	дата добавления в формате unixtime.
		положительное число*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long album_id;
	@DatabaseField
	public long owner_id;
	@DatabaseField
	public long user_id;
	@DatabaseField
	public String photo_75;
	@DatabaseField
	public String photo_130;
	@DatabaseField
	public String photo_604;
	@DatabaseField
	public String photo_807;
	@DatabaseField
	public String photo_1280;
	@DatabaseField
	public String photo_2560;
	@DatabaseField
	public int width;
	@DatabaseField
	public int height;
	@DatabaseField
	public String text;
	@DatabaseField
	public long date;

	@DatabaseField(foreign = true)
	public News news;


	public String getBiggestPhoto() {
		return Observable.from(photo_2560, photo_1280, photo_807, photo_604).toBlocking().first(val -> val != null);
	}
}