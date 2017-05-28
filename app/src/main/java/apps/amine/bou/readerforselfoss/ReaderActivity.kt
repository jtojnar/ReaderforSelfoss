package apps.amine.bou.readerforselfoss

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import apps.amine.bou.readerforselfoss.api.mercury.MercuryApi
import apps.amine.bou.readerforselfoss.api.mercury.ParsedContent
import apps.amine.bou.readerforselfoss.utils.buildCustomTabsIntent
import apps.amine.bou.readerforselfoss.utils.customtabs.CustomTabActivityHelper
import com.bumptech.glide.Glide
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import xyz.klinker.android.drag_dismiss.activity.DragDismissActivity


class ReaderActivity : DragDismissActivity() {
    private var mCustomTabActivityHelper: CustomTabActivityHelper? = null

    override fun onStart() {
        super.onStart()
        mCustomTabActivityHelper!!.bindCustomTabsService(this)
    }

    override fun onStop() {
        super.onStop()
        mCustomTabActivityHelper!!.unbindCustomTabsService(this)
    }

    override fun onCreateContent(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.activity_reader, parent, false)
        showProgressBar()

        val image = v.findViewById(R.id.imageView) as ImageView
        val source = v.findViewById(R.id.source) as TextView
        val title = v.findViewById(R.id.title) as TextView
        val content = v.findViewById(R.id.content) as HtmlTextView
        val url = intent.getStringExtra("url")
        val parser = MercuryApi(getString(R.string.mercury))

        val customTabsIntent = buildCustomTabsIntent(this@ReaderActivity)
        mCustomTabActivityHelper = CustomTabActivityHelper()
        mCustomTabActivityHelper!!.bindCustomTabsService(this)


        parser.parseUrl(url).enqueue(object : Callback<ParsedContent> {
            override fun onResponse(call: Call<ParsedContent>, response: Response<ParsedContent>) {
                if (response.body() != null) {
                    source.text = response.body().domain
                    title.text = response.body().title
                    if (response.body().content != null && !response.body().content.isEmpty())
                        content.setHtml(response.body().content, HtmlHttpImageGetter(content, null, true))
                    if (response.body().lead_image_url != null && !response.body().lead_image_url.isEmpty())
                        Glide.with(applicationContext).load(response.body().lead_image_url).asBitmap().fitCenter().into(image)
                    hideProgressBar()
                } else {
                    errorAfterMercuryCall()
                }
            }

            override fun onFailure(call: Call<ParsedContent>, t: Throwable) {
                errorAfterMercuryCall()
            }

            private fun errorAfterMercuryCall() {
                CustomTabActivityHelper.openCustomTab(this@ReaderActivity, customTabsIntent, Uri.parse(url)
                ) { _, uri ->
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                finish()
            }
        })
        return v
    }
}