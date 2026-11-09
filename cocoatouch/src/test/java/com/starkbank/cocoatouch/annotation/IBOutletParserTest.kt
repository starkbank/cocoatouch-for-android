package com.starkbank.cocoatouch.annotation

import android.content.Context
import android.widget.FrameLayout
import com.starkbank.cocoatouch.nullOf
import com.starkbank.cocoatouch.uikit.UILabel
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UISwitch
import com.starkbank.cocoatouch.uikit.UIView
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
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

open class InternalBase : UIViewController() {

    @IBOutlet(1) internal open var label: UILabel? = null
}

class InternalChild : InternalBase() {

    @IBOutlet(2) override var label: UILabel? = null
}

open class IsBase : UIViewController() {

    @IBOutlet(1) open var isEnabledSwitch: UISwitch? = null
}

class IsChild : IsBase() {

    @IBOutlet(2) override var isEnabledSwitch: UISwitch? = null
}

open class GrandparentOutletFixture : UIViewController() {

    @IBOutlet(1) open var label: UILabel? = null
}

open class MiddleOutletFixture : GrandparentOutletFixture() {

    @IBOutlet(2) override var label: UILabel? = null
}

class GrandchildOutletFixture : MiddleOutletFixture() {

    @IBOutlet(3) override var label: UILabel? = null
}

class SingleOutletFixture : UIViewController() {

    @IBOutlet(1) lateinit var label: UILabel
}

class IBOutletParserTest {

    @Test
    fun outletFieldsIncludeInheritedOutlets() {
        assertEquals(
            setOf("parentLabel", "childLabel"),
            IBOutletParser.outletFields(ChildOutletFixture::class.java).map { it.field.name }.toSet()
        )
    }

    @Test
    fun aSubclassOutletShadowsTheParentFieldWithTheSameName() {
        assertShadowsParent(HidingChildOutletFixture::class.java, HidingParentOutletFixture::class.java, "label")
    }

    @Test
    fun aPrivateParentFieldAndASubclassFieldWithTheSameNameAreBothOutlets() {
        assertShadowsParent(ShadowChild::class.java, ShadowBase::class.java, "label")
    }

    @Test
    fun anOverriddenNullablePropertyShadowsTheParentField() {
        assertShadowsParent(NullableChild::class.java, NullableBase::class.java, "label")
    }

    @Test
    fun anOverriddenInternalPropertyShadowsTheParentField() {
        assertShadowsParent(InternalChild::class.java, InternalBase::class.java, "label")
    }

    @Test
    fun anIsPrefixedOverriddenPropertyShadowsTheParentField() {
        assertShadowsParent(IsChild::class.java, IsBase::class.java, "isEnabledSwitch")
    }

    @Test
    fun onlyTheMostDerivedOutletIsUnshadowedAcrossThreeLevels() {
        val entries = IBOutletParser.outletFields(GrandchildOutletFixture::class.java)
        assertEquals(3, entries.size)
        assertFalse(entries.single { it.field.declaringClass == GrandchildOutletFixture::class.java }.isShadowed)
        assertTrue(entries.single { it.field.declaringClass == MiddleOutletFixture::class.java }.isShadowed)
        assertTrue(entries.single { it.field.declaringClass == GrandparentOutletFixture::class.java }.isShadowed)
    }

    @Test
    fun anUnshadowedOutletWhoseIdIsAbsentThrows() {
        val fixture = SingleOutletFixture()
        fixture.view = UIView().apply { widget = FrameLayout(nullOf<Context>()) }
        val error = assertThrows(IllegalStateException::class.java) { IBOutletParser.parse(fixture) }
        assertTrue(error.message!!.contains("not found"))
    }

    @Test
    fun walkStopsBeforeUIResponder() {
        assertTrue(
            IBOutletParser.outletFields(ChildOutletFixture::class.java)
                .all { it.field.declaringClass != UIResponder::class.java }
        )
    }

    @Test
    fun outletFieldsAreComputedOncePerClass() {
        assertSame(
            IBOutletParser.outletFields(ChildOutletFixture::class.java),
            IBOutletParser.outletFields(ChildOutletFixture::class.java)
        )
    }

    private fun assertShadowsParent(child: Class<*>, parent: Class<*>, name: String) {
        val entries = IBOutletParser.outletFields(child).filter { it.field.name == name }
        assertEquals(2, entries.size)
        assertFalse(entries.single { it.field.declaringClass == child }.isShadowed)
        assertTrue(entries.single { it.field.declaringClass == parent }.isShadowed)
    }
}
