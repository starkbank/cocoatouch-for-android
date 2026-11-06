package com.starkbank.cocoatouch.uikit

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RotateDrawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.ColorInt
import kotlin.math.sqrt


internal val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

internal val Float.dpToPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

internal fun View.removeFromParent() {
    this.parent?.let {
        val parentViewGroup = it as ViewGroup
        parentViewGroup.removeView(this)
    }
}

internal fun UIColor.isLight(): Boolean {
    val darkness =
        1 - (0.299 * Color.red(colorInt) + 0.587 * Color.green(colorInt) + 0.114 * Color.blue(colorInt)) / 255
    return darkness < 0.5
}

internal fun EditText.setCursorDrawableColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    textCursorDrawable =
        GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(color, color))
            .apply { setSize(2.spToPx(context).toInt(), textSize.toInt()) }
}

internal fun EditText.setHandlesColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val size = 22.spToPx(context).toInt()
    val corner = size.toFloat() / 2
    val inset = 10.spToPx(context).toInt()

    //left drawable
    val drLeft = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(color, color))
    drLeft.setSize(size, size)
    drLeft.cornerRadii = floatArrayOf(corner, corner, 0f, 0f, corner, corner, corner, corner)
    setTextSelectHandleLeft(InsetDrawable(drLeft, inset, 0, inset, inset))

    //right drawable
    val drRight = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(color, color))
    drRight.setSize(size, size)
    drRight.cornerRadii = floatArrayOf(0f, 0f, corner, corner, corner, corner, corner, corner)
    setTextSelectHandleRight(InsetDrawable(drRight, inset, 0, inset, inset))

    //middle drawable
    val drMiddle = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(color, color))
    drMiddle.setSize(size, size)
    drMiddle.cornerRadii = floatArrayOf(0f, 0f, corner, corner, corner, corner, corner, corner)
    val mInset = (sqrt(2f) * corner - corner).toInt()
    val insetDrawable = InsetDrawable(drMiddle, mInset, mInset, mInset, mInset)
    val rotateDrawable = RotateDrawable()
    rotateDrawable.drawable = insetDrawable
    rotateDrawable.toDegrees = 45f
    rotateDrawable.level = 10000
    setTextSelectHandle(rotateDrawable)
}

internal fun Number.spToPx(context: Context? = null): Float {
    val res = context?.resources ?: Resources.getSystem()
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), res.displayMetrics)
}
