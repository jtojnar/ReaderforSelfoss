package apps.amine.bou.readerforselfoss.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Patterns

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import okhttp3.HttpUrl

import apps.amine.bou.readerforselfoss.BuildConfig
import apps.amine.bou.readerforselfoss.R
import apps.amine.bou.readerforselfoss.api.selfoss.Item


fun checkAndDisplayStoreApk(context: Context) = {
    fun isStoreVersion(context: Context): Boolean {
        var result = false
        try {
            val installer = context.packageManager
                .getInstallerPackageName(context.packageName)
            result = !TextUtils.isEmpty(installer)
        } catch (e: Throwable) {
        }

        return result
    }

    if (!isStoreVersion(context) && !BuildConfig.GITHUB_VERSION) {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(context.getString(R.string.warning_version))
        alertDialog.setMessage(context.getString(R.string.text_version))
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
            { dialog, _ -> dialog.dismiss() })
        alertDialog.show()
    } else Unit
}

fun isUrlValid(url: String): Boolean {
    val baseUrl = HttpUrl.parse(url)
    var existsAndEndsWithSlash = false
    if (baseUrl != null) {
        val pathSegments = baseUrl.pathSegments()
        existsAndEndsWithSlash = "" == pathSegments[pathSegments.size - 1]
    }

    return Patterns.WEB_URL.matcher(url).matches() && existsAndEndsWithSlash
}

fun isEmptyOrNullOrNullString(str: String?): Boolean =
    str == null || str == "null" || str.isEmpty()

fun checkApkVersion(settings: SharedPreferences,
                    editor: SharedPreferences.Editor,
                    context: Context,
                    mFirebaseRemoteConfig: FirebaseRemoteConfig) = {
    fun isThereAnUpdate() {
        val APK_LINK = "github_apk"

        val apkLink = mFirebaseRemoteConfig.getString(APK_LINK)
        val storedLink = settings.getString(APK_LINK, "")
        if (apkLink != storedLink && !apkLink.isEmpty()) {
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle(context.getString(R.string.new_apk_available_title))
            alertDialog.setMessage(context.getString(R.string.new_apk_available_message))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.new_apk_available_get)) { _, _ ->
                editor.putString(APK_LINK, apkLink)
                editor.apply()
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(apkLink))
                context.startActivity(browserIntent)
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.new_apk_available_no),
                { dialog, _ ->
                    editor.putString(APK_LINK, apkLink)
                    editor.apply()
                    dialog.dismiss()
                })
            alertDialog.show()
        }

    }

    mFirebaseRemoteConfig.fetch(43200)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mFirebaseRemoteConfig.activateFetched()
            }

            isThereAnUpdate()
        }
}

fun longHash(string: String): Long {
    var h = 98764321261L
    val l = string.length
    val chars = string.toCharArray()

    for (i in 0..l - 1) {
        h = 31 * h + chars[i].toLong()
    }
    return h
}

fun handleLinksWithoutHttp(itemUrl: String) =
    if (!itemUrl.startsWith("https://") && !itemUrl.startsWith("http://"))
        "http://" + itemUrl
    else
        itemUrl

fun shareLink(itemUrl: String, c: Context) {
    val sendIntent = Intent()
    sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_TEXT, handleLinksWithoutHttp(itemUrl))
    sendIntent.type = "text/plain"
    c.startActivity(Intent.createChooser(sendIntent, c.getString(R.string.share)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun openInBrowser(i: Item, c: Context) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data = Uri.parse(handleLinksWithoutHttp(i.getLinkDecoded()))
    c.startActivity(intent)
}