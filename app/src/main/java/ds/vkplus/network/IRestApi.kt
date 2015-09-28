package ds.vkplus.network

import ds.vkplus.model.*
import retrofit.http.GET
import retrofit.http.Query
import rx.Observable

interface IRestApi {
	
	/*@GET("/newsfeed.get")
	ApiResponse<NewsResponse> getNews(
			@Query("filters") String filters,
			@Query("source_ids") String sourceIds,  // groups, friends, pages, following, <uid>
			@Query("start_from") String next,
			@Query("count") int count);             // max=100
*/
	@GET("/newsfeed.get")
	public fun getNews2(
		@Query("filters") filters: String,
		@Query("source_ids") sourceIds: String?, // groups, friends, pages, following, <uid>
		@Query("start_from") next: String?,
		@Query("count") count: Int, // max=100
		@Query("start_time") startTime: Long?,
		@Query("max_photos") maxPhotos: Int?): Observable<ApiResponse<NewsResponse>>
	
	
	@GET("/wall.get")
	public fun getWall(
		@Query("filters") filters: String,
		@Query("source_ids") sourceIds: String, // groups, friends, pages, following, <uid>
		@Query("start_from") next: String,
		@Query("count") count: Int, // max=100
		@Query("start_time") startTime: Long?,
		@Query("max_photos") maxPhotos: Int?): Observable<ApiResponse<NewsResponse>>
	
	
	/*
	owner_id	идентификатор владельца страницы (пользователь или сообщество).

	Обратите внимание, идентификатор сообщества в параметре owner_id необходимо указывать со знаком "-" — например, owner_id=-1 соответствует идентификатору сообщества
	ВКонтакте API (club1)


	целое число, по умолчанию идентификатор текущего пользователя
	post_id	идентификатор записи на стене.
	положительное число, обязательный параметр
	need_likes	1 — возвращать информацию о лайках.
	флаг, может принимать значения 1 или 0
	offset	сдвиг, необходимый для получения конкретной выборки результатов.
			положительное число
	count	число комментариев, которые необходимо получить. По умолчанию — 10, максимальное значение — 100.
	положительное число
	sort	порядок сортировки комментариев (asc — от старых к новым, desc - от новых к старым)
	строка
	preview_length	количество символов, по которому нужно обрезать текст комментария. Укажите 0, если Вы не хотите обрезатьтекст.
			положительное число
	extended	1 — комментарии в ответе будут возвращены в виде пронумерованных объектов, дополнительно будут возвращены списки объектов profiles, groups.
	флаг, может принимать значения 1 или 0, доступен начиная с версии 5.0*/
	@GET("/wall.getComments?need_likes=1&extended=1&sort=asc")
	public fun getComments(
		//@Query("filters") String filters,
		//@Query("source_ids") String sourceIds,  // groups, friends, pages, following, <uid>
		@Query("post_id") postId: Long,
		@Query("owner_id") ownerId: Long,
		@Query("offset") offset: Int,
		@Query("count") count: Int): Observable<ApiResponse<CommentsList>>
	
	@GET("/wall.getComments?need_likes=1&extended=1&sort=asc")
	public fun getCommentsRaw(
		//@Query("filters") String filters,
		//@Query("source_ids") String sourceIds,  // groups, friends, pages, following, <uid>
		@Query("post_id") postId: Long,
		@Query("owner_id") ownerId: Long,
		@Query("offset") offset: Int,
		@Query("count") count: Int): ApiResponse<CommentsList>
	
	
	
	/*owner_id	идентификатор пользователя или сообщества, которому принадлежат видеозаписи.

	Обратите внимание, идентификатор сообщества в параметре owner_id необходимо указывать со знаком "-" — например, owner_id=-1 соответствует идентификатору сообщества ВКонтакте API (club1)


	целое число, по умолчанию идентификатор текущего пользователя
	videos	перечисленные через запятую идентификаторы — идущие через знак подчеркивания id пользователей, которым принадлежат видеозаписи, и id самих видеозаписей. Если видеозапись принадлежит сообществу, то в качестве первого параметра используется -id сообщества.

	Пример значения videos:
			-4363_136089719,13245770_137352259



	Некоторые видеозаписи, идентификаторы которых могут быть получены через API, закрыты приватностью, и не будут получены. В этом случае следует использовать ключ доступа access_key в её идентификаторе.

	Пример значения videos:

			1_129207899_220df2876123d3542f, 6492_135055734_e0a9bcc31144f67fbd


	Поле access_key будет возвращено вместе с остальными данными видеозаписи в методах, которые возвращают видеозаписи, закрытые приватностью, но доступные в данном контексте. Например данное поле имеют видеозаписи, возвращаемые методом newsfeed.get.
	список строк, разделенных через запятую
	album_id	идентификатор альбома, видеозаписи из которого нужно вернуть.
			целое число
	width	требуемая ширина изображений видеозаписей в пикселах.
	Возможные значения — 130, 160 (по умолчанию), 320.
	целое число
	count	количество возвращаемых видеозаписей.
	положительное число, максимальное значение 200, по умолчанию 100
	offset	смещение относительно первой найденной видеозаписи для выборки определенного подмножества.
	положительное число
	extended	определяет, возвращать ли информацию о настройках приватности видео для текущего пользователя.
			флаг, может принимать значения 1 или 0*/
	
	@GET("/video.get")
	public fun getVideo(
		@Query("videos") videos: String, // 6492_135055734_e0a9bcc31144f67fbd
		@Query("extended") extended: Int?): Observable<ApiResponse<VideosList>>
	
	
	/*type	тип объекта.
	Возможные типы:

	post — запись на стене пользователя или группы;
	comment — комментарий к записи на стене;
	photo — фотография;
	audio — аудиозапись;
	video — видеозапись;
	note — заметка;
	photo_comment — комментарий к фотографии;
	video_comment — комментарий к видеозаписи;
	topic_comment — комментарий в обсуждении;


	строка, обязательный параметр
	owner_id	идентификатор владельца объекта.
	целое число, по умолчанию идентификатор текущего пользователя
	item_id	идентификатор объекта.
	положительное число, обязательный параметр
	access_key	ключ доступа в случае работы с приватными объектами.
			строка*/
	@GET("/likes.add")
	public fun like(
		@Query("item_id") id: Long,
		@Query("owner_id") ownerId: Long,
		@Query("type") type: String): Observable<ApiResponse<LikeResponse>>
	
	@GET("/likes.delete")
	public fun unlike(
		@Query("item_id") id: Long,
		@Query("owner_id") ownerId: Long,
		@Query("type") type: String): Observable<ApiResponse<LikeResponse>>
	
	@GET("/groups.get?extended=1")
	public fun getGroups(): Observable<ApiResponse<VKList<Group>>>
}