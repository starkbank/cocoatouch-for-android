package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.compability.BackPressedParser
import com.starkbank.cocoatouch.foundation.NSMutableArray
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Method
import android.os.Bundle


open class UIActivity: AppCompatActivity() {

    protected val navigationController = UINavigationController()

    open fun initialViewControllerId(): Int {
        return 0
    }

    open fun containerId(): Int {
        return 0
    }

    open fun storyboardId(): Int {
        return 0
    }

    internal open fun initApplication() {

    }

    //
    // Android Methods
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(storyboardId())
        initApplication()
    }

    override fun onBackPressed() {
        var lastController: UIViewController? = null
        if (this.navigationController.modalViewControllers.count() > 0)
            lastController = this.navigationController.modalViewControllers.lastObject()
        else if (this.navigationController.pushViewControllers.count() > 1)
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
        finishPausedTransitions()
    }

    override fun onPause() {
        super.onPause()

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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        val application = UIApplication.sharedApplication()
//        val delegate = application.delegate
//        delegate?.applicationDidBecomeActive(application)
//    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
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