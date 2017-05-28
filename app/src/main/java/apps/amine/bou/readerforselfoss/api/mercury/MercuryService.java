package apps.amine.bou.readerforselfoss.api.mercury;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;


public interface MercuryService {
    @GET("parser")
    Call<ParsedContent> parseUrl(@Query("url") String url, @Header("x-api-key") String key);
}
