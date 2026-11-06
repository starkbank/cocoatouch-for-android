package com.starkbank.cocoatouch.uikit

import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Test


class UITraitCollectionTest {

    @Test
    fun nightYesIsDark() {
        assertEquals(
            UIUserInterfaceStyle.dark,
            UITraitCollection.from(Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_YES).userInterfaceStyle
        )
    }

    @Test
    fun nightNoIsLight() {
        assertEquals(
            UIUserInterfaceStyle.light,
            UITraitCollection.from(Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_NO).userInterfaceStyle
        )
    }

    @Test
    fun nightUndefinedIsUnspecified() {
        assertEquals(
            UIUserInterfaceStyle.unspecified,
            UITraitCollection.from(Configuration.UI_MODE_NIGHT_UNDEFINED).userInterfaceStyle
        )
    }
}
