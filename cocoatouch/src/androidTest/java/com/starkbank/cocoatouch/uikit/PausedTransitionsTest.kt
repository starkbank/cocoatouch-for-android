package com.starkbank.cocoatouch.uikit

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.storyboard
import com.starkbank.cocoatouch.test.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PausedTransitionsTest {

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
    fun pushWhileStoppedIsPresentedOnceAfterRestart() {
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {
            navigation(it).pushViewController(
                storyboard(it).instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed),
                false
            )
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            activity.supportFragmentManager.executePendingTransactions()
            assertEquals(2, navigation(activity).viewControllers.size)
            assertEquals(1, activity.supportFragmentManager.backStackEntryCount)
        }
    }

    @Test
    fun twoPushesWhileStoppedPresentBoth() {
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity { activity ->
            repeat(2) {
                navigation(activity).pushViewController(
                    storyboard(activity).instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed),
                    false
                )
            }
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            activity.supportFragmentManager.executePendingTransactions()
            assertEquals(3, navigation(activity).viewControllers.size)
            assertEquals(2, activity.supportFragmentManager.backStackEntryCount)
        }
    }
}
