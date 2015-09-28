package ds.vkplus.db.extras

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.DatabaseTableConfig

import java.sql.SQLException

open class AndroidDao<T, ID> : BaseDaoImpl<T, ID> {

    @Throws(SQLException::class)
    constructor(dataClass: Class<T>) : super(dataClass)

    @Throws(SQLException::class)
    constructor(connectionSource: ConnectionSource, dataClass: Class<T>) : super(connectionSource, dataClass)

    @Throws(SQLException::class)
    constructor(connectionSource: ConnectionSource, tableConfig: DatabaseTableConfig<T>) : super(connectionSource, tableConfig)


}

