package com.starkbank.android
import android.content.res.Resources
import com.starkbank.cocoatouch.uikit.UIApplicationDelegate
import com.starkbank.cocoatouch.uikit.UIStoryboard
import com.starkbank.cocoatouch.uikit.UITableViewCell
import com.starkbank.cocoatouch.uikit.UIViewController


class MainStoryboard: UIStoryboard() {

    override fun applicationDelegate(): UIApplicationDelegate {
        return AppDelegate()
    }

    override fun initialViewControllerId(): Int {
        return R.layout.mainviewcontroller
    }

    override fun storyboardId(): Int {
        return R.layout.mainstoryboard
    }

    override fun containerId(): Int {
        return R.id.container
    }

    override fun viewControllerForIdentifier(identifier: Int): UIViewController {
        return when (identifier) {
            R.layout.mainviewcontroller -> MainViewController()
            R.layout.textfieldviewcontroller -> TextFieldViewController()
            R.layout.tableviewcontroller -> TableViewController()
            else -> throw Resources.NotFoundException()
        }
    }

    override fun viewCellForIdentifier(identifier: Int): UITableViewCell {
        return when (identifier) {
            R.layout.tableviewcell -> TableViewCell()
            else -> throw Resources.NotFoundException()
        }
    }
}