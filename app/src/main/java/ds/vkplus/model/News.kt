package ds.vkplus.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.misc.BaseDaoEnabled
import com.j256.ormlite.table.DatabaseTable
import ds.vkplus.db.DBHelper
import ds.vkplus.db.extras.AndroidDao
import ds.vkplus.utils.format

@DatabaseTable(daoClass = AndroidDao::class)
class News : BaseDaoEnabled<Any, Any>() {
	
	@DatabaseField
	var type: String? = null
	@DatabaseField
	var post_type: String? = null
	@DatabaseField
	var text: String? = null
	@DatabaseField
	var source_id: Long = 0
	@DatabaseField
	var date: Long = 0
	@DatabaseField(id = true, uniqueIndex = true)
	var post_id: Long = 0
	@DatabaseField
	var signer_id: Long = 0
	
	@ForeignCollectionField(eager = true) /*, maxEagerLevel = 2*/
	var copy_history: java.util.Collection<News>? = null
	@ForeignCollectionField(eager = true)
	var attachments: java.util.Collection<Attachment>? = null
	//@ForeignCollectionField(eager = true)
	var photos: VKList<Photo>? = null
	
	
	@ForeignCollectionField(eager = true)
	var photosPersist: java.util.Collection<Photo>? = null
	
	
	var post_source: PostSource? = null
	lateinit var comments: Comments
	lateinit var likes: Likes
	lateinit var reposts: Reposts
	var id: Long = 0 // used in nested posts
	var owner_id: Long = 0   // --
	var from_id: Long = 0
	
	// local stuff
	val producer: Producer by lazy { DBHelper.instance.getProducerById(source_id) }
	val signer: Producer by lazy { DBHelper.instance.getProducerById(signer_id) }
	
	//@DatabaseField
	var isExpanded: Boolean = false
	@DatabaseField
	var commentsCount: Int = 0
	@DatabaseField
	var commentsCanPost: Boolean = false
	@DatabaseField
	var repostsCount: Int = 0
	@DatabaseField
	var repostsUserReposted: Boolean = false
	@DatabaseField
	var postSourceType: String? = null
	@DatabaseField
	var likesCount: Int = 0
	@DatabaseField
	var likesUserLikes: Boolean = false
	@DatabaseField
	var likesCanLike: Boolean = false
	
	@DatabaseField(foreign = true)
	@Transient var parent: News? = null
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	override fun toString(): String {
		val result = format("text=%s postId=%s", text!!, post_id)
		return result
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	class PostSource {
		var type: String? = null
	}
	
	
	class Comments {
		
		var count: Int = 0
		var can_post: Int = 0
	}
	
	
	class Reposts {
		
		var count: Int = 0
		var user_reposted: Int = 0
	}
	
	companion object {
		
		/*post — новые записи со стен
	photo — новые фотографии
	photo_tag — новые отметки на фотографиях
	wall_photo — новые фотографии на стенах
	friend — новые друзья
	note — новые заметки*/
		
		val TYPE_POST = "post"
		val TYPE_PHOTO = "photo"
		val TYPE_PHOTO_TAG = "photo_tag"
		val TYPE_WALL_PHOTO = "wall_photo"
		val TYPE_FRIEND = "friend"
		val TYPE_NOTE = "note"
	}
	
	
}
