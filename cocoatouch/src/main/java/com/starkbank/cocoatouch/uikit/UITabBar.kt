package com.starkbank.cocoatouch.uikit

import android.R
import android.content.res.ColorStateList
import android.view.Menu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.starkbank.cocoatouch.foundation.weakReference


class UITabBar : UIView() {

    var items: ArrayList<UITabBarItem> = arrayListOf()

    var selectedItem: UITabBarItem? = null

    var delegate: UITabBarDelegate? by weakReference(null)

    private fun getTabBar(): BottomNavigationView {
        return (this.widget as BottomNavigationView)
    }

    fun setItems(items: ArrayList<UITabBarItem>, animated: Boolean = false) {
        this.items = items
        getTabBar().menu.clear()
        items.forEachIndexed { index, value ->
            getTabBar().menu.add(Menu.NONE, index, Menu.NONE, value.title)
                .setIcon(value.image.imageRes)
        }
        selectedItem = items.firstOrNull()

        getTabBar().setOnItemSelectedListener { item ->
            delegate?.didSelect(item.itemId)
            true
        }
    }

    fun setBackground(color: UIColor) {
        getTabBar().setBackgroundColor(color.colorInt)
    }

    fun setIconColor(activeColor: UIColor, inactiveColor: UIColor) {
        val states = arrayOf(intArrayOf(R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(
            activeColor.colorInt,
            inactiveColor.colorInt
        )
        val colorStateList = ColorStateList(states, colors)
        getTabBar().itemIconTintList = colorStateList
        getTabBar().itemTextColor = colorStateList
    }

}
