package com.starkbank.cocoatouch.uikit

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.storyboard
import com.starkbank.cocoatouch.test.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
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

    @Test
    fun popWhileStateIsSavedIsAppliedOnRestart() {
        val activity = launchedActivity()
        pushAndSettle(activity, 1)
        scenario.moveToState(Lifecycle.State.CREATED)
        onMain { navigation(activity).popViewController(false) }
        assertEquals(1, navigation(activity).viewControllers.size)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            it.supportFragmentManager.executePendingTransactions()
            assertEquals(0, it.supportFragmentManager.backStackEntryCount)
        }
    }

    @Test
    fun popToRootWhileStateIsSavedIsAppliedOnRestart() {
        val activity = launchedActivity()
        pushAndSettle(activity, 2)
        scenario.moveToState(Lifecycle.State.CREATED)
        onMain { navigation(activity).popToRootViewController(false) }
        assertEquals(1, navigation(activity).viewControllers.size)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            it.supportFragmentManager.executePendingTransactions()
            assertEquals(0, it.supportFragmentManager.backStackEntryCount)
        }
    }

    @Test
    fun pushThenPopWhilePausedNetsToTheOriginalStack() {
        val activity = launchedActivity()
        scenario.moveToState(Lifecycle.State.CREATED)
        onMain {
            navigation(activity).pushViewController(pushedController(activity), false)
            navigation(activity).popViewController(false)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            it.supportFragmentManager.executePendingTransactions()
            assertEquals(1, navigation(it).viewControllers.size)
            assertEquals(0, it.supportFragmentManager.backStackEntryCount)
        }
    }

    @Test
    fun popThenPushWhilePausedReplacesTheTopInOrder() {
        val activity = launchedActivity()
        pushAndSettle(activity, 1)
        val replacement = pushedController(activity)
        scenario.moveToState(Lifecycle.State.CREATED)
        onMain {
            navigation(activity).popViewController(false)
            navigation(activity).pushViewController(replacement, false)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            it.supportFragmentManager.executePendingTransactions()
            assertEquals(2, navigation(it).viewControllers.size)
            assertSame(replacement, navigation(it).viewControllers.last())
            assertEquals(1, it.supportFragmentManager.backStackEntryCount)
        }
    }

    private fun launchedActivity(): TestStoryboard {
        lateinit var activity: TestStoryboard
        scenario.onActivity { activity = it }
        return activity
    }

    private fun pushedController(activity: TestStoryboard): UIViewController {
        return storyboard(activity).instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed)
    }

    private fun pushAndSettle(activity: TestStoryboard, count: Int) {
        onMain {
            repeat(count) { navigation(activity).pushViewController(pushedController(activity), false) }
            activity.supportFragmentManager.executePendingTransactions()
        }
        assertEquals(count, activity.supportFragmentManager.backStackEntryCount)
    }
}
