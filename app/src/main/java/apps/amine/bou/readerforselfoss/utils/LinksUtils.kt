package apps.amine.bou.readerforselfoss.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import apps.amine.bou.readerforselfoss.R
import apps.amine.bou.readerforselfoss.ReaderActivity
import apps.amine.bou.readerforselfoss.api.selfoss.Item
import apps.amine.bou.readerforselfoss.utils.customtabs.CustomTabActivityHelper
import xyz.klinker.android.drag_dismiss.DragDismissIntentBuilder

fun buildCustomTabsIntent(c: Context): CustomTabsIntent {

    fun createPendingShareIntent(c: Context): PendingIntent {
        val actionIntent = Intent(Intent.ACTION_SEND)
        actionIntent.type = "text/plain"
        return PendingIntent.getActivity(
                c, 0, actionIntent, 0)
    }

    val intentBuilder = CustomTabsIntent.Builder()

    // TODO: change to primary when it's possible to customize custom tabs title color
    //intentBuilder.setToolbarColor(c.getResources().getColor(R.color.colorPrimary));
    intentBuilder.setToolbarColor(c.resources.getColor(R.color.colorAccentDark))
    intentBuilder.setShowTitle(true)


    intentBuilder.setStartAnimations(c,
            R.anim.slide_in_right,
            R.anim.slide_out_left)
    intentBuilder.setExitAnimations(c,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)

    val closeicon = BitmapFactory.decodeResource(c.resources, R.drawable.ic_close_white_24dp)
    intentBuilder.setCloseButtonIcon(closeicon)

    val shareLabel = c.getString(R.string.label_share)
    val icon = BitmapFactory.decodeResource(c.resources,
            R.drawable.ic_share_white_24dp)
    intentBuilder.setActionButton(icon, shareLabel, createPendingShareIntent(c))

    return intentBuilder.build()
}

fun openItemUrl(i: Item,
                customTabsIntent: CustomTabsIntent,
                internalBrowser: Boolean,
                articleViewer: Boolean,
                app: Activity,
                c: Context) {
    if (!internalBrowser) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(i.getLinkDecoded())
        app.startActivity(intent)
    } else {
        if (articleViewer) {
            val intent = Intent(c, ReaderActivity::class.java)

            DragDismissIntentBuilder(c)
                    .setFullscreenOnTablets(true)      // defaults to false, tablets will have padding on each side
                    .setDragElasticity(DragDismissIntentBuilder.DragElasticity.NORMAL)  // Larger elasticities will make it easier to dismiss.
                    .build(intent)

            intent.putExtra("url", i.getLinkDecoded())
            app.startActivity(intent)
        } else {
            CustomTabActivityHelper.openCustomTab(app, customTabsIntent, Uri.parse(i.getLinkDecoded())
            ) { _, uri ->
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                c.startActivity(intent)
            }
        }
    }
}
