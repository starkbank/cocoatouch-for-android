package com.starkbank.cocoatouch.dispatch

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class DispatchGroupTest {

    private val group = DispatchGroup()
    private val latch = CountDownLatch(1)
    private val runs = AtomicInteger()

    @Volatile
    private var onMain = false

    private fun isMain(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    @Test
    fun notifyOnEmptyGroupRunsOnMain() {
        group.notify(DispatchQueue.main) { onMain = isMain(); latch.countDown() }
        assertTrue(latch.await(1, SECONDS))
        assertTrue(onMain)
    }

    @Test
    fun notifyAfterCompletionRuns() {
        group.enter()
        group.leave()
        group.notify(DispatchQueue.main) { latch.countDown() }
        assertTrue(latch.await(1, SECONDS))
    }

    @Test
    fun everyPendingNotifyRunsOnce() {
        val latch = CountDownLatch(2)
        group.enter()
        repeat(2) { group.notify(DispatchQueue.main) { runs.incrementAndGet(); latch.countDown() } }
        group.leave()
        assertTrue(latch.await(1, SECONDS))
        awaitMainQueueDrained()
        assertEquals(2, runs.get())
    }

    @Test
    fun globalQueueRunsOffMain() {
        group.enter()
        group.notify(DispatchQueue.global) { onMain = isMain(); latch.countDown() }
        group.leave()
        assertTrue(latch.await(1, SECONDS))
        assertFalse(onMain)
    }

    @Test
    fun leaveFromBackgroundThreadDeliversOnMain() {
        group.enter()
        group.notify(DispatchQueue.main) { onMain = isMain(); latch.countDown() }
        Thread { group.leave() }.start()
        assertTrue(latch.await(1, SECONDS))
        assertTrue(onMain)
    }

    @Test
    fun unbalancedLeaveThrows() {
        val error = assertThrows(IllegalStateException::class.java) { DispatchGroup().leave() }
        assertEquals("DispatchGroup.leave() called more times than enter()", error.message)
    }

    @Test
    fun blockRunsOnceAcrossCycles() {
        val first = CountDownLatch(1)
        group.enter()
        group.notify(DispatchQueue.main) { runs.incrementAndGet(); first.countDown() }
        group.leave()
        assertTrue(first.await(1, SECONDS))
        val second = CountDownLatch(1)
        group.enter()
        group.notify(DispatchQueue.main) { second.countDown() }
        group.leave()
        assertTrue(second.await(1, SECONDS))
        awaitMainQueueDrained()
        assertEquals(1, runs.get())
    }

    @Test
    fun leaveAfterCompletionThrows() {
        group.enter()
        group.leave()
        val error = assertThrows(IllegalStateException::class.java) { group.leave() }
        assertEquals("DispatchGroup.leave() called more times than enter()", error.message)
    }

    @Test
    fun notifyRacingLeaveDeliversEveryBlock() {
        val latch = CountDownLatch(50)
        repeat(50) {
            group.enter()
            Thread { group.leave() }.start()
            group.notify(DispatchQueue.global) { latch.countDown() }
        }
        assertTrue(latch.await(5, SECONDS))
    }

    // runOnMainSync returns after every message posted earlier to the main looper ran, so a duplicate run is counted
    private fun awaitMainQueueDrained() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {}
    }
}
