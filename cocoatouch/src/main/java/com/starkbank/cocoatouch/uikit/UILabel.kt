package com.starkbank.cocoatouch.uikit
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat


open class UILabel : UIView() {

    var textColor: UIColor? = null
    set(value) {
        field = value
        value?.let { getTextView().setTextColor(it.colorInt) }
    }

    var text: String?
        get() = getTextView().text.toString()
        set(text) {
            getTextView().text = text
        }

    var font: UIFont? = null
        set(font) {
            font?.let {
                val typeface = ResourcesCompat.getFont(UIApplication.context(), it.name)
                getTextView().textSize = it.size
                getTextView().typeface = typeface
            }
            field = font
        }

    private fun getTextView(): TextView {
        return (this.widget as TextView)
    }

    override fun setHidden(hidden: Boolean) {
        super.setHidden(hidden)
        getTextView().visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }
}