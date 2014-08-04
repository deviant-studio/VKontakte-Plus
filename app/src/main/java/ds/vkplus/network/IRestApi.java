package ds.vkplus.network;

import ds.vkplus.network.model.*;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

interface IRestApi {

	@GET("/newsfeed.get")
	ApiResponse<NewsResponse> getNews(
			@Query("filters") String filters,
			@Query("source_ids") String sourceIds,  // groups, friends, pages, following, <uid>
			@Query("start_from") String next,
			@Query("count") int count);             // max=100

	@GET("/newsfeed.get")
	Observable<ApiResponse<NewsResponse>> getNews2(
			@Query("filters") String filters,
			@Query("source_ids") String sourceIds,  // groups, friends, pages, following, <uid>
			@Query("start_from") String next,
			@Query("count") int count);             // max=100


	/*
	owner_id	идентификатор владельца страницы (пользователь или сообщество).

	Обратите внимание, идентификатор сообщества в параметре owner_id необходимо указывать со знаком "-" — например, owner_id=-1 соответствует идентификатору сообщества ВКонтакте API (club1)


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
	@GET("/newsfeed.get?need_likes=1&extended=1&sort=asc")
	Observable<ApiResponse<CommentsList>> getComments(
			//@Query("filters") String filters,
			//@Query("source_ids") String sourceIds,  // groups, friends, pages, following, <uid>
			@Query("post_id") long postId,
			@Query("owner_id") long ownerId,
			@Query("offset") int offset,
			@Query("count") int count);

}