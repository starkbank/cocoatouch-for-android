package com.starkbank.cocoatouch.uikit

import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.starkbank.cocoatouch.annotation.IBActionParser
import com.starkbank.cocoatouch.annotation.IBOutletParser
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi


open class UIView() : UIResponder() {

    private var coder: Int? = null
    private var fragment: UIFragment? = null
    private var mTag: String? = null
    open var backgroundColor: UIColor? = null
        set(value) {
            field = value
            value?.let {
                fragment()?.setBackgroundColor(it)
                    ?: kotlin.run {
                        widget.setBackgroundColor(it.colorInt)
                    }
                if (_cornerRadii.size == 8) {
                    setViewBackground(
                        _cornerRadii[0].toInt(),
                        _cornerRadii[2].toInt(),
                        _cornerRadii[4].toInt(),
                        _cornerRadii[6].toInt()
                    )
                    return
                }
                if (cornerRadius > 0 || borderWidth > 0 || borderColor != null) {
                    setViewBackground()
                    return
                }
            }
        }

    private var _cornerRadii: FloatArray = floatArrayOf()

    var frame: Frame = Frame()
        set(value) {
            field = value
            val layoutParams = when (widget.layoutParams) {
                is LinearLayout.LayoutParams -> {
                    val params = widget.layoutParams
                    params.width = field.width
                    params.height = field.height
                    params
                }
                is FrameLayout.LayoutParams -> FrameLayout.LayoutParams(field.width, field.height)
                is ConstraintLayout.LayoutParams -> {
                    val params = widget.layoutParams
                    params.width = field.width
                    params.height = field.height
                    params
                }
                else -> {
                    ViewGroup.LayoutParams(field.width, field.height)
                }
            }
            widget.layoutParams = layoutParams
            widget.requestLayout()
        }
        get() {
            return Frame(widget.width, widget.height)
        }

    var cornerRadius: Int = 0
        set(value) {
            field = value
            setViewBackground()
        }

    var borderWidth: Int = 0
        set(value) {
            field = value
            setViewBackground()
        }

    var borderColor: UIColor? = null
        set(value) {
            field = value
            setViewBackground()
        }

    var accessibilityLabel: String?
        get() = widget.contentDescription?.toString()
        set(value) { widget.contentDescription = value }

    var clipsToBounds: Boolean
        get() = widget.clipToOutline
        set(value) {
            widget.clipToOutline = value
            (widget as? ViewGroup)?.clipChildren = value
        }

    var alpha: Float = 1f
        set(value) {
            field = value
            widget.alpha = value
        }

    val intrinsicContentSize: CGSize
        get() {
            widget.measure(0,0)
            return CGSize(widget.measuredWidth, widget.measuredHeight)
        }

    var tag: String
        get() {
            return mTag ?: widget.id.toString()
        }
        set(value) {
            mTag = value
        }

    val isHidden: Boolean
        get() {
            return widget.visibility != View.VISIBLE
        }

    constructor(coder: Int? = null) : this() {
        this.coder = coder
        val inflater = LayoutInflater.from(UIApplication.context())
        coder?.let {
            widget = inflater.inflate(it, null)
            IBOutletParser.parse(this)
            IBActionParser.parse(this)
        }
    }

    fun addSubView(view: UIView) {
        (widget as? ViewGroup)?.let{
            it.addView(view.widget)
            it.requestLayout()
        }
    }

    fun removeFromSuperview() {
        widget.removeFromParent()
    }

    //
    // Public Instance Methods
    //
    @InternalCocoaTouchApi
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

    open fun animate(
        withDuration: Long,
        animations: String,
        animationValue: Float
    ) {
        ObjectAnimator.ofFloat(
            this.widget,
            animations,
            animationValue
        ).apply {
            duration = withDuration
            start()
        }
    }

    fun addGestureRecognizer(callback: (sender: Any) -> Unit) {
        this.widget.setOnClickListener {
            callback(this)
        }
    }

    fun setNeedsDisplay() {
        this.widget.requestLayout()
    }

    //
    // Internal Methods
    //
    open fun setViewBackground() {
        val shape = GradientDrawable()
        shape.cornerRadius = cornerRadius.toFloat().dpToPx
        backgroundColor?.let { shape.setColor(it.colorInt) }
        borderColor?.let { shape.setStroke(borderWidth.dpToPx, it.colorInt) }
        (widget).background = shape
    }

    open fun setViewBackground(topLeft: Int, topRight: Int, bottomLeft: Int, bottomRight: Int) {
        val shape = GradientDrawable()
        _cornerRadii = floatArrayOf(
            topLeft.toFloat().dpToPx,
            topLeft.toFloat().dpToPx,
            topRight.toFloat().dpToPx,
            topRight.toFloat().dpToPx,
            bottomLeft.toFloat().dpToPx,
            bottomLeft.toFloat().dpToPx,
            bottomRight.toFloat().dpToPx,
            bottomRight.toFloat().dpToPx,
        )
        shape.cornerRadii = _cornerRadii
        backgroundColor?.let { shape.setColor(it.colorInt) }
        borderColor?.let { shape.setStroke(borderWidth.dpToPx, it.colorInt) }
        (widget).background = shape
    }

    @InternalCocoaTouchApi
    fun fragment(): UIFragment? {
        return this.fragment
    }

    @InternalCocoaTouchApi
    fun initWith(viewController: UIViewController, identifier: Int): UIView {
        val fragment = UIFragment()
        fragment.identifier = identifier
        fragment.viewController = viewController
        this.fragment = fragment
        return this
    }

}

class Frame {
    var width = 0
    var height = 0

    constructor(width: Int = 0, height: Int = 0) {
        this.width = width
        this.height = height
    }
}

class CGSize {
    var width = 0
    var height = 0

    constructor(width: Int = 0, height: Int = 0) {
        this.width = width
        this.height = height
    }
}
