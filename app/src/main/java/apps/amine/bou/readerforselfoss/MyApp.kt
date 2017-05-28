package apps.amine.bou.readerforselfoss

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.github.stkent.amplify.tracking.Amplify
import io.fabric.sdk.android.Fabric


class MyApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG)
            Fabric.with(this, Crashlytics())

        Amplify.initSharedInstance(this)
                .setFeedbackEmailAddress(getString(R.string.feedback_email))
                .setAlwaysShow(BuildConfig.DEBUG)
                .applyAllDefaultRules()
    }
}