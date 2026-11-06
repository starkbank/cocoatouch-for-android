package com.starkbank.cocoatouch.annotation

import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.uikit.UIApplication
import com.starkbank.cocoatouch.uikit.UILabel
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIView
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class ViewActionFixture : UIViewController() {

    var sender: UIResponder? = null

    @IBAction(1) fun tap(sender: UIView) {
        this.sender = sender
    }
}

class BoomFixture : UIViewController() {

    @IBAction(1) fun boom(sender: UIView) {
        throw IllegalArgumentException("boom")
    }
}

class MissingOutletFixture : UIViewController() {

    @IBOutlet(99) lateinit var missing: UILabel
}

@RunWith(AndroidJUnit4::class)
class IBActionParserInstrumentedTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun viewActionReceivesSenderWithWidget() {
        onMain {
            val clickedView = View(UIApplication.context()).apply { id = 1 }
            val fixture = ViewActionFixture()
            fixture.view = UIView().apply {
                widget = FrameLayout(UIApplication.context()).apply { addView(clickedView) }
            }
            IBActionParser.parse(fixture)
            clickedView.performClick()
            assertSame(clickedView, (fixture.sender as UIView).widget)
        }
    }

    @Test
    fun handlerExceptionReachesTheCaller() {
        onMain {
            val clickedView = View(UIApplication.context()).apply { id = 1 }
            val fixture = BoomFixture()
            fixture.view = UIView().apply {
                widget = FrameLayout(UIApplication.context()).apply { addView(clickedView) }
            }
            IBActionParser.parse(fixture)
            val error = assertThrows(IllegalArgumentException::class.java) { clickedView.performClick() }
            assertEquals("boom", error.message)
        }
    }

    @Test
    fun missingOutletFailsWithFieldName() {
        onMain {
            val missingFixture = MissingOutletFixture()
            missingFixture.view = UIView().apply { widget = FrameLayout(UIApplication.context()) }
            val error = assertThrows(IllegalStateException::class.java) { IBOutletParser.parse(missingFixture) }
            assertTrue(error.message!!.contains("missing"))
        }
    }

    @Test
    fun outletBoundBeforeTheViewExistsSaysSo() {
        onMain {
            val error = assertThrows(IllegalStateException::class.java) { IBOutletParser.parse(MissingOutletFixture()) }
            assertTrue(error.message!!.contains("before"))
            assertTrue(error.message!!.contains("missing"))
        }
    }
}
