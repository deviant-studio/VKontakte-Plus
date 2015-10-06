package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;

@DatabaseTable(daoClass = AndroidDao.class)
public class Video extends BaseDaoEnabled<Video, Integer> {
		/*id	идентификатор видеозаписи.
				положительное число
		owner_id	идентификатор владельца видеозаписи.
		int (числовое значение)
		title	название видеозаписи.
				строка
		description	текст описания видеозаписи.
		строка
		duration	длительность ролика в секундах.
				положительное число
		link	строка, состоящая из ключа video+vid.
				строка
		photo_130	url изображения-обложки ролика с размером 130x98px.
				строка
		photo_320	url изображения-обложки ролика с размером 320x240px.
				строка
		photo_640	url изображения-обложки ролика с размером 640x480px (если размер есть).
		строка
		date	дата добавления видеозаписи в формате unixtime.
				положительное число
		views	количество просмотров видеозаписи.
		положительное число
		comments	количество комментариев к видеозаписи.
				положительное число
		player	адрес страницы с плеером, который можно использовать для воспроизведения ролика в браузере. Поддерживается flash и html5,
		плеер всегда масштабируется по размеру окна.
				строка*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long owner_id;
	@DatabaseField
	public String photo_130;
	@DatabaseField
	public String photo_320;
	@DatabaseField
	public String photo_640;
	@DatabaseField
	public String title;
	@DatabaseField
	public String description;
	@DatabaseField
	public String player;
	@DatabaseField
	public String link;
	@DatabaseField
	public int duration;
	@DatabaseField
	public long date;
	@DatabaseField
	public String access_key;

	/*{
			"id":169960108,
			"owner_id":-38854900,
			"title":"Как российские ",
			"duration":293,
			"description":"",
			"date":1407752827,
			"views":2513,
			"comments":51,
			"photo_130":"http:\/\/cs619624.vk.me\/u13455651\/video\/s_72bc3e95.jpg",
			"photo_320":"http:\/\/cs619624.vk.me\/u13455651\/video\/l_98527e42.jpg",
			"player":"http:\/\/www.youtube.com\/embed\/jU1pXagKJ_w"}]}*/
}