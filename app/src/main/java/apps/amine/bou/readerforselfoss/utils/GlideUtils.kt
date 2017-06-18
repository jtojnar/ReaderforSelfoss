package apps.amine.bou.readerforselfoss.utils

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget


fun Context.bitmapCenterCrop(url: String, iv: ImageView) =
    Glide.with(this).load(url).asBitmap().centerCrop().into(iv)

fun Context.bitmapFitCenter(url: String, iv: ImageView) =
    Glide.with(this).load(url).asBitmap().fitCenter().into(iv)

fun Context.circularBitmapDrawable(url: String, iv: ImageView) =
    Glide.with(this).load(url).asBitmap().centerCrop().into(object : BitmapImageViewTarget(iv) {
        override fun setResource(resource: Bitmap) {
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
            circularBitmapDrawable.isCircular = true
            iv.setImageDrawable(circularBitmapDrawable)
        }
    })