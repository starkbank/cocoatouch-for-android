package com.starkbank.cocoatouch.uikit

import com.starkbank.cocoatouch.R


open class UITabBarController : UIViewController(), UITabBarDelegate {

    var itemsLimit = 5
    var selectedViewController: UIViewController? = null

    var tabBar: UITabBar? = null
        set(value) {
            field = value
            tabBar?.delegate = this
        }

    var delegate: UITabBarDelegate? = null

    var viewControllers: ArrayList<UIViewController> = arrayListOf()
        set(value) {
            field = value
            if(value.isNotEmpty()) {
                setTabBarItems()
            }
        }

    open var selectedIndex: Int = 0
        set(value) {
            field = value
            if(viewControllers.isNotEmpty()) {
                presentViewController()
            }
        }

    private fun setTabBarItems() {
        if(viewControllers.size > itemsLimit) { return }
        val items = viewControllers.map { it.tabBarItem } as ArrayList<UITabBarItem>
        tabBar?.setItems(items)
        selectedIndex = 0
    }


    private fun presentViewController() {
        val controller = viewControllers[selectedIndex]
        selectedViewController = controller
        controller.navigationController = navigationController
        controller.tabBarController = this
        controller.view?.fragment()?.let { fragment ->
            controller.setParentViewController(this)
            transactOnOwner { it.beginTransaction().replace(tabContainerId(), fragment).commit() }
        }
    }

    open fun presentViewController(viewController: UIViewController, animated: Boolean = false) {
        viewController.navigationController = navigationController
        viewController.tabBarController = this
        viewController.setParentViewController(this.selectedViewController)
        viewController.view?.fragment()?.let { fragment ->
            viewController.setParentViewController(this)
            transactOnOwner {
                UIApplication.sharedApplication().isNotificationsEnabled = true
                val fragmentTransaction = it.beginTransaction()
                if (animated) {
                    fragmentTransaction.setCustomAnimations(
                        R.anim.cocoatouch_slide_in,
                        R.anim.cocoatouch_fade_out,
                        R.anim.cocoatouch_fade_in,
                        R.anim.cocoatouch_slide_out
                    )
                }
                fragmentTransaction.add(tabContainerId(), fragment)
                fragmentTransaction.addToBackStack(viewController.javaClass.name)
                fragmentTransaction.commit()
            }
        }
    }

    open fun tabContainerId(): Int {
        return R.id.cocoatouch_tab_container
    }

    open fun popInTab(animated: Boolean = true, controller: UIViewController? = null): Boolean {
        return false
    }

    override fun didSelect(item: Int) {
        selectedIndex = item
    }


}