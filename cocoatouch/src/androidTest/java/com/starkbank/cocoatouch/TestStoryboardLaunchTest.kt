package com.starkbank.cocoatouch

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.uikit.UIApplication
import com.starkbank.cocoatouch.uikit.UIFragment
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TestStoryboardLaunchTest {

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
    fun launchesInitialViewControllerOnce() {
        scenario.onActivity { activity ->
            assertSame(activity, UIApplication.activity())
            val root = (activity.supportFragmentManager.fragments.first() as UIFragment).viewController as RecordingViewController
            assertEquals(1, root.events.count { it == "viewDidLoad" })
        }
    }

    @Test
    fun rootInterceptsBackWhenItIsTheOnlyController() {
        scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
            val root = (activity.supportFragmentManager.fragments.first() as UIFragment).viewController as RecordingViewController
            assertEquals(1, root.events.count { it == "back" })
            assertFalse(activity.isFinishing)
        }
    }
}
