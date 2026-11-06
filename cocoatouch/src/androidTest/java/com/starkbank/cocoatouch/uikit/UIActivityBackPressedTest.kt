package com.starkbank.cocoatouch.uikit

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.onMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class ThrowingBackFixture : UIViewController() {

    @IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {
        throw IllegalStateException("back boom")
    }
}

@RunWith(AndroidJUnit4::class)
class UIActivityBackPressedTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var activity: TestStoryboard

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
        scenario.onActivity { activity = it }
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun backHandlerExceptionReachesTheCaller() {
        onMain {
            navigation(activity).pushViewControllers.addObject(ThrowingBackFixture())
            val error = assertThrows(IllegalStateException::class.java) {
                activity.onBackPressedDispatcher.onBackPressed()
            }
            assertEquals("back boom", error.message)
        }
    }

    @Test
    fun controllerWithoutBackHandlerFinishesTheActivity() {
        onMain {
            navigation(activity).pushViewControllers.addObject(UIViewController())
            activity.onBackPressedDispatcher.onBackPressed()
            assertTrue(activity.isFinishing)
        }
    }
}
