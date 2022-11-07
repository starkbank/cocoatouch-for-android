package com.starkbank.cocoatouch.uikit
import android.view.View


open class UIView: UIResponder() {

    private var fragment: UIFragment? = null

    //
    // Public Instance Methods
    //
    override fun viewWithTag(tag: Int): View {
        if (this.fragment != null) {
            return this.fragment!!.viewWithTag(tag)
        }
        return super.viewWithTag(tag)
    }

    open fun setHidden(hidden: Boolean) {
        this.fragment!!.isHidden = hidden
    }

    open fun setUserInteractionEnabled(enabled: Boolean) {
        this.fragment!!.setUserInteractionEnabled(enabled)
    }

    //
    // Internal Methods
    //
    internal fun fragment(): UIFragment? {
        return this.fragment
    }

    internal fun initWith(viewController: UIViewController, identifier: Int) : UIView {
        val fragment = UIFragment()
        fragment.identifier = identifier;
        fragment.viewController = viewController;
        this.fragment = fragment
        return this
    }
}