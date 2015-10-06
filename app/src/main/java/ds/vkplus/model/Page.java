package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;

@DatabaseTable(daoClass = AndroidDao.class)
public class Page  extends BaseDaoEnabled {

/*
    pid — идентификатор wiki страницы;
    gid — идентификатор группы, которой принадлежит wiki страница;
    title — название wiki страницы.
*/

	@DatabaseField(id = true)
	public long pid;
	@DatabaseField
	public long gid;
	@DatabaseField
	public String title;

}