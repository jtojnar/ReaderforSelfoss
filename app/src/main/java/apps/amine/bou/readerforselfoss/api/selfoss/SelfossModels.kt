package apps.amine.bou.readerforselfoss.api.selfoss

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

import apps.amine.bou.readerforselfoss.utils.Config
import apps.amine.bou.readerforselfoss.utils.isEmptyOrNullOrNullString



private fun constructUrl(config: Config?, path: String, file: String): String {
    val baseUriBuilder = Uri.parse(config!!.baseUrl).buildUpon()
    baseUriBuilder.appendPath(path).appendPath(file)

    return if (file.isEmptyOrNullOrNullString()) ""
    else baseUriBuilder.toString()
}


data class Tag(val tag: String, val color: String, val unread: Int)

class SuccessResponse(val success: Boolean) {
    val isSuccess: Boolean
        get() = success
}

class Stats(val total: Int, val unread: Int, val starred: Int)

data class Spout(val name: String, val description: String)

data class Sources(val id: String,
                   val title: String,
                   val tags: String,
                   val spout: String,
                   val error: String,
                   val icon: String) {
    var config: Config? = null

    fun getIcon(app: Context): String {
        if (config == null) {
            config = Config(app)
        }
        return constructUrl(config,"favicons", icon)
    }

}

data class Item(val id: String,
                val datetime: String,
                val title: String,
                val unread: Boolean,
                val starred: Boolean,
                val thumbnail: String,
                val icon: String,
                val link: String,
                val sourcetitle: String) : Parcelable {

    var config: Config? = null

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Item> = object : Parcelable.Creator<Item> {
            override fun createFromParcel(source: Parcel): Item = Item(source)
            override fun newArray(size: Int): Array<Item?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
        id = source.readString(),
        datetime = source.readString(),
        title = source.readString(),
        unread = 0.toByte() != source.readByte(),
        starred = 0.toByte() != source.readByte(),
        thumbnail = source.readString(),
        icon = source.readString(),
        link = source.readString(),
        sourcetitle = source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(datetime)
        dest.writeString(title)
        dest.writeByte((if (unread) 1 else 0))
        dest.writeByte((if (starred) 1 else 0))
        dest.writeString(thumbnail)
        dest.writeString(icon)
        dest.writeString(link)
        dest.writeString(sourcetitle)
    }

    fun getIcon(app: Context): String {
        if (config == null) {
            config = Config(app)
        }
        return constructUrl(config, "favicons", icon)
    }

    fun getThumbnail(app: Context): String {
        if (config == null) {
            config = Config(app)
        }
        return constructUrl(config, "thumbnails", thumbnail)
    }

    // TODO: maybe find a better way to handle these kind of urls
    fun getLinkDecoded(): String {
        var stringUrl: String
        if (link.startsWith("http://news.google.com/news/") || link.startsWith("https://news.google.com/news/")) {
            if (link.contains("&amp;url=")) {
                stringUrl = link.substringAfter("&amp;url=")
            } else {
                stringUrl = this.link.replace("&amp;", "&")
            }
        } else {
            stringUrl = this.link.replace("&amp;", "&")
        }

        // handle :443 => https
        if (stringUrl.contains(":443")) {
            stringUrl = stringUrl.replace(":443", "").replace("http://", "https://")
        }
        return stringUrl
    }

}