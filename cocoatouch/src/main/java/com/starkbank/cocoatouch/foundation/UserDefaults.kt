package com.starkbank.cocoatouch.foundation

import android.content.Context
import android.content.SharedPreferences
import com.starkbank.cocoatouch.uikit.UIApplication

class UserDefaults {
    //
    // Private Methods
    //
    private fun context(): Context {
        return UIApplication.context()
    }

    private fun preferences(key: String): SharedPreferences {
        return context().getSharedPreferences("${context().packageName}$key", Context.MODE_PRIVATE)
    }

    private fun editor(key: String): SharedPreferences.Editor {
        return preferences(key).edit()
    }

    //
    // Public Setters
    //


    fun set(string: String?, key: String) {
        val editor = editor(key)
        editor.putString(key, string)
        editor.apply()
    }

    fun set(bool: Boolean?, key: String) {
        val editor = editor(key)
        editor.putBoolean(key, bool!!)
        editor.apply()
    }

    //
    // Public Delete
    //
    fun removeObject(key: String) {
        val editor = editor(key)
        editor.remove(key)
        editor.apply()
    }

    //
    // Public Getters
    //
    fun string(key: String): String? {
        return preferences(key).getString(key, null)
    }

    fun bool(key: String): Boolean {
        return preferences(key).getBoolean(key, false)
    }


    companion object {
        val standard: UserDefaults by lazy {
            UserDefaults()
        }
    }
}