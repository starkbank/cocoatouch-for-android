package com.starkbank.cocoatouch.uikit
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.starkbank.cocoatouch.compatibility.BackPressedParser
import com.starkbank.cocoatouch.foundation.NSMutableArray
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException


open class UIActivity: AppCompatActivity() {

    protected val navigationController = UINavigationController()
    private var isInBackground = false
    private var lastCall: (() -> Unit)? = null
    internal var pickers: ActivityPickers? = null

    init {
        navigationController.owner = WeakReference(this)
    }

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
        if (isDestroyed) return
        if(!isInBackground) {
            for (fragment in this.supportFragmentManager.fragments) {
                this.supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
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
        prepareLaunch()
        super.onCreate(savedInstanceState)
        UITraitCollection.current = UITraitCollection.from(resources.configuration.uiMode)
        setContentView(storyboardId())
        UIApplication.attach(this)
        initApplication()
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { dispatchBackPressed() }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        UITraitCollection.current = UITraitCollection.from(resources.configuration.uiMode)
    }

    private fun dispatchBackPressed() {
        val lastController = navigationController.modalViewControllers.lastObject()
            ?: navigationController.pushViewControllers.lastObject()
        val method = BackPressedParser.getMethod(lastController)
        if (method == null) {
            finish()
            return
        }
        try {
            method.invoke(lastController, UIButton())
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }

    override fun onStart() {
        super.onStart()
        if (UIApplication.activity !== this) UIApplication.attach(this)
        finishPausedTransitions()
    }

    override fun onDestroy() {
        super.onDestroy()
        UIApplication.detach(this)
    }

    override fun onRestart() {
        super.onRestart()
        val application = UIApplication.sharedApplication()
        application.delegate?.applicationDidBecomeActive(application)
        isInBackground = false
    }

    override fun onResume() {
        super.onResume()
        if (UIApplication.activity !== this) UIApplication.attach(this)
        val application = UIApplication.sharedApplication()
        application.delegate?.applicationWillEnterForeground(application)
        isInBackground = false
        lastCall?.let {
            it.invoke()
            lastCall = null
        }
    }

    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        super.onTopResumedActivityChanged(isTopResumedActivity)
        if (!isTopResumedActivity) return
        if (UIApplication.activity !== this) UIApplication.attach(this)
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
        this.navigationController.pausedTransactions = mutableListOf()
    }

    private fun finishPausedTransitions() {
        val transactions = this.navigationController.pausedTransactions ?: return
        // Detach first: a transaction that re-queues while the list is non-null would grow it during the iteration
        this.navigationController.pausedTransactions = null
        this.navigationController.pausedViewControllers = null
        for (transaction in transactions) {
            transaction()
        }
    }

}
