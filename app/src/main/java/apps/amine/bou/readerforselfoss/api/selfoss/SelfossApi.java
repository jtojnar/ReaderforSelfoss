package apps.amine.bou.readerforselfoss.api.selfoss;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

import apps.amine.bou.readerforselfoss.utils.Config;
import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.DispatchingAuthenticator;
import com.burgstaller.okhttp.basic.BasicAuthenticator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SelfossApi {

    private final SelfossService service;
    private final Config config;
    private final String userName;
    private final String password;

    public SelfossApi(Context c) {
        this.config = new Config(c);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();

        String httpUserName = config.getHttpUserLogin();
        String httpPassword = config.getHttpUserPassword();

        Credentials credentials = new Credentials(httpUserName, httpPassword);
        final BasicAuthenticator basicAuthenticator = new BasicAuthenticator(credentials);
        final DigestAuthenticator digestAuthenticator = new DigestAuthenticator(credentials);

        // note that all auth schemes should be registered as lowercase!
        DispatchingAuthenticator authenticator = new DispatchingAuthenticator.Builder()
                .with("digest", digestAuthenticator)
                .with("basic", basicAuthenticator)
                .build();

        OkHttpClient client = httpBuilder
                .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                .addInterceptor(interceptor)
                .build();


        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(boolean.class, new BooleanTypeAdapter());

        Gson gson = builder
                .setLenient()
                .create();

        userName = config.getUserLogin();
        password = config.getUserPassword();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getBaseUrl()).client(client)
                .addConverterFactory(GsonConverterFactory.create(gson)).build();
        service = retrofit.create(SelfossService.class);
    }

    public Call<SuccessResponse> login() {
        return service.loginToSelfoss(config.getUserLogin(), config.getUserPassword());
    }

    public Call<List<Item>> getReadItems() {
        return getItems("read");
    }

    public Call<List<Item>> getUnreadItems() {
        return getItems("unread");
    }

    public Call<List<Item>> getStarredItems() {
        return getItems("starred");
    }

    private Call<List<Item>> getItems(String type) {
        return service.getItems(type, userName, password);
    }

    public Call<SuccessResponse> markItem(String itemId) {
        return service.markAsRead(itemId, userName, password);
    }

    public Call<SuccessResponse> unmarkItem(String itemId) {
        return service.unmarkAsRead(itemId, userName, password);
    }

    public Call<SuccessResponse> readAll(List<String> ids) {
        return service.markAllAsRead(ids, userName, password);
    }

    public Call<SuccessResponse> starrItem(String itemId) {
        return service.starr(itemId, userName, password);
    }


    public Call<SuccessResponse> unstarrItem(String itemId) {
        return service.unstarr(itemId, userName, password);
    }

    public Call<Stats> getStats() {
        return service.stats(userName, password);
    }

    public Call<List<Tag>> getTags() {
        return service.tags(userName, password);
    }

    public Call<String> update() {
        return service.update(userName, password);
    }

    public Call<List<Sources>> getSources() { return service.sources(userName, password); }

    public Call<SuccessResponse> deleteSource(String id) { return service.deleteSource(id, userName, password);}

    public Call<Map<String, Spout>> spouts() { return service.spouts(userName, password); }

    public Call<SuccessResponse> createSource(String title, String url, String spout, String tags, String filter) {return service.createSource(title, url, spout, tags, filter, userName, password);}

}
