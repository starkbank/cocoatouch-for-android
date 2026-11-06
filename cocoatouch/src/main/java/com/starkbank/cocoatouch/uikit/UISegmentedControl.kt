package com.starkbank.cocoatouch.uikit

import android.widget.Button
import androidx.core.view.children
import com.google.android.material.button.MaterialButtonToggleGroup

open class UISegmentedControl: UIControl() {

    private fun getSegmentedControl(): MaterialButtonToggleGroup? {
        return (widget as? MaterialButtonToggleGroup)
    }

    var numberOfSegments: Int = 0
        get() {
            return getSegmentedControl()?.childCount ?: 0
        }

    var selectedSegmentIndex: Int
        get() {
            return getSegmentedControl()?.children?.indexOfFirst { it.id == getSegmentedControl()?.checkedButtonId } ?: -1
        }
        set(value) {
            if (value > numberOfSegments) { return }
            val viewId = getSegmentedControl()?.getChildAt(value)?.id
            if (viewId != null) {
                getSegmentedControl()?.check(viewId)
            }
        }

    fun setTitle(title:String?, forSegmentAt: Int) {
        val segment = getSegmentedControl()?.getChildAt(forSegmentAt) as? Button
        segment?.text = title
    }

    fun addTarget(action: (UISegmentedControl)-> Unit, forControlEvent: Event) {
        when(forControlEvent) {
            Event.valueChange -> getSegmentedControl()?.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if(isChecked) {
                    return@addOnButtonCheckedListener action.invoke(this)
                }
            }
            Event.touchUpInside -> getSegmentedControl()?.children?.forEach {
                it.setOnClickListener {
                    val viewId = getSegmentedControl()?.getChildAt(selectedSegmentIndex)?.id
                    if (viewId != null) {
                        getSegmentedControl()?.uncheck(viewId)
                    }
                    return@setOnClickListener action.invoke(this)
                }
            }
        }
    }

}