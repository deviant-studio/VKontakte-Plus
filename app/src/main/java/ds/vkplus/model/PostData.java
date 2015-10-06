package ds.vkplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidDao;

@DatabaseTable(daoClass = AndroidDao.class)
public class PostData {

	@DatabaseField(id = true)
	public long postId;
	@DatabaseField
	public String nextRaw;
	@DatabaseField
	public long fetchDate;
	@DatabaseField
	public long postDate;
	@DatabaseField
	public int lastIndex;   // index of bottom post
	@DatabaseField
	public int total;   // total news count in the table News

}
