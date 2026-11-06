package com.starkbank.cocoatouch.uikit

import android.widget.ImageView
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi

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

    @InternalCocoaTouchApi
    fun getImageView(): ImageView {
        return (widget as ImageView)
    }
}
