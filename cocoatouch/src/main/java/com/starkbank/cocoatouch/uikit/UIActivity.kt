package com.starkbank.cocoatouch.uikit
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.starkbank.cocoatouch.compatibility.BackPressedParser
import com.starkbank.cocoatouch.foundation.NSMutableArray
import java.lang.reflect.Method


open class UIActivity: AppCompatActivity() {

    protected val navigationController = UINavigationController()
    private var isInBackground = false
    private var lastCall: (() -> Unit)? = null

    open fun initialViewControllerId(): Int {
        return 0
    }

    open fun containerId(): Int {
        return 0
    }

    open fun storyboardId(): Int {
        return 0
    }

    protected open fun prepareLaunch() {

    }

    protected open fun initApplication() {

    }

    fun setRootViewController(viewController: UIViewController) {
        val activity = UIApplication.sharedApplication().activity()
        if(!isInBackground) {
            for (fragment in activity.supportFragmentManager.fragments) {
                activity.supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
            val viewControllers = NSMutableArray<UIViewController>()
            viewControllers.addObject(viewController)
            this.navigationController.setViewControllers(viewControllers, animated = false)
            return
        }
        lastCall = { setRootViewController(viewController) }
    }

    //
    // Android Methods
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareLaunch()
        setContentView(storyboardId())
        initApplication()
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { dispatchBackPressed() }
        })
    }

    private fun dispatchBackPressed() {
        var lastController: UIViewController? = null
        if (this.navigationController.modalViewControllers.count() > 0)
            lastController = this.navigationController.modalViewControllers.lastObject()
        else if (this.navigationController.pushViewControllers.count() > 0)
            lastController = this.navigationController.pushViewControllers.lastObject()
        val method: Method? = BackPressedParser.getMethod(lastController)
        if (method == null)
            UIApplication.sharedApplication().activity().finish()

        try {
            method?.invoke(lastController, UIButton())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRestart() {
        super.onRestart()
        val application = UIApplication.sharedApplication()
        application.delegate?.applicationDidBecomeActive(application)
        if(application.activity?.supportFragmentManager?.isDestroyed == true) {
            application.activity = this
        }
        finishPausedTransitions()
        isInBackground = false
    }

    override fun onResume() {
        super.onResume()
        val application = UIApplication.sharedApplication()
        application.delegate?.applicationWillEnterForeground(application)
        isInBackground = false
        lastCall?.let {
            it.invoke()
            lastCall = null
        }
    }

    override fun onPause() {
        super.onPause()
        isInBackground = true
        //
        // Applications goes to background. This code prevent ViewControllers
        // to receive calls from viewDidAppear or viewDidDisappead...
        //
        UIApplication.sharedApplication().isNotificationsEnabled = false
    }

    override fun onStop() {
        super.onStop()

        //
        // Applications goes to background. This code prevent ViewControllers
        // to receive calls from viewDidAppear or viewDidDisappead...
        //
        UIApplication.sharedApplication().isNotificationsEnabled = false
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        isInBackground = true
        super.onSaveInstanceState(savedInstanceState)
        startPausedTransitions()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        finishPausedTransitions()
    }

    private fun startPausedTransitions() {
        this.navigationController.pausedViewControllers = NSMutableArray<UIViewController>()
    }

    private fun finishPausedTransitions() {
        if (this.navigationController.pausedViewControllers == null) {
            return
        }
        for (controller in this.navigationController.pausedViewControllers!!) {
            this.navigationController.presentPushViewController(controller, false)
        }
        this.navigationController.pausedViewControllers!!.removeAllObjects()
        this.navigationController.pausedViewControllers = null
    }

}