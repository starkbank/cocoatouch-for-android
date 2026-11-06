package com.starkbank.cocoatouch.compatibility

import com.starkbank.cocoatouch.annotation.GrandChildActionFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class BackPressedParserTest {

    @Test
    fun backPressedMethodIsFoundTwoLevelsUp() {
        assertEquals("back", BackPressedParser.getMethod(GrandChildActionFixture())!!.name)
    }
}
