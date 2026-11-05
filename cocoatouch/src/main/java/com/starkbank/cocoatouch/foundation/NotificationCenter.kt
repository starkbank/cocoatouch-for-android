package com.starkbank.cocoatouch.foundation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.starkbank.cocoatouch.uikit.UIApplication

class NotificationCenter {

    companion object {

        private var broadcastReceiver: BroadcastReceiver? = null

        fun addObserver(selector: ()-> (Unit), name: String, ) {
            broadcastReceiver = object: BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    selector.invoke()
                }
            }
            broadcastReceiver?.let {
                LocalBroadcastManager.getInstance(UIApplication.context()).registerReceiver(it, IntentFilter(name))
            }
        }

        fun removeObserver() {
            broadcastReceiver?.let {
                LocalBroadcastManager.getInstance(UIApplication.context()).unregisterReceiver(
                    it
                )
            }
        }

        fun postNotification(name: String, params: HashMap<String, String> = hashMapOf()) {
            val intent = Intent(name)
            //TODO: CREATE MAP DO INTENT
            //        for ((key, value): Map.Entry<String, String> in params.entrySet()) {
            //            intent.putExtra(key, value)
            //        }
            LocalBroadcastManager.getInstance(UIApplication.context()).sendBroadcast(intent)
        }
    }
}

enum class NSNotificationName {
    willEnterForegroundNotification,
    didBecomeActiveNotification
}