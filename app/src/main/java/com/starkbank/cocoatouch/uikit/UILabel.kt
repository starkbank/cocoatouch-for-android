package com.starkbank.cocoatouch.uikit
import android.view.View
import android.widget.TextView


open class UILabel : UIView() {

    var text: String
        get() = (this.widget as TextView).text.toString()
        set(text) {
            (this.widget as TextView).text = text
        }

    override fun setHidden(hidden: Boolean) {
        super.setHidden(hidden)
        (this.widget as TextView).visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }
}