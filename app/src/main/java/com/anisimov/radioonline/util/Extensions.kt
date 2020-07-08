package com.anisimov.radioonline.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.View.VISIBLE
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL

fun ImageView.setImageFromUrl(
    url: String?,
    width: Int = this.width,
    blurTo: ImageView? = null
) {
    val iv = this
    var w = width
    CoroutineScope(Dispatchers.Main).launch {
        while (w <= 0) {
            delay(10)
            w = iv.width
        }
        url?.let {
            try {
                val requestListener = object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        resource?.let { initBlur(resource) }
                        return false
                    }

                    private fun initBlur(bitmap: Bitmap?) {
                        if (blurTo != null) {
                            Blurry.with(context).radius(10).sampling(8)
                                .color(Color.argb(100, 100, 100, 100))
                                .async().from(bitmap).into(blurTo)
                        }
                    }
                }

                Glide.with(iv).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .listener(requestListener)
                    .load(it).override(width).into(iv)

                visibility = VISIBLE
            } catch (e: Exception) {
                Log.e("ImageView", "setImageFromUrl: ${e.localizedMessage}")
            }
        }
    }
}

fun getBitmapFromUrl(
    url: String?
): Bitmap? {
    url?.let {
        try {
            val readBytes = URL(url).openStream().readBytes()
            return BitmapFactory.decodeByteArray(readBytes, 0, readBytes.size)
        } catch (e: Exception) {
        }
    }
    return null
}