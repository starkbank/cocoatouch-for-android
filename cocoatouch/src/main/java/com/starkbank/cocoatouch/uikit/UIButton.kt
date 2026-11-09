package com.starkbank.cocoatouch.uikit
import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import androidx.core.graphics.drawable.DrawableCompat


open class UIButton: UIControl() {

    private var _tintColor: UIColor? = null

    internal fun resolvedTintColor(): UIColor =
        _tintColor ?: UIApplication.appearance.onActionColor(UITraitCollection.current)

    private fun getButton(): Button{
        return (widget as Button)
    }

    open fun setTitle(title: String, state: UIControlState) {
        getButton().text = title
    }

    open fun titleForState(state: UIControlState): String {
        return getButton().text.toString()
    }

    open fun setTitleColor(color: UIColor, state: UIControlState){
        val intColor = color.colorInt
        getButton().setTextColor(intColor)
        getButton().compoundDrawableTintList = ColorStateList.valueOf(intColor)
    }

    open fun setImage(image: UIImage, state: UIControlState){
        getButton().setBackgroundResource(image.imageRes)
        setTintColor(resolvedTintColor())
    }

    open fun setTintColor(color: UIColor){
        _tintColor = color
        val colorInd = color.colorInt
        val drawable = DrawableCompat.wrap(getButton().background)
        DrawableCompat.setTint(drawable, colorInd)
        getButton().background = drawable
    }
}