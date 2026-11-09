package com.starkbank.cocoatouch.uikit

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.starkbank.cocoatouch.PlainActivity
import com.starkbank.cocoatouch.PushedViewController
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.TranslucentActivity
import com.starkbank.cocoatouch.assertCollected
import com.starkbank.cocoatouch.foundation.NSMutableArray
import com.starkbank.cocoatouch.foundation.NotificationCenter
import com.starkbank.cocoatouch.foundation.NotificationObserver
import com.starkbank.cocoatouch.foundation.UserDefaults
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.storyboard
import com.starkbank.cocoatouch.test.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.ref.WeakReference

@RunWith(AndroidJUnit4::class)
class UIApplicationLifetimeTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val application = instrumentation.targetContext.applicationContext as Application
    private lateinit var scenario: ActivityScenario<TestStoryboard>

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
    }

    @After
    fun close() {
        liveStoryboards().forEach { finishAndAwaitDestroyed(it) }
        if (scenario.state == Lifecycle.State.DESTROYED) return
        scenario.close()
    }

    @Test
    fun recreateAttachesNewActivity() {
        var first: UIActivity? = null
        scenario.onActivity { first = it }
        scenario.recreate()
        scenario.onActivity { assertNotSame(first, it); assertSame(it, UIApplication.activity()) }
    }

    @Test
    fun closeDetachesActivity() {
        scenario.close()
        assertNull(UIApplication.activity)
        assertNull(UIApplication.storyboard)
    }

    @Test
    fun recreateCreatesFreshImagePicker() {
        val first = UIImagePickerController.shared
        scenario.recreate()
        assertNotSame(first, UIImagePickerController.shared)
    }

    @Test
    fun recreateCreatesFreshDocumentPicker() {
        val first = UIDocumentPickerViewController.shared
        scenario.recreate()
        assertNotSame(first, UIDocumentPickerViewController.shared)
    }

    @Test
    fun userDefaultsWorksAfterClose() {
        scenario.close()
        UserDefaults.standard.set("value", "afterClose")
        assertEquals("value", UserDefaults.standard.string("afterClose"))
    }

    @Test
    fun pushAfterCloseIsIgnored() {
        lateinit var stack: UINavigationController
        scenario.onActivity { stack = navigation(it) }
        scenario.close()
        instrumentation.runOnMainSync { stack.pushViewController(PushedViewController(), false) }
        assertEquals(1, stack.viewControllers.size)
    }

    @Test
    fun setRootViewControllerAfterCloseIsIgnored() {
        lateinit var stack: UINavigationController
        val activity = scenarioActivity()
        scenario.onActivity { stack = navigation(it) }
        scenario.close()
        instrumentation.runOnMainSync { activity.setRootViewController(PushedViewController()) }
        assertEquals(1, stack.viewControllers.size)
    }

    @Test
    fun navigationAfterCloseLeavesTheStackUntouched() {
        lateinit var stack: UINavigationController
        scenario.onActivity { stack = navigation(it) }
        scenario.close()
        instrumentation.runOnMainSync {
            stack.presentViewController(PushedViewController(), false)
            stack.setViewControllers(NSMutableArray<UIViewController>().apply { addObject(PushedViewController()) }, false)
            stack.popViewController(false)
            stack.popToRootViewController(false)
            stack.dismissModalViewControllerAnimated(false)
        }
        assertEquals(1, stack.viewControllers.size)
    }

    @Test
    fun readersWorkAfterClose() {
        scenario.close()
        var notified = false
        lateinit var observer: NotificationObserver
        instrumentation.runOnMainSync {
            observer = NotificationCenter.addObserver({ notified = true }, "afterClose")
            NotificationCenter.postNotification("afterClose")
            UIPasteboard.general.string = "afterClose"
        }
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync { NotificationCenter.removeObserver(observer) }
        assertEquals("afterClose", UIPasteboard.general.string)
        assertEquals(true, notified)
    }

    @Test
    fun stopStartKeepsTheSamePicker() {
        val picker = UIImagePickerController.shared
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        assertSame(picker, UIImagePickerController.shared)
    }

    @Test
    fun finishingTopInstanceReattachesTheOneBelow() {
        val first = scenarioActivity()
        val top = launchSecondInstance(first)
        finishAndAwaitDestroyed(top)
        instrumentation.runOnMainSync { assertSame(first, UIApplication.activity()) }
    }

    @Test
    fun destroyingTheActivityBelowKeepsTheTopAttached() {
        val first = scenarioActivity()
        val top = launchSecondInstance(first)
        var picker: UIImagePickerController? = null
        instrumentation.runOnMainSync { picker = UIImagePickerController.shared }
        finishAndAwaitDestroyed(first)
        instrumentation.runOnMainSync {
            assertSame(top, UIApplication.activity())
            assertSame(picker, UIImagePickerController.shared)
        }
        finishAndAwaitDestroyed(top)
    }

    @Test
    fun plainActivityAttachKeepsThePreviousStoryboard() {
        val first = scenarioActivity()
        instrumentation.runOnMainSync { first.startActivity(Intent(first, PlainActivity::class.java)) }
        var plain: PlainActivity? = null
        awaitCondition("the PlainActivity to resume") {
            plain = resumedActivities().filterIsInstance<PlainActivity>().firstOrNull()
            plain != null
        }
        instrumentation.runOnMainSync {
            assertSame(plain, UIApplication.activity)
            assertSame(storyboard(first), UIApplication.storyboard)
        }
        finishAndAwaitDestroyed(plain!!)
        awaitCondition("first resumed") { resumedStoryboards() == listOf(first) }
    }

    @Test
    fun translucentActivityOnTopKeepsTheOneBelowAttachedAfterItFinishes() {
        val first = scenarioActivity()
        instrumentation.runOnMainSync { first.startActivity(Intent(first, TranslucentActivity::class.java)) }
        var translucent: TranslucentActivity? = null
        awaitCondition("the TranslucentActivity to be attached") {
            translucent = resumedActivities().filterIsInstance<TranslucentActivity>().firstOrNull()
            translucent != null && UIApplication.activity === translucent
        }
        onMain { assertEquals(Lifecycle.State.STARTED, first.lifecycle.currentState) }
        finishAndAwaitDestroyed(translucent!!)
        awaitCondition("first resumed") { resumedStoryboards() == listOf(first) }
        onMain {
            assertSame(first, UIApplication.activity)
            navigation(first).pushViewController(pushedController(first), false)
        }
        awaitCondition("the push to reach the back stack") { first.supportFragmentManager.backStackEntryCount == 1 }
    }

    @Test
    fun becomingTheTopResumedActivityAttachesIt() {
        // The callback exists from API 29; below it nothing calls the override and its super does not exist
        assumeTrue(Build.VERSION.SDK_INT >= 29)
        val first = scenarioActivity()
        var firstPicker: UIImagePickerController? = null
        onMain { firstPicker = UIImagePickerController.shared }
        launchSecondInstance(first)
        onMain {
            assertNotSame(first, UIApplication.activity)
            first.onTopResumedActivityChanged(true)
            assertSame(first, UIApplication.activity)
            assertSame(firstPicker, UIImagePickerController.shared)
        }
    }

    @Test
    fun plainActivityKeepsTheFinishedStoryboardBelowIt() {
        val first = scenarioActivity()
        onMain { first.startActivity(Intent(first, PlainActivity::class.java)) }
        lateinit var plain: PlainActivity
        awaitCondition("the PlainActivity to resume") {
            val resumed = resumedActivities().filterIsInstance<PlainActivity>().firstOrNull() ?: return@awaitCondition false
            plain = resumed
            true
        }
        finishAndAwaitDestroyed(first)
        onMain { assertSame(first, UIApplication.storyboard) }
        finishAndAwaitDestroyed(plain)
    }

    @Test
    fun pushQueuedWhileStoppedIsPresentedWhenTheActivityRestarts() {
        val first = scenarioActivity()
        val top = launchSecondInstance(first)
        onMain { navigation(first).pushViewController(pushedController(first), false) }
        finishAndAwaitDestroyed(top)
        awaitCondition("first resumed") { resumedStoryboards() == listOf(first) }
        assertEquals(2, navigation(first).viewControllers.size)
        awaitCondition("the queued push to reach the back stack") { first.supportFragmentManager.backStackEntryCount == 1 }
    }

    @Test
    fun popOnAStoppedActivityDoesNotTouchTheAttachedOne() {
        val first = scenarioActivity()
        onMain { navigation(first).pushViewController(pushedController(first), false) }
        awaitCondition("first to hold one pushed controller") { first.supportFragmentManager.backStackEntryCount == 1 }
        val top = launchSecondInstance(first)
        onMain { navigation(top).pushViewController(pushedController(top), false) }
        awaitCondition("top to hold one pushed controller") { top.supportFragmentManager.backStackEntryCount == 1 }
        onMain { navigation(first).popViewController(false) }
        instrumentation.waitForIdleSync()
        onMain { assertEquals(1, top.supportFragmentManager.backStackEntryCount) }
        finishAndAwaitDestroyed(top)
        awaitCondition("first resumed") { resumedStoryboards() == listOf(first) }
        awaitCondition("the queued pop to reach the back stack") { first.supportFragmentManager.backStackEntryCount == 0 }
    }

    @Test
    fun pushWhileTheTopIsFinishingIsQueuedForTheActivityBelow() {
        val first = scenarioActivity()
        val top = launchSecondInstance(first)
        awaitCondition("first has a paused queue") { navigation(first).pausedTransactions != null }
        instrumentation.runOnMainSync {
            top.finish()
            navigation(first).pushViewController(pushedController(first), false)
        }
        awaitCondition("first resumed") { resumedStoryboards() == listOf(first) }
        awaitCondition("queued push reached the back stack") { first.supportFragmentManager.backStackEntryCount == 1 }
        assertEquals(2, navigation(first).viewControllers.size)
    }

    @Test
    fun prepareLaunchRunsBeforeSuperOnCreate() {
        val first = scenarioActivity()
        val events = TestStoryboard.launchEvents
        val callbacks = object : Application.ActivityLifecycleCallbacks by NoOpLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { events.add("created") }
        }
        application.registerActivityLifecycleCallbacks(callbacks)
        try {
            events.clear()
            finishAndAwaitDestroyed(launchSecondInstance(first))
        } finally {
            application.unregisterActivityLifecycleCallbacks(callbacks)
        }
        assertEquals(listOf("prepareLaunch", "created"), events.take(2))
    }

    @Test
    fun destroyedActivityIsCollected() {
        assertCollected(onWorkerThread { destroyBothInstances() })
    }

    @Test
    fun finishingTheTopRestoresTheBelowActivityPickers() {
        val first = scenarioActivity()
        var firstPicker: UIImagePickerController? = null
        onMain { firstPicker = UIImagePickerController.shared }
        val top = launchSecondInstance(first)
        var topPicker: UIImagePickerController? = null
        onMain { topPicker = UIImagePickerController.shared }
        finishAndAwaitDestroyed(top)
        awaitCondition("first resumed") { resumedStoryboards() == listOf(first) }
        onMain {
            assertSame(firstPicker, UIImagePickerController.shared)
            val error = assertThrows(IllegalStateException::class.java) { topPicker!!.present() }
            assertEquals(
                "UIImagePickerController.shared belongs to a destroyed activity; read UIImagePickerController.shared again",
                error.message
            )
        }
    }

    @Test
    fun finishingTheTopRestoresTheBelowActivityDocumentPicker() {
        val first = scenarioActivity()
        var firstPicker: UIDocumentPickerViewController? = null
        onMain { firstPicker = UIDocumentPickerViewController.shared }
        val top = launchSecondInstance(first)
        var topPicker: UIDocumentPickerViewController? = null
        onMain { topPicker = UIDocumentPickerViewController.shared }
        finishAndAwaitDestroyed(top)
        awaitCondition("first resumed") { resumedStoryboards() == listOf(first) }
        onMain {
            assertSame(firstPicker, UIDocumentPickerViewController.shared)
            val error = assertThrows(IllegalStateException::class.java) { topPicker!!.present() }
            assertEquals(
                "UIDocumentPickerViewController.shared belongs to a destroyed activity; " +
                    "read UIDocumentPickerViewController.shared again",
                error.message
            )
        }
    }

    // The last activity destroyed is the one the static shared pickers point to
    private fun destroyBothInstances(): WeakReference<TestStoryboard> {
        val first = scenarioActivity()
        val top = launchSecondInstance(first)
        finishAndAwaitDestroyed(first)
        finishAndAwaitDestroyed(top)
        return WeakReference(top)
    }

    // A worker's stack dies with it, so no stale slot of the test thread can keep an activity alive
    private fun <T> onWorkerThread(block: () -> T): T {
        var result: T? = null
        var failure: Throwable? = null
        val worker = Thread {
            try {
                result = block()
            } catch (error: Throwable) {
                failure = error
            }
        }
        worker.start()
        worker.join()
        failure?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun pushedController(activity: TestStoryboard): UIViewController {
        return storyboard(activity).instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed)
    }

    private fun scenarioActivity(): TestStoryboard {
        lateinit var activity: TestStoryboard
        scenario.onActivity { activity = it }
        return activity
    }

    private fun launchSecondInstance(from: Activity): TestStoryboard {
        instrumentation.runOnMainSync { from.startActivity(Intent(from, TestStoryboard::class.java)) }
        instrumentation.waitForIdleSync()
        var second: TestStoryboard? = null
        awaitCondition("a second TestStoryboard to resume") {
            second = resumedStoryboards().firstOrNull { it !== from }
            second != null
        }
        awaitCondition("the first TestStoryboard to stop") {
            ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(from) == Stage.STOPPED
        }
        return second!!
    }

    // Before O the system destroys a finished background activity only when the resumed one reports idle, which under
    // the load of the whole suite on API 25 took more than 15 s once
    private fun finishAndAwaitDestroyed(activity: Activity) {
        instrumentation.runOnMainSync { activity.finish() }
        instrumentation.waitForIdleSync()
        awaitCondition("the activity to be destroyed", timeoutMillis = 30000) {
            ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity) == Stage.DESTROYED
        }
    }

    private fun resumedActivities(): List<Activity> {
        return ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).toList()
    }

    private fun resumedStoryboards(): List<TestStoryboard> {
        return ActivityLifecycleMonitorRegistry.getInstance()
            .getActivitiesInStage(Stage.RESUMED)
            .filterIsInstance<TestStoryboard>()
    }

    private fun liveStoryboards(): List<TestStoryboard> {
        var alive: List<TestStoryboard> = emptyList()
        instrumentation.runOnMainSync {
            alive = Stage.values()
                .filter { it != Stage.DESTROYED }
                .flatMap { ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(it) }
                .filterIsInstance<TestStoryboard>()
        }
        return alive
    }

    private fun awaitCondition(description: String, timeoutMillis: Long = 5000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            var isMet = false
            instrumentation.runOnMainSync { isMet = condition() }
            if (isMet) return
            Thread.sleep(50)
        }
        fail("Timed out after ${timeoutMillis / 1000}s waiting for $description")
    }
}
