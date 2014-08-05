package ds.vkplus.network.model;

public class Attachment {

	public static final String TYPE_PHOTO = "photo";
	public static final String TYPE_VIDEO = "video";
	public static final String TYPE_AUDIO = "audio";
	public static final String TYPE_DOC = "doc";
	public static final String TYPE_POSTED_PHOTO = "posted_photo";
	public static final String TYPE_GRAFFITI = "graffiti";
	public static final String TYPE_LINK = "link";
	public static final String TYPE_NOTE = "note";
	public static final String TYPE_APP = "app";
	public static final String TYPE_POLL = "poll";
	public static final String TYPE_PAGE = "page";
	public static final String TYPE_ALBUM = "album";
	public static final String TYPE_PHOTOS_LIST = "photos_list";

	/*
	photo — фотография из альбома;
	posted_photo — фотография, загруженная напрямую с компьютера пользователя;
	video — видеозапись;
	audio — аудиозапись;
	doc — документ;
	graffiti — граффити;
	link — ссылка на web-страницу;
	note — заметка;
	app — изображение, загруженное сторонним приложением;
	poll — голосование;
	page — вики-страница;
	album — альбом с фотографиями.
	photos_list — список фотографий, размещенных в одном посте. Количество фотографий может превышать допустимое количество аттачей.*/


	public String type;

	public Video video;
	public Photo photo;
	public PostedPhoto posted_photo;
	public Audio audio;
	public Link link;
	public Album album;
	public Poll poll;
	public Document document;
	public Page page;
	// ...


	public static class Photo {
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

		public long id;
		public long album_id;
		public long owner_id;
		public long user_id;
		public String photo_75;
		public String photo_130;
		public String photo_604;
		public String photo_807;
		public String photo_1280;
		public String photo_2560;
		public int width;
		public int height;
		public String text;
		public long date;
	}

	public static class PostedPhoto {


		/*id — идентификатор фотографии;
		owner_id — идентификатор владельца фотографии;
		photo_130 — адрес фотографии для предпросмотра;
		photo_604 — адрес полноразмерной фотографии.*/


		public long id;
		public long owner_id;
		public String photo_130;
		public String photo_604;
	}


	public static class Video {
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

		public long id;
		public long owner_id;
		public String photo_130;
		public String photo_320;
		public String photo_640;
		public String title;
		public String description;
		public String player;
		public String link;
		public int duration;
		public long date;
	}


	public static class Audio {
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

		public long id;
		public long owner_id;
		public String artist;
		public String title;
		public String player;
		public String url;
		public int duration;
		public long lyrics_id;
		public long album_id;
		public long genre_id;

	}


	public static class Document {
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

		public long id;
		public long owner_id;
		public String title;
		public int size;
		public String ext;
		public String url;
		public String photo_100;
		public String photo_130;

	}


	public static class Link {
	/*url — адрес ссылки;
	title — заголовок ссылки;
	description — описание ссылки;
	image_src — адрес превью изображения к ссылке (если имеется);
	preview_page — идентификатор wiki страницы с контентом для предпросмотра содержимого страницы, который может быть получен используя метод pages.get. Идентификатор
	возвращается в формате "owner_id_page_id";
	preview_url — адрес страницы для предпросмотра содержимого страницы.*/

		public String url;
		public String title;
		public String description;
		public String image_src;
		public String preview_page;
		public String preview_url;
	}


	public static class Poll {

/*		id — идентификатор опроса для получения информации о нем через метод polls.getById;
		question — вопрос, заданный в голосовании.*/

		public long id;
		public String question;

	}


	public static class Page {

/*
    pid — идентификатор wiki страницы;
    gid — идентификатор группы, которой принадлежит wiki страница;
    title — название wiki страницы.
*/

		public long pid;
		public long gid;
		public String title;

	}


	public static class Album {

		/*id — идентификатор альбома;
		thumb — обложка альбома, содержит объект photo:
		owner_id — идентификатор владельца альбома;
		title — название альбома;
		description — описание альбома;
		created — дата создания альбома в формате unixtime;
		updated — дата последнего обновления альбома в формате unixtime;
		size — количество фотографий в альбоме.
*/

		public long id;
		public long owner_id;
		public long created;
		public long updated;
		public Photo thumb;
		public String title;
		public String description;
		public int size;


	}

}