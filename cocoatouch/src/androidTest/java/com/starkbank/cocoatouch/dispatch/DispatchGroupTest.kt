package com.starkbank.cocoatouch.dispatch

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        repeat(2) { group.notify(DispatchQueue.global) { runs.incrementAndGet(); latch.countDown() } }
        group.leave()
        assertTrue(latch.await(1, SECONDS))
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
        assertThrows(IllegalStateException::class.java) { DispatchGroup().leave() }
    }
}
