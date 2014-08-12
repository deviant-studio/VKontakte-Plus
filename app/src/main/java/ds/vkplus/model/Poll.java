package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;

@DatabaseTable(daoClass = AndroidDao.class)
public class Poll  extends BaseDaoEnabled {

/*		id — идентификатор опроса для получения информации о нем через метод polls.getById;
		question — вопрос, заданный в голосовании.*/

	@DatabaseField(id = true)
	public long id;
	@DatabaseField
	public String question;

}