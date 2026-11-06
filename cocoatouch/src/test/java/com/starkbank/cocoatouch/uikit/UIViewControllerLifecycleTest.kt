package com.starkbank.cocoatouch.uikit

import org.junit.Assert.assertEquals
import org.junit.Test


class UIViewControllerLifecycleTest {

    private class TraceViewController(
        private val name: String,
        private val events: MutableList<String>
    ) : UIViewController() {

        override fun viewWillDisappear(animated: Boolean) {
            events.add("$name.viewWillDisappear")
            super.viewWillDisappear(animated)
        }

        override fun viewDidDisappear(animated: Boolean) {
            events.add("$name.viewDidDisappear")
            super.viewDidDisappear(animated)
        }
    }

    @Test
    fun disappearanceReachesEachChildOncePerCallback() {
        val events = mutableListOf<String>()
        val parent = TraceViewController("parent", events)
        parent.addChild(TraceViewController("child", events))
        parent.viewWillDisappear(false)
        parent.viewDidDisappear(false)
        assertEquals(
            listOf(
                "parent.viewWillDisappear",
                "child.viewWillDisappear",
                "parent.viewDidDisappear",
                "child.viewDidDisappear"
            ),
            events
        )
    }
}
