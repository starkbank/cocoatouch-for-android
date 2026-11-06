package com.starkbank.cocoatouch.uikit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import java.io.ByteArrayOutputStream

class UIImageView : UIView() {

    var tintColor: UIColor? = null
        set(value) {
            field = value
            value?.let { getImageView().setColorFilter(it.colorInt)}
        }

    var image: UIImage? = null
        set(value) {
            field = value
            value?.imageRes?.let {
                getImageView().setImageResource(it)
            }
        }

    internal fun getImageView(): ImageView {
        return (widget as ImageView)
    }

    private val IMG_WIDTH = 640
    private val IMG_HEIGHT = 480

    private fun resizeBase64Image(base64image: String): String {
        val encodeByte: ByteArray = Base64.decode(base64image.toByteArray(), Base64.DEFAULT)
        val options = BitmapFactory.Options()
        var image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size, options)
        if (image.height == 400 && image.width == 400) {
            return base64image
        }
        image = Bitmap.createScaledBitmap(image, IMG_WIDTH, IMG_HEIGHT, false)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        System.gc()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

}