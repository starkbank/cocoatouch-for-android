package com.starkbank.cocoatouch.annotation

import android.content.Context
import android.widget.FrameLayout
import com.starkbank.cocoatouch.nullOf
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UITextField
import com.starkbank.cocoatouch.uikit.UIView
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
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

open class SubmitBaseFixture : UIViewController() {

    @IBAction(7) fun submit(sender: UIView) {}
}

class SubmitChildFixture : SubmitBaseFixture() {

    @IBAction(7) fun submitAndTrack(sender: UIView) {}
}

class NoSenderFixture : UIViewController() {

    @IBAction(1) fun noSender() {}
}

abstract class AbstractSender : UIResponder()

class AbstractSenderFixture : UIViewController() {

    @IBAction(1) fun act(sender: AbstractSender) {}
}

class StringSenderFixture : UIViewController() {

    @IBAction(1) fun act(sender: String) {}
}

class AnySenderFixture : UIViewController() {

    @IBAction(1) fun act(sender: Any) {}
}

class TextFieldBackFixture : UIViewController() {

    @IBAction(DefaultActions.onBackPressed) fun back(sender: UITextField) {}
}

class ButtonBackFixture : UIViewController() {

    @IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {}
}

class NoSenderBackFixture : UIViewController() {

    @IBAction(DefaultActions.onBackPressed) fun back() {}
}

class MissingViewFixture : UIViewController() {

    @IBAction(999999) fun act(sender: UIView) {}
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
    fun oneViewIdKeepsTheSubclassHandlerOnly() {
        val names = IBActionParser.actionMethods(SubmitChildFixture::class.java)
            .filter { it.getAnnotation(IBAction::class.java)!!.value == 7 }
            .map { it.name }
        assertEquals(listOf("submitAndTrack"), names)
    }

    @Test
    fun actionWithoutSenderParameterFailsAtBind() {
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(NoSenderFixture()) }
        assertTrue(error.message!!.contains("exactly one sender parameter"))
    }

    @Test
    fun senderTypeAUIViewCannotSatisfyFailsAtBind() {
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(StringSenderFixture()) }
        assertTrue(error.message!!.contains("cannot receive a UIView"))
    }

    @Test
    fun backHandlerWithoutSenderFailsAtBind() {
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(NoSenderBackFixture()) }
        assertTrue(error.message!!.contains("exactly one sender parameter"))
    }

    @Test
    fun backHandlerSenderMustReceiveAUIButton() {
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(TextFieldBackFixture()) }
        assertTrue(error.message!!.contains("cannot receive a UIButton"))
    }

    @Test
    fun backHandlerReceivingAUIButtonBinds() {
        IBActionParser.parse(ButtonBackFixture())
    }

    @Test
    fun anySenderTypePassesTheSenderCheck() {
        val fixture = AnySenderFixture()
        fixture.view = UIView().apply { widget = FrameLayout(nullOf<Context>()) }
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(fixture) }
        assertTrue(error.message!!.contains("not found"))
    }

    @Test
    fun abstractSenderTypeFailsAtBind() {
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(AbstractSenderFixture()) }
        assertTrue(error.message!!.contains("no public no-arg constructor"))
    }

    @Test
    fun missingActionViewFailsWithTheControllerAndId() {
        val fixture = MissingViewFixture()
        fixture.view = UIView().apply { widget = FrameLayout(nullOf<Context>()) }
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(fixture) }
        assertTrue(error.message!!.contains("999999"))
        assertTrue(error.message!!.contains("MissingViewFixture"))
    }

    @Test
    fun actionBoundBeforeTheViewExistsSaysSo() {
        val error = assertThrows(IllegalStateException::class.java) { IBActionParser.parse(MissingViewFixture()) }
        assertTrue(error.message!!.contains("has a view"))
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
