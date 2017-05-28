package apps.amine.bou.readerforselfoss.utils

import android.content.Context
import android.content.SharedPreferences


class Config(c: Context) {

    private val settings: SharedPreferences

    init {
        this.settings = c.getSharedPreferences(settingsName, Context.MODE_PRIVATE)
    }

    val baseUrl: String
        get() = settings.getString("url", "")

    val userLogin: String
        get() = settings.getString("login", "")

    val userPassword: String
        get() = settings.getString("password", "")

    val httpUserLogin: String
        get() = settings.getString("httpUserName", "")

    val httpUserPassword: String
        get() = settings.getString("httpPassword", "")

    companion object {
        val settingsName = "paramsselfoss"

    }
}
