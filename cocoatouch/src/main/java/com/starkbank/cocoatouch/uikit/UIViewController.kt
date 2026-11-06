package com.starkbank.cocoatouch.uikit
import android.view.View
import androidx.core.view.WindowInsetsControllerCompat
import com.starkbank.cocoatouch.R
import com.starkbank.cocoatouch.foundation.NSMutableArray
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi


open class UIViewController: UIResponder() {

    var view: UIView? = null
    var storyboard: UIStoryboard? = null
    var title: String = javaClass.simpleName
    var navigationController: UINavigationController? = null
    var tabBarController: UITabBarController? = null
    var tabBarItem: UITabBarItem = UITabBarItem(title = title, UIImage(R.drawable.cocoatouch_blank_shape))

    private var childViewControllers = NSMutableArray<UIViewController>()
    private var parentViewController: UIViewController? = null
    private var presentingViewController: UIViewController? = null
    private var presentedViewController: UIViewController? = null

    //
    // Public Instance Methods
    //
    @InternalCocoaTouchApi
    override fun viewWithTag(tag: Int): View {
        return this.view!!.viewWithTag(tag)
    }

    open fun push(viewController: UIViewController, animated: Boolean){
        this.navigationController?.pushViewController(viewController, animated)
    }

    open fun present(viewController: UIViewController, animated: Boolean) {
        this.navigationController?.presentViewController(viewController, animated)
    }

    open fun dismissViewControllerAnimated(animated: Boolean) {
        if (tabBarController !== this && tabBarController?.popInTab(animated, this) == true) return
        this.navigationController?.dismissModalViewControllerAnimated(animated)
    }

    open fun addChild(childController: UIViewController) {
        childController.setParentViewController(this)
        this.childViewControllers.addObject(childController)
    }

    open fun removeFromParent() {
        this.parentViewController!!.childViewControllers.removeObject(this)
    }

    fun setStatusBarColor(color: UIColor) {
        val window = UIApplication.activity().window
        val wic = WindowInsetsControllerCompat(window, window.decorView)
        wic.isAppearanceLightStatusBars = color.isLight()
        window.statusBarColor = color.colorInt
        updateSystemBarBackground(color, true)
    }

    fun setNavigationBarColor(color: UIColor) {
        val window = UIApplication.activity().window
        val wic = WindowInsetsControllerCompat(window, window.decorView)
        wic.isAppearanceLightNavigationBars = color.isLight()
        window.navigationBarColor = color.colorInt
        updateSystemBarBackground(color, false)
    }

    @InternalCocoaTouchApi
    fun updateSystemBarBackground(color: UIColor, statusBar: Boolean,
                                          window: android.view.Window = UIApplication.activity().window) {
        // Android 15+ makes both bars transparent. Keep the existing color API by painting
        // only their insets; the screen's content and its inset handling stay unchanged.
        if (android.os.Build.VERSION.SDK_INT < 35) return
        val decor = window.decorView as? android.widget.FrameLayout ?: return
        val key = if (statusBar) "stark-status-bar-background" else "stark-navigation-bar-background"
        val background = decor.findViewWithTag<View>(key) ?: View(decor.context).apply {
            tag = key
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            val gravity = if (statusBar) android.view.Gravity.TOP else android.view.Gravity.BOTTOM
            decor.addView(this, android.widget.FrameLayout.LayoutParams(-1, 0, gravity))
            fun updateHeight(insets: android.view.WindowInsets) {
                val bars = insets.getInsets(if (statusBar) android.view.WindowInsets.Type.statusBars()
                    else android.view.WindowInsets.Type.navigationBars())
                val height = if (statusBar) bars.top else bars.bottom
                if (layoutParams.height != height) layoutParams = layoutParams.apply { this.height = height }
            }
            setOnApplyWindowInsetsListener { _, insets -> updateHeight(insets); insets }
            decor.rootWindowInsets?.let(::updateHeight)
            requestApplyInsets()
        }
        background.setBackgroundColor(color.colorInt)
    }

    fun removeFromSuperview() {
        this.view?.fragment()?.let { fragment ->
            UIApplication.activity?.supportFragmentManager?.beginTransaction()?.remove(fragment)
                ?.commit()
        }
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

    open fun viewWillAppear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewWillAppear(animated)
        }
    }

    open fun viewDidAppear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewDidAppear(animated)
        }
    }

    open fun viewWillDisappear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewWillDisappear(animated)
        }
    }

    open fun viewDidDisappear(animated: Boolean) {
        for (viewController in childViewControllers) {
            viewController.viewDidDisappear(animated)
        }
    }

    //
    // Internal Instance Methods
    //
    internal open fun setIdentifier(identifier: Int) {
        this.view = UIView().initWith(this, identifier)
    }

    @InternalCocoaTouchApi
    open fun setParentViewController(parentViewController: UIViewController?) {
        this.parentViewController = parentViewController
    }

    internal open fun setPresentingViewController(presentingViewController: UIViewController?) {
        this.presentingViewController = presentingViewController
    }

    internal open fun setPresentedViewController(presentedViewController: UIViewController?) {
        this.presentedViewController = presentedViewController
    }
}
