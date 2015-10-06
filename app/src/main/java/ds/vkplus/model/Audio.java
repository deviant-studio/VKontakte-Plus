package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;

@DatabaseTable(daoClass = AndroidDao.class)
public class Audio  extends BaseDaoEnabled {
		/*id	идентификатор аудиозаписи.
				положительное число
		owner_id	идентификатор владельца аудиозаписи.
		int (числовое значение)
		artist	исполнитель.
		строка
		title	название композиции.
				строка
		duration	длительность аудиозаписи в секундах.
				положительное число
		url	ссылка на mp3.
		строка
		lyrics_id	идентификатор текста аудиозаписи (если доступно).
		положительное число
		album_id	идентификатор альбома, в котором находится аудиозапись (если присвоен).
		положительное число
		genre_id	идентификатор жанра из списка аудио жанров.
				положительное число*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long owner_id;
	@DatabaseField
	public String artist;
	@DatabaseField
	public String title;
	@DatabaseField
	public String player;
	@DatabaseField
	public String url;
	@DatabaseField
	public int duration;
	@DatabaseField
	public long lyrics_id;
	@DatabaseField
	public long album_id;
	@DatabaseField
	public long genre_id;

}
