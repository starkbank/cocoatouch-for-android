package com.starkbank.cocoatouch.annotation

import com.starkbank.cocoatouch.uikit.UILabel
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

open class ParentOutletFixture : UIViewController() {

    @IBOutlet(1) lateinit var parentLabel: UILabel
}

class ChildOutletFixture : ParentOutletFixture() {

    @IBOutlet(2) lateinit var childLabel: UILabel
}

open class HidingParentOutletFixture : UIViewController() {

    @IBOutlet(1) open lateinit var label: UILabel
}

class HidingChildOutletFixture : HidingParentOutletFixture() {

    @IBOutlet(2) override lateinit var label: UILabel
}

class IBOutletParserTest {

    @Test
    fun outletFieldsIncludeInheritedOutlets() {
        assertEquals(
            setOf("parentLabel", "childLabel"),
            IBOutletParser.outletFields(ChildOutletFixture::class.java).map { it.name }.toSet()
        )
    }

    @Test
    fun subclassOutletHidesTheParentFieldWithTheSameName() {
        val labels = IBOutletParser.outletFields(HidingChildOutletFixture::class.java).filter { it.name == "label" }
        assertEquals(1, labels.size)
        assertEquals(HidingChildOutletFixture::class.java, labels.first().declaringClass)
    }

    @Test
    fun walkStopsBeforeUIResponder() {
        assertTrue(
            IBOutletParser.outletFields(ChildOutletFixture::class.java)
                .all { it.declaringClass != UIResponder::class.java }
        )
    }

    @Test
    fun outletFieldsAreComputedOncePerClass() {
        assertSame(
            IBOutletParser.outletFields(ChildOutletFixture::class.java),
            IBOutletParser.outletFields(ChildOutletFixture::class.java)
        )
    }
}
