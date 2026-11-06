package com.starkbank.cocoatouch.foundation

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun removingTheSameObserverTwiceIsHarmless() {
        val token = NotificationCenter.addObserver({ xCalls++ }, "a")
        NotificationCenter.removeObserver(token)
        NotificationCenter.removeObserver(token)
        NotificationCenter.postNotification("a")
        waitForMain()
        assertEquals(0, xCalls)
    }

    @Test
    fun postFromABackgroundThreadRunsTheSelectorOnMain() {
        var onMain = false
        val token = NotificationCenter.addObserver({ onMain = Looper.myLooper() == Looper.getMainLooper() }, "a")
        val poster = Thread { NotificationCenter.postNotification("a") }
        poster.start()
        poster.join()
        waitForMain()
        assertTrue(onMain)
        NotificationCenter.removeObserver(token)
    }

    private fun waitForMain() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
}
