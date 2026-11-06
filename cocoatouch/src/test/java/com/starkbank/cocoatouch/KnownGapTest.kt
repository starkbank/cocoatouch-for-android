package com.starkbank.cocoatouch

import com.starkbank.cocoatouch.uikit.UIProgressView
import com.starkbank.cocoatouch.uikit.UIScrollView
import com.starkbank.cocoatouch.uikit.UITextField
import com.starkbank.cocoatouch.uikit.UITextFieldDelegate
import com.starkbank.cocoatouch.uikit.UIView
import org.junit.Assert.assertEquals
import org.junit.Test

class KnownGapTest {

    // Pins audit CT-04: the delegate callback returns Unit, so it cannot veto an edit. Not fixed in this version.
    @Test
    fun ct04ShouldChangeCannotVeto() {
        val method = UITextFieldDelegate::class.java.getMethod(
            "textFieldShouldChange",
            UITextField::class.java,
            CharSequence::class.java,
            Int::class.java,
            Int::class.java,
            Int::class.java
        )
        assertEquals(Void.TYPE, method.returnType)
    }

    // Pins audit CT-12: UIView.tag is a String where UIKit has an Int. Not fixed in this version.
    @Test
    fun ct12TagIsAStringNotAnInt() {
        assertEquals(String::class.java, UIView::class.java.getMethod("getTag").returnType)
    }

    // Pins audit CT-17: both classes are empty shells. Not fixed in this version.
    @Test
    fun ct17ProgressViewAndScrollViewDeclareNoMembers() {
        assertEquals(0, UIProgressView::class.java.declaredMethods.size)
        assertEquals(0, UIScrollView::class.java.declaredMethods.size)
    }
}
