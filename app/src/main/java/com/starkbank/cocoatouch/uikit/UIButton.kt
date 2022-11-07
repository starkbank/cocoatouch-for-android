package com.starkbank.cocoatouch.uikit
import android.view.View
import android.widget.Button


open class UIButton: UIControl() {

    open fun setTitle(title: String, state: UIControlState) {
        (this.widget as Button).text = title
    }

    open fun titleForState(state: UIControlState): String {
        return (this.widget as Button).text.toString()
    }

    override fun setHidden(hidden: Boolean) {
        super.setHidden(hidden)
        (this.widget as Button).visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }
}