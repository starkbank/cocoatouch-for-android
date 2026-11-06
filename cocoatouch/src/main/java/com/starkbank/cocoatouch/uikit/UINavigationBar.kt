package com.starkbank.cocoatouch.uikit

import androidx.appcompat.widget.Toolbar


class UINavigationBar: UIView() {

    override var backgroundColor: UIColor? = null
        get() = super.backgroundColor
        set(value) {
            field = value
            value?.let { getToolbar().setBackgroundColor(value.colorInt) }
        }

    private fun getToolbar(): Toolbar {
        return (this.widget as Toolbar)
    }
}