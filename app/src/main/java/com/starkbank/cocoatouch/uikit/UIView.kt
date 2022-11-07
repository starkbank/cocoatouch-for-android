package com.starkbank.cocoatouch.uikit


open class UIView: UIResponder() {

    private var fragment: UIFragment? = null

    //
    // Public Instance Methods
    //
    override fun viewWithTag(tag: Int): Any {
        return this.fragment!!.viewWithTag(tag)
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