package com.starkbank.cocoatouch

import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.test.R
import com.starkbank.cocoatouch.uikit.UIActivity
import com.starkbank.cocoatouch.uikit.UIFragment
import com.starkbank.cocoatouch.uikit.UINavigationController
import com.starkbank.cocoatouch.uikit.UIStoryboard
import com.starkbank.cocoatouch.uikit.UIViewController


class TestStoryboard : UIStoryboard() {

    companion object {
        val launchEvents: MutableList<String> = mutableListOf()
    }

    override fun prepareLaunch() {
        launchEvents.add("prepareLaunch")
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
