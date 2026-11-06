package com.starkbank.cocoatouch.uikit

import android.content.res.Configuration


enum class UIUserInterfaceStyle { unspecified, light, dark }


class UITraitCollection(val userInterfaceStyle: UIUserInterfaceStyle) {

    companion object {

        var current: UITraitCollection = UITraitCollection(UIUserInterfaceStyle.unspecified)
            internal set

        internal fun from(uiMode: Int): UITraitCollection {
            return when (uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> UITraitCollection(UIUserInterfaceStyle.dark)
                Configuration.UI_MODE_NIGHT_NO -> UITraitCollection(UIUserInterfaceStyle.light)
                else -> UITraitCollection(UIUserInterfaceStyle.unspecified)
            }
        }
    }
}
