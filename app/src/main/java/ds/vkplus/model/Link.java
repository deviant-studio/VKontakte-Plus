package ds.vkplus.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class Link  extends BaseDaoEnabled {
	/*url — адрес ссылки;
	title — заголовок ссылки;
	description — описание ссылки;
	image_src — адрес превью изображения к ссылке (если имеется);
	preview_page — идентификатор wiki страницы с контентом для предпросмотра содержимого страницы, который может быть получен используя метод pages.get. Идентификатор
	возвращается в формате "owner_id_page_id";
	preview_url — адрес страницы для предпросмотра содержимого страницы.*/

	@DatabaseField(generatedId = true)
	public long _id;

	@DatabaseField
	public String url;
	@DatabaseField
	public String title;
	@DatabaseField
	public String description;
	@DatabaseField
	public String image_src;
	@DatabaseField
	public String preview_page;
	@DatabaseField
	public String preview_url;

	@DatabaseField(foreign = true,foreignAutoRefresh = true)
	Attachment attachment;
}