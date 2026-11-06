package com.starkbank.cocoatouch.uikit
import androidx.fragment.app.FragmentManager
import com.starkbank.cocoatouch.R
import com.starkbank.cocoatouch.foundation.NSArray
import com.starkbank.cocoatouch.foundation.NSMutableArray
import com.starkbank.cocoatouch.uikit.UIApplication.sharedApplication
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi
import java.lang.ref.WeakReference


open class UINavigationController: UIViewController() {

    val viewControllers: ArrayList<UIViewController>
        get() {
            val array = arrayListOf<UIViewController>()
            array.addAll(pushViewControllers)
            array.addAll(modalViewControllers)
            return array
        }

    @InternalCocoaTouchApi
    var pushViewControllers = NSMutableArray<UIViewController>()
    internal var modalViewControllers = NSMutableArray<UIViewController>()
    @InternalCocoaTouchApi
    var pausedViewControllers: NSMutableArray<UIViewController>? = null
    internal var pausedTransactions: MutableList<() -> Unit>? = null
    internal var owner: WeakReference<UIActivity>? = null

    open fun pushViewController(viewController: UIViewController, animated: Boolean) {
        if (ownerActivity() == null) return
        val presentedViewController: UIViewController? = lastViewController(this.pushViewControllers)
        viewController.navigationController = this
        viewController.setPresentingViewController(null)
        presentedViewController?.setPresentedViewController(viewController)
        this.pushViewControllers.addObject(viewController)
        presentPushViewController(viewController, animated)
    }

    open fun presentViewController(viewController: UIViewController, animated: Boolean) {
        if (ownerActivity() == null) return
        val presentedViewController: UIViewController? = lastViewController(this.pushViewControllers)
        viewController.navigationController = this
        viewController.setPresentingViewController(null)
        presentedViewController?.setPresentedViewController(viewController)
        this.pushViewControllers.addObject(viewController)
        presentModalViewController(viewController, animated)
    }

    fun setViewControllers(viewControllers: NSMutableArray<UIViewController>, animated: Boolean) {
        if (ownerActivity() == null) return
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
        if (ownerActivity() == null) return
        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        sharedApplication().isNotificationsEnabled = true
        if (this.pushViewControllers.count() == 1) {
            return
        }
        this.pushViewControllers.removeObjectAtIndex(this.pushViewControllers.count() - 1)

        transactOnOwner { it.popBackStack() }

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
        if (ownerActivity() == null) return
        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        sharedApplication().isNotificationsEnabled = true
        val lastIndex = lastIndexOfArray(this.pushViewControllers)
        for (i in lastIndex downTo 1) {
            this.pushViewControllers.removeObjectAtIndex(i)
        }
        transactOnOwner { it.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE) }

        this.pushViewControllers.lastObject()?.viewWillAppear(animated)
    }



    internal fun presentModalViewController(viewController: UIViewController, animated: Boolean) {
        val activity = ownerActivity() ?: return
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

        this.pausedViewControllers?.addObject(viewController)
        val addsToBackStack = pushViewControllers.count() > 1
        // The drain replays without animation, as the old drain forced animated = false
        val animates = animated && pausedQueue() == null
        transact { commitModal(viewController, animates, addsToBackStack) }
    }

    open fun dismissModalViewControllerAnimated(animated: Boolean) {
        if (ownerActivity() == null) return
        //
        // Applications is in foreground because this action was trigged by user.
        // This code make ViewControllers to receive calls from viewDidAppear or viewDidDisappead...
        //
        sharedApplication().isNotificationsEnabled = true
        if (this.pushViewControllers.count() == 1) {
            return
        }
        this.pushViewControllers.removeObjectAtIndex(this.pushViewControllers.count() - 1)

        transactOnOwner { it.popBackStack() }

        this.pushViewControllers.lastObject()?.viewWillAppear(animated)
    }

    internal fun presentPushViewController(viewController: UIViewController, animated: Boolean) {
        this.pausedViewControllers?.addObject(viewController)
        val addsToBackStack = pushViewControllers.count() > 1
        val animates = animated && pausedQueue() == null
        transact { commitPush(viewController, animates, addsToBackStack) }
    }

    private fun commitModal(viewController: UIViewController, animated: Boolean, addsToBackStack: Boolean) {
        val activity = ownerActivity() ?: return
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

        if (addsToBackStack) {
            fragmentTransaction.addToBackStack(viewController.javaClass.name)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun commitPush(viewController: UIViewController, animated: Boolean, addsToBackStack: Boolean) {
        val activity = ownerActivity() ?: return
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

        if (addsToBackStack) {
            fragmentTransaction.addToBackStack(viewController.javaClass.name)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    // A destroyed owner counts as no activity, so a controller kept past its activity cannot act on another one
    internal fun ownerActivity(): UIActivity? {
        val owning = owner ?: return UIApplication.activity
        return owning.get()?.takeUnless { it.isDestroyed }
    }

    internal override fun transactOnOwner(block: (FragmentManager) -> Unit) {
        transact {
            val manager = ownerActivity()?.supportFragmentManager ?: return@transact
            block(manager)
        }
    }

    // FragmentActivity.onStop stops the fragment manager before onSaveInstanceState creates the queue (API 28+),
    // so a saved fragment manager opens the queue itself
    private fun pausedQueue(): MutableList<() -> Unit>? {
        val queue = this.pausedTransactions
        if (queue != null) return queue
        if (ownerActivity()?.supportFragmentManager?.isStateSaved != true) return null
        return mutableListOf<() -> Unit>().also { this.pausedTransactions = it }
    }

    // When the state is saved a fragment transaction throws, so it waits, in order, for the restart
    private fun transact(block: () -> Unit) {
        val queue = pausedQueue()
        if (queue == null) {
            block()
            return
        }
        queue.add(block)
    }

    @InternalCocoaTouchApi
    fun lastViewController(array: NSArray<UIViewController>): UIViewController? {
        val lastIndex = lastIndexOfArray(array)
        return if (lastIndex >= 0) array.objectAtIndex(lastIndex) else null
    }

    private fun lastIndexOfArray(array: NSArray<*>): Int {
        return array.count() - 1
    }
}