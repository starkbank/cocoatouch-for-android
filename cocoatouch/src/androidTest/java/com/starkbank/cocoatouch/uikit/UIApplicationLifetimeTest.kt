package com.starkbank.cocoatouch.uikit

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.starkbank.cocoatouch.PushedViewController
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.foundation.NSMutableArray
import com.starkbank.cocoatouch.foundation.NotificationCenter
import com.starkbank.cocoatouch.foundation.UserDefaults
import com.starkbank.cocoatouch.navigation
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
        instrumentation.runOnMainSync {
            NotificationCenter.addObserver({ notified = true }, "afterClose")
            NotificationCenter.postNotification("afterClose")
            UIPasteboard.general.string = "afterClose"
        }
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync { NotificationCenter.removeObserver() }
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
        finishAndAwaitDestroyed(first)
        instrumentation.runOnMainSync { assertSame(top, UIApplication.activity()); assertNotNull(UIImagePickerController.shared) }
        finishAndAwaitDestroyed(top)
    }

    @Test
    fun prepareLaunchRunsBeforeSuperOnCreate() {
        val events = TestStoryboard.launchEvents
        val callbacks = object : Application.ActivityLifecycleCallbacks by NoOpLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { events.add("created") }
        }
        application.registerActivityLifecycleCallbacks(callbacks)
        events.clear()
        ActivityScenario.launch(TestStoryboard::class.java).close()
        application.unregisterActivityLifecycleCallbacks(callbacks)
        assertEquals(listOf("prepareLaunch", "created"), events.take(2))
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

    private fun finishAndAwaitDestroyed(activity: Activity) {
        instrumentation.runOnMainSync { activity.finish() }
        instrumentation.waitForIdleSync()
        awaitCondition("the activity to be destroyed") {
            ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity) == Stage.DESTROYED
        }
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

    private fun awaitCondition(description: String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline) {
            var isMet = false
            instrumentation.runOnMainSync { isMet = condition() }
            if (isMet) return
            Thread.sleep(50)
        }
        fail("Timed out after 5s waiting for $description")
    }
}
