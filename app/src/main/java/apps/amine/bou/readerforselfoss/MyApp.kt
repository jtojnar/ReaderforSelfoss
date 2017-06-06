package apps.amine.bou.readerforselfoss

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.multidex.MultiDexApplication
import android.widget.ImageView
import com.crashlytics.android.Crashlytics
import com.github.stkent.amplify.tracking.Amplify
import io.fabric.sdk.android.Fabric
import com.anupcowkur.reservoir.Reservoir
import com.bumptech.glide.Glide
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import java.io.IOException


class MyApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG)
            Fabric.with(this, Crashlytics())

        Amplify.initSharedInstance(this)
                .setFeedbackEmailAddress(getString(R.string.feedback_email))
                .setAlwaysShow(BuildConfig.DEBUG)
                .applyAllDefaultRules()

        try {
            Reservoir.init(this, 4096) //in bytes
        } catch (e: IOException) {
            //failure
        }

        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView?, uri: Uri?, placeholder: Drawable?, tag: String?) {
                Glide.with(imageView?.context).load(uri).placeholder(placeholder).into(imageView)
            }

            override fun cancel(imageView: ImageView?) {
                Glide.clear(imageView)
            }

            override fun placeholder(ctx: Context?, tag: String?): Drawable {
                return applicationContext.resources.getDrawable(R.mipmap.ic_launcher)
            }
        })
    }
}