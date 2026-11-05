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
        if (this.fragment != null) {
            this.fragment!!.isHidden = hidden
            return
        }
        this.widget.visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }

    open fun setUserInteractionEnabled(enabled: Boolean) {
        if (this.fragment != null) {
            this.fragment!!.setUserInteractionEnabled(enabled)
            return
        }
        this.widget.isEnabled = enabled
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