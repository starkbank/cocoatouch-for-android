package com.starkbank.cocoatouch.foundation

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArrayList

class NotificationObserver internal constructor(internal val selector: () -> Unit, val name: String)

class NotificationCenter {

    companion object {

        // Observers may be added, removed or posted to from any thread (copy-on-write list: a post iterates a snapshot);
        // selectors always run on the main thread
        private val observers = CopyOnWriteArrayList<NotificationObserver>()
        private val mainHandler = Handler(Looper.getMainLooper())

        fun addObserver(selector: () -> Unit, name: String): NotificationObserver {
            val observer = NotificationObserver(selector, name)
            observers.add(observer)
            return observer
        }

        fun removeObserver(observer: NotificationObserver) {
            observers.remove(observer)
        }

        @JvmSynthetic
        @Deprecated(
            "Remove the observer you added with the token addObserver returned",
            level = DeprecationLevel.ERROR
        )
        fun removeObserver() {
            observers.clear()
        }

        fun postNotification(name: String, params: HashMap<String, String> = hashMapOf()) {
            mainHandler.post {
                for (observer in observers) {
                    if (observer.name == name) observer.selector.invoke()
                }
            }
        }
    }
}

enum class NSNotificationName {
    willEnterForegroundNotification,
    didBecomeActiveNotification
}