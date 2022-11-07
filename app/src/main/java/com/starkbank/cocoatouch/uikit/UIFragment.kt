package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.animation.NoneTransition
import android.animation.Animator
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.starkbank.cocoatouch.annotation.IBActionParser
import com.starkbank.cocoatouch.annotation.IBOutletParser


class UIFragment : Fragment() {

    var identifier = 0
    var viewController: UIViewController? = null
    var transition = NoneTransition()
    var animated = false
    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.identifier == 0) {
            return View(UIApplication.sharedApplication().context())
        }
        this.container = inflater.inflate(this.identifier, container, false)
        IBOutletParser.parse(this.viewController!!)
        IBActionParser.parse(this.viewController!!)
        this.viewController?.viewDidLoad()

        // This code is used to unable to touch UIViewController after a transition
        // Fix the problem to call action in the behind UIViewController
        this.container?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return true
            }
        })
        return this.container
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        val activity = UIApplication.sharedApplication().activity()
        val display: Display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return transition.animator(this, size, enter)
    }

    fun viewWithTag(tag: Int): View {
        return this.container!!.findViewById(tag)
    }

    fun setHidden(hidden: Boolean) {
        this.container!!.visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }

    fun setUserInteractionEnabled(enabled: Boolean) {
        this.container!!.isEnabled = enabled
    }

    override fun onStart() {
        super.onStart()

        //
        // Should not notify controllers when app goes to background
        //
        if (!UIApplication.sharedApplication().isNotificationsEnabled)
            return

        this.viewController?.presentingViewController()?.viewWillDisappear(this.animated)
        this.viewController?.viewWillAppear(this.animated)
    }

    override fun onResume() {
        super.onResume()

        //
        // Should not notify controllers when app goes to background
        //
        if (!UIApplication.sharedApplication().isNotificationsEnabled)
            return

        this.viewController?.presentingViewController()?.viewDidDisappear(this.animated)
        this.viewController?.presentingViewController()?.view?.setUserInteractionEnabled(false)
        this.viewController?.viewDidAppear(this.animated)
    }

    override fun onPause() {
        super.onPause()

        //
        // Should not notify controllers when app goes to background
        //
        if (!UIApplication.sharedApplication().isNotificationsEnabled)
            return

        this.viewController?.presentingViewController()?.viewWillAppear(this.animated)
        this.viewController?.viewWillDisappear(this.animated)
    }

    override fun onStop() {
        super.onStop()

        //
        // Should not notify controllers when app goes to background
        //
        if (!UIApplication.sharedApplication().isNotificationsEnabled)
            return

        this.viewController?.presentingViewController()?.viewDidAppear(this.animated)
        this.viewController?.presentingViewController()?.view?.setUserInteractionEnabled(true)
        this.viewController?.viewDidDisappear(this.animated)
    }
}