package ds.vkplus.network.model;

import java.util.List;

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

	public long id;
	public long from_id;
	public long date;
	public String text;
	public long reply_to_user;
	public long reply_to_comment;
	public List<Attachment> attachments;

}
