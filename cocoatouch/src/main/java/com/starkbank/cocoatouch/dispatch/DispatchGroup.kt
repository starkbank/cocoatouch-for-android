package com.starkbank.cocoatouch.dispatch

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

private val globalExecutor = Executors.newCachedThreadPool()
private val mainHandler = Handler(Looper.getMainLooper())

class DispatchGroup {

    private val lock = Any()
    private var pending = 0
    private val blocks = mutableListOf<Pair<DispatchQueue, () -> Unit>>()

    fun enter() {
        synchronized(lock) {
            pending += 1
        }
    }

    fun leave() {
        val ready = synchronized(lock) {
            check(pending > 0) { "DispatchGroup.leave() called more times than enter()" }
            pending -= 1
            if (pending > 0) return@synchronized emptyList()
            val drained = blocks.toList()
            blocks.clear()
            drained
        }
        ready.forEach { (queue, work) -> deliver(queue, work) }
    }

    fun notify(dispatchQueue: DispatchQueue, work: () -> Unit) {
        val balanced = synchronized(lock) {
            if (pending > 0) blocks.add(dispatchQueue to work)
            pending == 0
        }
        if (!balanced) return
        deliver(dispatchQueue, work)
    }

    private fun deliver(queue: DispatchQueue, work: () -> Unit) {
        if (queue == DispatchQueue.main) {
            mainHandler.post(work)
            return
        }
        globalExecutor.execute(work)
    }
}

enum class DispatchQueue {
    main,
    global
}
