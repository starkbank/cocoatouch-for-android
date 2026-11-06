package com.starkbank.cocoatouch.annotation

import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.Assert.assertEquals
import org.junit.Test

open class ParentActionFixture : UIViewController() {

    @IBAction(1) open fun tap(sender: UIButton) {}

    @IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {}
}

open class ChildActionFixture : ParentActionFixture() {

    @IBAction(2) fun other(sender: UIButton) {}
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
}
