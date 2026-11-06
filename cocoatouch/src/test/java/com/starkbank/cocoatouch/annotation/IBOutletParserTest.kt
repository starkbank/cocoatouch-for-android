package com.starkbank.cocoatouch.annotation

import com.starkbank.cocoatouch.uikit.UILabel
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.Assert.assertEquals
import org.junit.Test

open class ParentOutletFixture : UIViewController() {

    @IBOutlet(1) lateinit var parentLabel: UILabel
}

class ChildOutletFixture : ParentOutletFixture() {

    @IBOutlet(2) lateinit var childLabel: UILabel
}

class IBOutletParserTest {

    @Test
    fun outletFieldsIncludeInheritedOutlets() {
        assertEquals(
            setOf("parentLabel", "childLabel"),
            IBOutletParser.outletFields(ChildOutletFixture::class.java).map { it.name }.toSet()
        )
    }
}
