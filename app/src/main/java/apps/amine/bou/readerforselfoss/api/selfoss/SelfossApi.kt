package apps.amine.bou.readerforselfoss.api.selfoss


import android.content.Context
import apps.amine.bou.readerforselfoss.utils.Config
import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.DispatchingAuthenticator
import com.burgstaller.okhttp.basic.BasicAuthenticator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap


class SelfossApi(c: Context) {

    private val service: SelfossService
    private val config: Config = Config(c)
    private val userName: String
    private val password: String

    init {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpBuilder = OkHttpClient.Builder()
        val authCache = ConcurrentHashMap<String, CachingAuthenticator>()

        val httpUserName = config.httpUserLogin
        val httpPassword = config.httpUserPassword

        val credentials = Credentials(httpUserName, httpPassword)
        val basicAuthenticator = BasicAuthenticator(credentials)
        val digestAuthenticator = DigestAuthenticator(credentials)

        // note that all auth schemes should be registered as lowercase!
        val authenticator = DispatchingAuthenticator.Builder()
                .with("digest", digestAuthenticator)
                .with("basic", basicAuthenticator)
                .build()

        val client = httpBuilder
                .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(AuthenticationCacheInterceptor(authCache))
                .addInterceptor(interceptor)
                .build()


        val builder = GsonBuilder()
        builder.registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanTypeAdapter())

        val gson = builder
                .setLenient()
                .create()

        userName = config.userLogin
        password = config.userPassword
        val retrofit = Retrofit.Builder().baseUrl(config.baseUrl).client(client)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
        service = retrofit.create(SelfossService::class.java)
    }

    fun login(): Call<SuccessResponse> {
        return service.loginToSelfoss(config.userLogin, config.userPassword)
    }

    val readItems: Call<List<Item>>
        get() = getItems("read")

    val unreadItems: Call<List<Item>>
        get() = getItems("unread")

    val starredItems: Call<List<Item>>
        get() = getItems("starred")

    private fun getItems(type: String): Call<List<Item>> {
        return service.getItems(type, userName, password)
    }

    fun markItem(itemId: String): Call<SuccessResponse> {
        return service.markAsRead(itemId, userName, password)
    }

    fun unmarkItem(itemId: String): Call<SuccessResponse> {
        return service.unmarkAsRead(itemId, userName, password)
    }

    fun readAll(ids: List<String>): Call<SuccessResponse> {
        return service.markAllAsRead(ids, userName, password)
    }

    fun starrItem(itemId: String): Call<SuccessResponse> {
        return service.starr(itemId, userName, password)
    }


    fun unstarrItem(itemId: String): Call<SuccessResponse> {
        return service.unstarr(itemId, userName, password)
    }

    val stats: Call<Stats>
        get() = service.stats(userName, password)

    val tags: Call<List<Tag>>
        get() = service.tags(userName, password)

    fun update(): Call<String> {
        return service.update(userName, password)
    }

    val sources: Call<List<Sources>>
        get() = service.sources(userName, password)

    fun deleteSource(id: String): Call<SuccessResponse> {
        return service.deleteSource(id, userName, password)
    }

    fun spouts(): Call<Map<String, Spout>> {
        return service.spouts(userName, password)
    }

    fun createSource(title: String, url: String, spout: String, tags: String, filter: String): Call<SuccessResponse> {
        return service.createSource(title, url, spout, tags, filter, userName, password)
    }

}
