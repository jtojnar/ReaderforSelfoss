package apps.amine.bou.readerforselfoss.api.selfoss;


import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


interface SelfossService {
    @GET("login")
    Call<SuccessResponse> loginToSelfoss(@Query("username") String username, @Query("password") String password);

    @GET("items")
    Call<List<Item>> getItems(@Query("type") String type, @Query("username") String username, @Query("password") String password);

    @POST("mark/{id}")
    Call<SuccessResponse> markAsRead(@Path("id") String id, @Query("username") String username, @Query("password") String password);


    @POST("unmark/{id}")
    Call<SuccessResponse> unmarkAsRead(@Path("id") String id, @Query("username") String username, @Query("password") String password);

    @FormUrlEncoded
    @POST("mark")
    Call<SuccessResponse> markAllAsRead(@Field("ids[]") List<String> ids, @Query("username") String username, @Query("password") String password);


    @POST("starr/{id}")
    Call<SuccessResponse> starr(@Path("id") String id, @Query("username") String username, @Query("password") String password);


    @POST("unstarr/{id}")
    Call<SuccessResponse> unstarr(@Path("id") String id, @Query("username") String username, @Query("password") String password);


    @GET("stats")
    Call<Stats> stats(@Query("username") String username, @Query("password") String password);


    @GET("tags")
    Call<List<Tag>> tags(@Query("username") String username, @Query("password") String password);


    @GET("update")
    Call<String> update(@Query("username") String username, @Query("password") String password);

    @GET("sources/spouts")
    Call<Map<String, Spout>> spouts(@Query("username") String username, @Query("password") String password);

    @GET("sources/list")
    Call<List<Sources>> sources(@Query("username") String username, @Query("password") String password);


    @DELETE("source/{id}")
    Call<SuccessResponse> deleteSource(@Path("id") String id, @Query("username") String username, @Query("password") String password);

    @FormUrlEncoded
    @POST("source")
    Call<SuccessResponse> createSource(@Field("title") String title, @Field("url") String url, @Field("spout") String spout, @Field("tags") String tags, @Field("filter") String filter, @Query("username") String username, @Query("password") String password);
}
