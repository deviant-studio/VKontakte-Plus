package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class Attachment extends BaseDaoEnabled {

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

	//Map<String, ? extends BaseDaoEnabled> typesMap=new HashMap<>();


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

	@DatabaseField(generatedId = true)
	public long _id;

	@DatabaseField
	public String type;

	@DatabaseField(foreign = true)
	public Video video;
	@DatabaseField(foreign = true)
	public Photo photo;
	@DatabaseField(foreign = true)
	public PostedPhoto posted_photo;
	@DatabaseField(foreign = true)
	public Audio audio;
	@DatabaseField(foreign = true)
	public Link link;
	@DatabaseField(foreign = true)
	public Album album;
	@DatabaseField(foreign = true)
	public Poll poll;
	@DatabaseField(foreign = true)
	public Document document;
	@DatabaseField(foreign = true)
	public Page page;
	// ...

	@DatabaseField(foreign = true/*,foreignAutoCreate = true,foreignAutoRefresh = true*/)
	public News news;
	@DatabaseField(foreign = true/*,foreignAutoCreate = true,foreignAutoRefresh = true*/)
	public Comment comment;


	public <T extends BaseDaoEnabled> T getContent() {
		switch (type) {
			case TYPE_ALBUM:
				return (T) album;
			case TYPE_APP:
				return null;    // todo
			case TYPE_AUDIO:
				return (T) audio;
			case TYPE_DOC:
				return (T) document;
			case TYPE_GRAFFITI:
				return null;    // todo
			case TYPE_LINK:
				return (T) link;
			case TYPE_NOTE:
				return null;    // todo
			case TYPE_PAGE:
				return (T) page;
			case TYPE_PHOTO:
				return (T) photo;
			case TYPE_PHOTOS_LIST:
				return null;    // todo
			case TYPE_POLL:
				return (T) poll;
			case TYPE_POSTED_PHOTO:
				return (T) posted_photo;
			case TYPE_VIDEO:
				return (T) video;

		}

		return null;
	}

}