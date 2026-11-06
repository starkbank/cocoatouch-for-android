package com.starkbank.cocoatouch.uikit

import android.graphics.Color
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class UIRefreshControl: UIControl() {

    private fun getRefreshControl(): SwipeRefreshLayout {
        return (widget as SwipeRefreshLayout)
    }

    fun endRefreshing() {
        getRefreshControl().isRefreshing = false
    }

    fun beginRefreshing() {
        getRefreshControl().isRefreshing = true
    }

    fun isRefreshing(): Boolean {
        return getRefreshControl().isRefreshing
    }

    fun addTarget(action: ()-> Unit) {
        getRefreshControl().setOnRefreshListener {
            action.invoke()
        }
    }

    var tintColor: UIColor? = null
        set(value) {
            field = value
            value?.colorInt?.let { getRefreshControl().setColorSchemeColors(it) }
        }

    override var backgroundColor: UIColor?= null
        set(value) {
            field = value
            value?.colorInt?.let { getRefreshControl().setProgressBackgroundColorSchemeColor(it) }
        }

}