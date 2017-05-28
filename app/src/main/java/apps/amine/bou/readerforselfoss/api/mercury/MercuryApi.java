package apps.amine.bou.readerforselfoss.api.mercury;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MercuryApi {
    private final MercuryService service;
    private final String key;

    public MercuryApi(String key) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        this.key = key;
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://mercury.postlight.com").client(client)
                .addConverterFactory(GsonConverterFactory.create(gson)).build();
        service = retrofit.create(MercuryService.class);
    }

    public Call<ParsedContent> parseUrl(String url) {
        return service.parseUrl(url, this.key);
    }
}
