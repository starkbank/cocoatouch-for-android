package com.starkbank.cocoatouch

import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.test.R
import com.starkbank.cocoatouch.uikit.UIActivity
import com.starkbank.cocoatouch.uikit.UIFragment
import com.starkbank.cocoatouch.uikit.UINavigationController
import com.starkbank.cocoatouch.uikit.UIStoryboard
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.Assert.fail
import java.lang.ref.WeakReference


class TestStoryboard : UIStoryboard() {

    companion object {
        val launchEvents: MutableList<String> = mutableListOf()
    }

    override fun prepareLaunch() {
        launchEvents.add("prepareLaunch")
    }

    // Runs right after super.onStop(), where the fragment manager is already stopped but onSaveInstanceState has not run
    var afterStop: (() -> Unit)? = null

    override fun onStop() {
        super.onStop()
        afterStop?.invoke()
    }

    // Lets a test observe a failure of the queued transactions replayed by onStart without crashing the process
    var capturesStartFailure = false
    var startFailure: Throwable? = null

    override fun onStart() {
        try {
            super.onStart()
        } catch (error: Throwable) {
            if (!capturesStartFailure) throw error
            startFailure = error
        }
    }

    fun navigation(): UINavigationController {
        return navigationController
    }

    override fun initialViewControllerId(): Int {
        return R.layout.cocoatouch_test_root
    }

    override fun storyboardId(): Int {
        return R.layout.cocoatouch_test_storyboard
    }

    override fun containerId(): Int {
        return R.id.cocoatouch_test_container
    }

    // Library R ids are not constants, so a when over them would not compile
    override fun viewControllerForIdentifier(identifier: Int): UIViewController {
        if (identifier == R.layout.cocoatouch_test_root) return RecordingViewController()
        if (identifier == R.layout.cocoatouch_test_pushed) return PushedViewController()
        if (identifier == R.layout.cocoatouch_test_tabs) return TestTabBarController()
        return super.viewControllerForIdentifier(identifier)
    }
}

fun navigation(activity: UIActivity): UINavigationController {
    return (activity as TestStoryboard).navigation()
}

fun storyboard(activity: UIActivity): UIStoryboard {
    return activity as UIStoryboard
}

fun rootController(activity: UIActivity): RecordingViewController {
    return (activity.supportFragmentManager.fragments.first() as UIFragment).viewController as RecordingViewController
}

// A reference is cleared on a GC cycle the VM schedules, so the test keeps asking for one, with allocation pressure,
// instead of sleeping a fixed time
fun assertCollected(reference: WeakReference<*>) {
    repeat(20) {
        allocatePressure()
        Runtime.getRuntime().gc()
        Runtime.getRuntime().runFinalization()
        Thread.sleep(50)
        if (reference.get() == null) return
    }
    fail("${reference.get()} still reachable after 20 GC rounds")
}

private fun allocatePressure() {
    val pressure = ArrayList<ByteArray>()
    repeat(3) { pressure.add(ByteArray(4 shl 20)) }
}

// An assertion that fails on the main thread would crash the process instead of failing the test
fun onMain(block: () -> Unit) {
    var failure: Throwable? = null
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        try {
            block()
        } catch (error: Throwable) {
            failure = error
        }
    }
    failure?.let { throw it }
}
