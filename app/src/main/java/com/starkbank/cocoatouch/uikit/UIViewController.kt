package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.foundation.NSMutableArray
import android.view.View


open class UIViewController: UIResponder() {

    var view: UIView? = null
    var storyboard: UIStoryboard? = null
    var navigationController: UINavigationController? = null
    private var childViewControllers = NSMutableArray<UIViewController>()
    private var parentViewController: UIViewController? = null
    private var presentingViewController: UIViewController? = null
    private var presentedViewController: UIViewController? = null

    //
    // Public Instance Methods
    //
    override fun viewWithTag(tag: Int): View {
        return this.view!!.viewWithTag(tag)
    }

    open fun presentViewController(viewController: UIViewController, animated: Boolean) {
        this.navigationController?.presentModalViewController(viewController, animated)
    }

    open fun dismissViewControllerAnimated(animated: Boolean) {
        this.navigationController?.dismissModalViewControllerAnimated(animated)
    }

    open fun addChild(childController: UIViewController) {
        childController.setParentViewController(this)
        this.childViewControllers.addObject(childController)
    }

    open fun removeFromParent() {
        this.parentViewController!!.childViewControllers.removeObject(this)
    }

    //
    // Public Get Methods
    //
    open fun parentViewController(): UIViewController? {
        return this.parentViewController
    }

    open fun presentingViewController(): UIViewController? {
        return this.presentingViewController
    }

    open fun presentedViewController(): UIViewController? {
        return this.presentedViewController
    }

    //
    // Life Cycle Notifications
    //
    open fun viewDidLoad() {
        for (viewController in childViewControllers) {
            viewController.viewDidLoad()
        }
    }

    fun viewWillAppear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewWillAppear(animated)
        }
    }

    fun viewDidAppear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewDidAppear(animated)
        }
    }

    fun viewWillDisappear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewWillDisappear(animated)
        }
    }

    fun viewDidDisappear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewWillDisappear(animated)
        }
    }

    //
    // Internal Instance Methods
    //
    internal open fun setIdentifier(identifier: Int) {
        this.view = UIView().initWith(this, identifier)
    }

    internal open fun setParentViewController(parentViewController: UIViewController?) {
        this.parentViewController = parentViewController
    }

    internal open fun setPresentingViewController(presentingViewController: UIViewController?) {
        this.presentingViewController = presentingViewController
    }

    internal open fun setPresentedViewController(presentedViewController: UIViewController?) {
        this.presentedViewController = presentedViewController
    }
}