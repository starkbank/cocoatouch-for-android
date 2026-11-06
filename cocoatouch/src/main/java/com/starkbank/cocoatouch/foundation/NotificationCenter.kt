package com.starkbank.cocoatouch.foundation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.starkbank.cocoatouch.uikit.UIApplication
import java.util.concurrent.CopyOnWriteArrayList

class NotificationObserver internal constructor(internal val receiver: BroadcastReceiver, val name: String)

class NotificationCenter {

    companion object {

        // Observers may be added or removed from any thread (copy-on-write list, synchronized LocalBroadcastManager);
        // selectors always run on the main thread
        private val observers = CopyOnWriteArrayList<NotificationObserver>()

        fun addObserver(selector: () -> Unit, name: String): NotificationObserver {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    selector.invoke()
                }
            }
            LocalBroadcastManager.getInstance(UIApplication.applicationContext!!)
                .registerReceiver(receiver, IntentFilter(name))
            val observer = NotificationObserver(receiver, name)
            observers.add(observer)
            return observer
        }

        fun removeObserver(observer: NotificationObserver) {
            if (!observers.remove(observer)) return
            LocalBroadcastManager.getInstance(UIApplication.applicationContext!!).unregisterReceiver(observer.receiver)
        }

        @Deprecated(
            "Remove the observer you added with the token addObserver returned",
            level = DeprecationLevel.ERROR
        )
        fun removeObserver() {
            for (observer in observers) {
                removeObserver(observer)
            }
        }

        fun postNotification(name: String, params: HashMap<String, String> = hashMapOf()) {
            val intent = Intent(name)
            //TODO: CREATE MAP DO INTENT
            //        for ((key, value): Map.Entry<String, String> in params.entrySet()) {
            //            intent.putExtra(key, value)
            //        }
            LocalBroadcastManager.getInstance(UIApplication.applicationContext!!).sendBroadcast(intent)
        }
    }
}

enum class NSNotificationName {
    willEnterForegroundNotification,
    didBecomeActiveNotification
}