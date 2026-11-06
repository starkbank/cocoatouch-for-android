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

open class ShadowBase : UIViewController() {

    @IBOutlet(1) private lateinit var label: UILabel

    fun baseLabel(): UILabel = label
}

class ShadowChild : ShadowBase() {

    @IBOutlet(1) lateinit var label: UILabel
}

open class NullableBase : UIViewController() {

    @IBOutlet(1) open var label: UILabel? = null
}

class NullableChild : NullableBase() {

    @IBOutlet(2) override var label: UILabel? = null
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
    fun aPrivateParentFieldAndASubclassFieldWithTheSameNameAreBothOutlets() {
        val labels = IBOutletParser.outletFields(ShadowChild::class.java).filter { it.name == "label" }
        assertEquals(
            setOf(ShadowBase::class.java, ShadowChild::class.java),
            labels.map { it.declaringClass }.toSet()
        )
    }

    @Test
    fun anOverriddenNullablePropertyIsBoundOnlyOnTheSubclass() {
        val labels = IBOutletParser.outletFields(NullableChild::class.java).filter { it.name == "label" }
        assertEquals(1, labels.size)
        assertEquals(NullableChild::class.java, labels.first().declaringClass)
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
