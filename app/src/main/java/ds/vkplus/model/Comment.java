package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

import java.util.Collection;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class Comment {

	/*id	идентификатор комментария.
			положительное число
	from_id	идентификатор автора комментария.
	int (числовое значение)
	date	дата создания комментария в формате unixtime.
			положительное число
	text	текст комментария.
			строка
	reply_to_user	идентификатор пользователя или сообщества, в ответ которому оставлен текущий комментарий (если применимо).
	int (числовое значение)
	reply_to_comment	идентификатор комментария, в ответ на который оставлен текущий (если применимо).
	положительное число
	attachments	объект, содержащий информацию о медиавложениях в комментарии. См. описание формата медиавложений.*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public long from_id;
	@DatabaseField
	public long date;
	@DatabaseField
	public String text;
	@DatabaseField
	public long reply_to_user;
	@DatabaseField
	public long reply_to_comment;
	@ForeignCollectionField
	public Collection<Attachment> attachments;
	public Likes likes;

	@DatabaseField
	public int likesCount;
	@DatabaseField
	public boolean likesUserLikes;

	public Producer producer;

}
