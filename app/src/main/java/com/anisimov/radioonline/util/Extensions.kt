package com.anisimov.radioonline.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View.VISIBLE
import android.widget.ImageView
import com.anisimov.radioonline.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun ImageView.setImageFromUrl(
    url: String?,
    width: Int = this.width,
    blurTo: ImageView? = null
) {
    val iv = this
    var w = width
    CoroutineScope(Dispatchers.Main).launch{
        while (w <= 0) {
            delay(10)
            w = iv.width
        }
        url?.let {
            try {
                Glide.with(iv).asBitmap().placeholder(R.drawable.ic_launcher_foreground).error(
                    R.drawable.ic_launcher_foreground
                ).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .load(it).override(this@setImageFromUrl.width).into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        try {
                            this@setImageFromUrl.setImageBitmap(resource)
                            if (blurTo != null) {
                                Blurry.with(context)
                                    .radius(10)
                                    .sampling(8)
                                    .color(Color.argb(100, 100, 100, 100))
                                    .async()
                                    .from(resource)
                                    .into(blurTo)
                            }
                        } catch (e: Exception) {
                            Log.e("ERROR", "err: ${e.localizedMessage}")
                            setImageFromUrl(url)
                        }
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
                visibility = VISIBLE
            } catch (e: Exception) {
                Log.e("ImageView", "setImageFromUrl: ${e.localizedMessage}")
            }
        }
    }
}