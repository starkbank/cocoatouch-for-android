package com.starkbank.cocoatouch.foundation

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationCenterTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private var xCalls = 0
    private var yCalls = 0

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun removingOneObserverKeepsTheOthers() {
        val xToken = NotificationCenter.addObserver({ xCalls++ }, "a")
        val yToken = NotificationCenter.addObserver({ yCalls++ }, "b")
        NotificationCenter.removeObserver(xToken)
        NotificationCenter.postNotification("a")
        NotificationCenter.postNotification("b")
        waitForMain()
        assertEquals(0, xCalls)
        assertEquals(1, yCalls)
        NotificationCenter.removeObserver(yToken)
    }

    @Suppress("DEPRECATION")
    @Test
    fun removeObserverWithoutArgumentRemovesEveryObserver() {
        NotificationCenter.addObserver({ xCalls++ }, "a")
        NotificationCenter.addObserver({ yCalls++ }, "b")
        NotificationCenter.removeObserver()
        NotificationCenter.postNotification("a")
        NotificationCenter.postNotification("b")
        waitForMain()
        assertEquals(0, xCalls + yCalls)
    }

    @Test
    fun removingTheSameObserverTwiceIsHarmless() {
        val token = NotificationCenter.addObserver({ xCalls++ }, "a")
        NotificationCenter.removeObserver(token)
        NotificationCenter.removeObserver(token)
        NotificationCenter.postNotification("a")
        waitForMain()
        assertEquals(0, xCalls)
    }

    private fun waitForMain() {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(300)
    }
}
