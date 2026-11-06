package com.starkbank.cocoatouch.uikit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test


class UIAppearanceDefaultsTest {

    private val appearance = object : UIAppearance {}
    private val traits = UITraitCollection(UIUserInterfaceStyle.light)

    @Test
    fun anImplementerWithoutOverridesGetsTheDefaultColors() {
        assertEquals("#FFFFFF", appearance.onActionColor(traits).hex)
        assertEquals("#CCCCCC", appearance.borderColor(traits).hex)
        assertEquals("#EFEFF0", appearance.secondaryBackgroundColor(traits).hex)
    }

    @Test
    fun anImplementerWithoutOverridesGetsNoSectionHeaderFont() {
        assertNull(appearance.sectionHeaderFont(traits))
    }
}
