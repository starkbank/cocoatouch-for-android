package com.starkbank.cocoatouch.animation
import android.animation.Animator
import android.graphics.Point
import androidx.fragment.app.Fragment
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi


@InternalCocoaTouchApi
open class NoneTransition : Transition {

    override fun animator(fragment: Fragment, size: Point, enter: Boolean): Animator? {
        return null
    }
}