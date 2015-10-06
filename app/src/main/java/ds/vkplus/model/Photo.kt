package ds.vkplus.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.misc.BaseDaoEnabled
import com.j256.ormlite.table.DatabaseTable
import ds.vkplus.db.extras.AndroidDao
import rx.Observable
import rx.functions.Func1


@DatabaseTable(daoClass = AndroidDao::class)
class Photo : BaseDaoEnabled<Any, Any>() {
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
	
	@DatabaseField(id = true)
	var id: Long = 0
	@DatabaseField
	var album_id: Long = 0
	@DatabaseField
	var owner_id: Long = 0
	@DatabaseField
	var user_id: Long = 0
	@DatabaseField
	var photo_75: String? = null
	@DatabaseField
	var photo_130: String? = null
	@DatabaseField
	var photo_604: String? = null
	@DatabaseField
	var photo_807: String? = null
	@DatabaseField
	var photo_1280: String? = null
	@DatabaseField
	var photo_2560: String? = null
	@DatabaseField
	var width: Int = 0
	@DatabaseField
	var height: Int = 0
	@DatabaseField
	var text: String? = null
	@DatabaseField
	var date: Long = 0
	
	@DatabaseField(foreign = true)
	var news: News? = null
	
	
	val biggestPhoto: String?
		get() = listOf(photo_2560, photo_1280, photo_807, photo_604).first { it != null }
	
	
	val thumb: String?
		get() = listOf(photo_604, photo_807, photo_1280, photo_2560).first { it != null }
}