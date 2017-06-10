package apps.amine.bou.readerforselfoss.api.mercury

import android.os.Parcel
import android.os.Parcelable



class ParsedContent(val title: String,
                    val content: String,
                    val date_published: String,
                    val lead_image_url: String,
                    val dek: String,
                    val url: String,
                    val domain: String,
                    val excerpt: String,
                    val total_pages: Int,
                    val rendered_pages: Int,
                    val next_page_url: String) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ParsedContent> = object : Parcelable.Creator<ParsedContent> {
            override fun createFromParcel(source: Parcel): ParsedContent = ParsedContent(source)
            override fun newArray(size: Int): Array<ParsedContent?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
        title = source.readString(),
        content = source.readString(),
        date_published = source.readString(),
        lead_image_url = source.readString(),
        dek = source.readString(),
        url = source.readString(),
        domain = source.readString(),
        excerpt = source.readString(),
        total_pages = source.readInt(),
        rendered_pages = source.readInt(),
        next_page_url = source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(content)
        dest.writeString(date_published)
        dest.writeString(lead_image_url)
        dest.writeString(dek)
        dest.writeString(url)
        dest.writeString(domain)
        dest.writeString(excerpt)
        dest.writeInt(total_pages)
        dest.writeInt(rendered_pages)
        dest.writeString(next_page_url)
    }
}
