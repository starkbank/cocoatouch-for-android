package com.starkbank.cocoatouch.uikit


open class UIStoryboard: UIActivity() {

    //
    // Public instance methods
    //
    fun instantiateViewControllerWithIdentifier(identifier: Int): UIViewController {
        val viewController: UIViewController = this.viewControllerForIdentifier(identifier)
        viewController.setIdentifier(identifier)
        viewController.storyboard = this
        return viewController
    }

    //
    // Init
    //
    override fun initApplication() {
        val application = UIApplication.sharedApplication()
        application.activity = this
        application.storyboard = this
        application.delegate = this.applicationDelegate()
        application.delegate?.application(application, null)
        application.delegate?.applicationDidBecomeActive(application)

        val viewController = instantiateViewControllerWithIdentifier(this.initialViewControllerId())
        this.navigationController.pushViewController(viewController, false)
    }

    //
    // Methods that needs to be override
    //
    open fun viewControllerForIdentifier(identifier: Int): UIViewController {
        return UIViewController()
    }

    open fun viewCellForIdentifier(identifier: Int): UITableViewCell {
        return UITableViewCell()
    }

    open fun applicationDelegate(): UIApplicationDelegate? {
        return null
    }
}