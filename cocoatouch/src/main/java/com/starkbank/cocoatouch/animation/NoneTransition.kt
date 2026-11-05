package com.starkbank.cocoatouch.animation
import androidx.fragment.app.Fragment
import android.animation.Animator
import android.graphics.Point


open class NoneTransition : Transition {

    override fun animator(fragment: Fragment, size: Point, enter: Boolean): Animator? {
        return null
    }
}