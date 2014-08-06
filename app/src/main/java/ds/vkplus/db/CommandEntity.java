package ds.vkplus.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import ds.vkplus.db.extras.AndroidBaseDaoImpl;

@DatabaseTable(daoClass = AndroidBaseDaoImpl.class)
public class CommandEntity extends BaseDaoEnabled {

	@DatabaseField(generatedId = true)
	public transient int id;

	@DatabaseField
	public String message;

	@DatabaseField
	public int type;


	public CommandEntity() { }


	public CommandEntity(final String m) {
		message = m;
	}


	@Override
	public String toString() {
		return message;
	}
}
