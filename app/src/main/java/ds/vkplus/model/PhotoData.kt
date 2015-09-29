package ds.vkplus.model

class PhotoData {
	
	val id: Long
	var width = 0
	var height = 0
	var url: String
	var ratio = 0F
	var type = 0
	var breakAfter = false
	var floating = false
	//var paddingBottom = false
	var extra: String?=null
	
	constructor(url: String, width: Int, height: Int, t: Int, id: Long) {
		this.width = if (width != 0) width else 1600
		this.height = if (height != 0) height else 1200
		this.url = url
		ratio = width.toFloat() / height
		type = t
		this.id = id
	}
	
	
	constructor(url: String, urlBig: String, id: Long) {
		this.url = url
		extra = urlBig
		this.id = id
	}
	
	
	fun setViewSize(w: Float, h: Float, breakAfter: Boolean, floating: Boolean) {
		width = w.toInt()
		height = h.toInt()
		this.breakAfter = breakAfter
		this.floating = floating
		
		
	}
	
	companion object {
		
		val TYPE_PHOTO = 0
		val TYPE_VIDEO = 1
		val TYPE_LINK = 2
	}
}
