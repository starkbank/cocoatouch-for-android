package com.starkbank.cocoatouch.annotation

import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIView
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

open class ParentActionFixture : UIViewController() {

    @IBAction(1) open fun tap(sender: UIButton) {}

    @IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {}
}

open class ChildActionFixture : ParentActionFixture() {

    @IBAction(2) fun other(sender: UIButton) {}

    @IBAction(3) fun tap(sender: UIView) {}
}

class GrandChildActionFixture : ChildActionFixture()

class OverridingActionFixture : ParentActionFixture() {

    @IBAction(1) override fun tap(sender: UIButton) {}
}

class IBActionParserTest {

    @Test
    fun actionMethodsIncludeInheritedActions() {
        assertEquals(
            setOf("tap", "back", "other"),
            IBActionParser.actionMethods(ChildActionFixture::class.java).map { it.name }.toSet()
        )
    }

    @Test
    fun overriddenActionIsListedOnce() {
        assertEquals(1, IBActionParser.actionMethods(OverridingActionFixture::class.java).count { it.name == "tap" })
    }

    @Test
    fun overloadWithOtherParameterTypesIsNotHidden() {
        assertEquals(2, IBActionParser.actionMethods(ChildActionFixture::class.java).count { it.name == "tap" })
    }

    @Test
    fun walkStopsBeforeUIResponder() {
        assertTrue(
            IBActionParser.actionMethods(ChildActionFixture::class.java)
                .all { it.declaringClass != UIResponder::class.java }
        )
    }

    @Test
    fun actionMethodsAreComputedOncePerClass() {
        assertSame(
            IBActionParser.actionMethods(ChildActionFixture::class.java),
            IBActionParser.actionMethods(ChildActionFixture::class.java)
        )
    }
}
