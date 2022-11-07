package com.starkbank.cocoatouch.uikit
import androidx.fragment.app.FragmentManager
import com.starkbank.cocoatouch.foundation.NSArray
import com.starkbank.cocoatouch.foundation.NSMutableArray
import com.starkbank.cocoatouch.uikit.UIApplication.sharedApplication


class UINavigationController: UIViewController() {

    private var pushViewControllers = NSMutableArray<UIViewController>()
    internal var modalViewControllers = NSMutableArray<UIViewController>()
    internal var pausedViewControllers: NSMutableArray<UIViewController>? = null

    fun pushViewController(viewController: UIViewController, animated: Boolean) {
        val presentedViewController: UIViewController? = lastViewController(this.pushViewControllers)
        viewController.navigationController = this
        viewController.setPresentingViewController(null)
//        viewController.view.fragment().transition = if (animated) PushTransition() else NoneTransition()
        presentedViewController?.setPresentedViewController(viewController)
        this.pushViewControllers.addObject(viewController)
        presentPushViewController(viewController, animated)
    }

    fun popViewController(animated: Boolean) {
        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        sharedApplication().isNotificationsEnabled = true
        if (this.pushViewControllers.count() == 1) {
            return
        }
        val controllerToRemove = lastViewController(this.pushViewControllers)
        this.pushViewControllers.removeObject(controllerToRemove!!)

        val activity = UIApplication.sharedApplication().activity()
        activity.supportFragmentManager.popBackStack()
    }

    fun popToRootViewController(animated: Boolean) {
        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        sharedApplication().isNotificationsEnabled = true
        val lastIndex = lastIndexOfArray(this.pushViewControllers)
        for (i in lastIndex downTo 1) {
            val controllerToRemove = this.pushViewControllers.objectAtIndex(i)
            this.pushViewControllers.removeObject(controllerToRemove)
        }
        val viewController = this.pushViewControllers.objectAtIndex(0)
        val activity = UIApplication.sharedApplication().activity()
        activity.supportFragmentManager.popBackStack(
            viewController.javaClass.name,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    internal fun presentModalViewController(viewController: UIViewController, animated: Boolean) {

    }

    internal fun dismissModalViewControllerAnimated(animated: Boolean) {

    }

    internal fun presentPushViewController(viewController: UIViewController, animated: Boolean) {
        val activity = UIApplication.sharedApplication().activity()
        //
        // Don' let push items to fragment when activity is finishing or destroyed
        //
        if (activity.isFinishing)
            return

        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        UIApplication.sharedApplication().isNotificationsEnabled = true

        //
        // When Activity is paused, you can not perform this
        //
        if (this.pausedViewControllers != null) {
            this.pausedViewControllers?.addObject(viewController)
            return
        }

        val storyboard = viewController.storyboard!!
        val fragment = viewController.view!!.fragment()!!

        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()
//        fragment.transition = if (animated) fragment.transition else NoneTransition()
        fragmentTransaction.add(storyboard.containerId(), fragment)

        if (pushViewControllers.count() > 1) {
            fragmentTransaction.addToBackStack(viewController.javaClass.name)
        }

        fragmentTransaction.commit()
    }

    private fun lastViewController(array: NSArray<UIViewController>): UIViewController? {
        val lastIndex = lastIndexOfArray(array)
        return if (lastIndex >= 0) array.objectAtIndex(lastIndex) else null
    }

    private fun lastIndexOfArray(array: NSArray<*>): Int {
        return array.count() - 1
    }
}