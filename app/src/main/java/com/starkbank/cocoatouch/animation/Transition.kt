package com.starkbank.cocoatouch.animation
import androidx.fragment.app.Fragment
import android.animation.Animator
import android.graphics.Point


interface Transition {
    fun animator(fragment: Fragment, size: Point, enter: Boolean): Animator?
}