package com.starkbank.cocoatouch.uikit
import androidx.fragment.app.FragmentManager
import com.starkbank.cocoatouch.R
import com.starkbank.cocoatouch.foundation.NSArray
import com.starkbank.cocoatouch.foundation.NSMutableArray
import com.starkbank.cocoatouch.uikit.UIApplication.sharedApplication


open class UINavigationController: UIViewController() {

    val viewControllers: ArrayList<UIViewController>
        get() {
            val array = arrayListOf<UIViewController>()
            array.addAll(pushViewControllers)
            array.addAll(modalViewControllers)
            return array
        }

    internal var pushViewControllers = NSMutableArray<UIViewController>()
    internal var modalViewControllers = NSMutableArray<UIViewController>()
    internal var pausedViewControllers: NSMutableArray<UIViewController>? = null

    open fun pushViewController(viewController: UIViewController, animated: Boolean) {
        val presentedViewController: UIViewController? = lastViewController(this.pushViewControllers)
        viewController.navigationController = this
        viewController.setPresentingViewController(null)
        presentedViewController?.setPresentedViewController(viewController)
        this.pushViewControllers.addObject(viewController)
        presentPushViewController(viewController, animated)
    }

    open fun presentViewController(viewController: UIViewController, animated: Boolean) {
        val presentedViewController: UIViewController? = lastViewController(this.pushViewControllers)
        viewController.navigationController = this
        viewController.setPresentingViewController(null)
        presentedViewController?.setPresentedViewController(viewController)
        this.pushViewControllers.addObject(viewController)
        presentModalViewController(viewController, animated)
    }

    fun setViewControllers(viewControllers: NSMutableArray<UIViewController>, animated: Boolean) {
        this.pushViewControllers = NSMutableArray()
        val presentedViewController: UIViewController? = lastViewController(this.pushViewControllers)
        for (viewController in viewControllers) {
            viewController.navigationController = this
            viewController.setPresentingViewController(null)
            presentedViewController?.setPresentedViewController(viewController)
            this.pushViewControllers.addObject(viewController)
        }

        viewControllers.lastObject()?.let {
            presentPushViewController(it, animated)
        }
    }

    open fun popViewController(animated: Boolean) {
        val tabs = lastViewController(this.pushViewControllers) as? UITabBarController
        if (tabs?.popInTab(animated, null) == true) return
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

        val activity = sharedApplication().activity()
        activity.supportFragmentManager.popBackStack()

        val lastViewController = this.pushViewControllers.lastObject()
        if(lastViewController is UITabBarController) {
            lastViewController.selectedViewController?.viewDidLoad()
            lastViewController.selectedViewController?.viewWillAppear(animated)
            return
        }
        lastViewController?.viewDidLoad()
        lastViewController?.viewWillAppear(animated)
    }

    open fun popToRootViewController(animated: Boolean) {
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
        val activity = sharedApplication().activity()
        activity.supportFragmentManager.popBackStack(
            viewController.javaClass.name,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        this.pushViewControllers.lastObject()?.viewWillAppear(animated)
    }



    internal fun presentModalViewController(viewController: UIViewController, animated: Boolean) {
        val activity = sharedApplication().activity()
        //
        // Don' let push items to fragment when activity is finishing or destroyed
        //
        if (activity.isFinishing)
            return

        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        sharedApplication().isNotificationsEnabled = true

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
        if(animated)
            fragmentTransaction.setCustomAnimations(
                R.anim.cocoatouch_modal_up,
                0,
                0,
                R.anim.cocoatouch_modal_down
            )

        if(fragment.isAdded){
            fragmentTransaction.show(fragment)
        }else {
            fragmentTransaction.add(storyboard.containerId(), fragment)
        }

        if (pushViewControllers.count() > 1) {
            fragmentTransaction.addToBackStack(viewController.javaClass.name)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    open fun dismissModalViewControllerAnimated(animated: Boolean) {
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

        val activity = sharedApplication().activity()
        activity.supportFragmentManager.popBackStack()

        this.pushViewControllers.lastObject()?.viewWillAppear(animated)
    }

    internal fun presentPushViewController(viewController: UIViewController, animated: Boolean) {
        val activity = sharedApplication().activity()
        //
        // Don' let push items to fragment when activity is finishing or destroyed
        //
        if (activity.isFinishing)
            return

        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        sharedApplication().isNotificationsEnabled = true

        //
        // When Activity is paused, you can not perform this
        //
        if (this.pausedViewControllers != null) {
            this.pausedViewControllers?.addObject(viewController)
            return
        }

        val storyboard = viewController.storyboard
        val fragment = viewController.view?.fragment()

        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()
        if (animated) {
            fragmentTransaction.setCustomAnimations(
                R.anim.cocoatouch_slide_in,
                R.anim.cocoatouch_fade_out,
                R.anim.cocoatouch_fade_in,
                R.anim.cocoatouch_slide_out
            )
        }

        if (storyboard != null && fragment != null) {
            if(fragment.isAdded){
                fragmentTransaction.show(fragment)
                //TODO: remove
            } else {
                fragmentTransaction.add(storyboard.containerId(), fragment)
            }
        }

       if (pushViewControllers.count() > 1) {
            fragmentTransaction.addToBackStack(viewController.javaClass.name)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    internal fun lastViewController(array: NSArray<UIViewController>): UIViewController? {
        val lastIndex = lastIndexOfArray(array)
        return if (lastIndex >= 0) array.objectAtIndex(lastIndex) else null
    }

    private fun lastIndexOfArray(array: NSArray<*>): Int {
        return array.count() - 1
    }
}