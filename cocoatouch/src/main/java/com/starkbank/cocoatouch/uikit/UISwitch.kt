package com.starkbank.cocoatouch.uikit

import android.R
import android.content.res.ColorStateList
import androidx.appcompat.widget.SwitchCompat


class UISwitch : UIControl() {

    var isOn: Boolean
        get() = getSwitch().isChecked
        set(value) {
            getSwitch().isChecked = value
        }

    var onTintColor: UIColor? = null
        set(value) {
            val appearance = UIApplication.appearance
            val trait = UITraitCollection.current
            val thumbColors = ColorStateList(
                arrayOf(intArrayOf(R.attr.state_checked), intArrayOf()), value?.let {
                    intArrayOf(
                        it.colorInt,
                        appearance.borderColor(trait).colorInt
                    )
                }
            )
            getSwitch().trackTintList = thumbColors
            getSwitch().thumbTintList = ColorStateList.valueOf(appearance.onActionColor(trait).colorInt)
            field = value
        }

    fun setOn(value: Boolean, animated: Boolean) {
        getSwitch().isChecked = value
    }

    fun addTarget(action: (UISwitch) -> Unit, forControlEvent: Event) {
        when (forControlEvent) {
            Event.valueChange -> getSwitch().setOnCheckedChangeListener { _, _ ->
                action(this)
            }
            else -> {}
        }
    }

    private fun getSwitch(): SwitchCompat {
        return (widget as SwitchCompat)
    }

}