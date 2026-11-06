package com.starkbank.cocoatouch.uikit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test


class UITabBarControllerPopInTabTest {

    private class PoppingTabBarController : UITabBarController() {

        val poppedControllers = mutableListOf<UIViewController?>()

        override fun popInTab(animated: Boolean, controller: UIViewController?): Boolean {
            poppedControllers.add(controller)
            return true
        }
    }

    @Test
    fun popViewControllerLetsTheTopTabBarControllerConsumeThePop() {
        val navigationController = UINavigationController()
        val tabBarController = PoppingTabBarController()
        navigationController.pushViewControllers.addObject(UIViewController())
        navigationController.pushViewControllers.addObject(tabBarController)
        navigationController.popViewController(false)
        assertEquals(listOf<UIViewController?>(null), tabBarController.poppedControllers)
        assertEquals(2, navigationController.viewControllers.size)
    }

    @Test
    fun dismissLetsTheOwningTabBarControllerConsumeTheDismiss() {
        val tabBarController = PoppingTabBarController()
        val controller = UIViewController()
        controller.tabBarController = tabBarController
        controller.dismissViewControllerAnimated(false)
        assertEquals(listOf<UIViewController?>(controller), tabBarController.poppedControllers)
    }

    @Test
    fun dismissOfATabBarControllerOwningItselfSkipsPopInTab() {
        val tabBarController = PoppingTabBarController()
        tabBarController.tabBarController = tabBarController
        tabBarController.dismissViewControllerAnimated(false)
        assertEquals(0, tabBarController.poppedControllers.size)
    }

    @Test
    fun plainTabBarControllerDoesNotConsumeThePop() {
        assertFalse(UITabBarController().popInTab(false, null))
    }
}
