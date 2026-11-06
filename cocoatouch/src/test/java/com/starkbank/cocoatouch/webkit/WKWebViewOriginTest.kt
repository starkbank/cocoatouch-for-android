package com.starkbank.cocoatouch.webkit

import org.junit.Assert.assertEquals
import org.junit.Test

class WKWebViewOriginTest {

    @Test
    fun theSchemeAndHostAreLowercasedAndANonDefaultPortIsKept() {
        assertEquals("https://app.example.com:8443", originOf("HTTPS://App.Example.com:8443/x"))
    }

    @Test
    fun theDefaultPortOfTheSchemeIsDroppedLikeLocationHost() {
        assertEquals("https://a", originOf("https://a:443/x"))
        assertEquals("http://a", originOf("http://a:80/"))
        assertEquals("https://a:8443", originOf("https://a:8443/"))
        assertEquals("https://a:80", originOf("https://a:80/"))
    }

    @Test
    fun userInformationIsNotPartOfTheOrigin() {
        assertEquals("https://host", originOf("https://user@host/"))
    }

    @Test
    fun anInternationalHostIsPunycoded() {
        assertEquals("https://xn--bcher-kva.de", originOf("https://bücher.de/"))
    }

    @Test
    fun fileAndDataHaveAnEmptyHost() {
        assertEquals("file://", originOf("file:///android_asset/a.html"))
        assertEquals("data://", originOf("data:text/html,x"))
    }

    @Test
    fun anOverlongLabelDoesNotThrow() {
        assertEquals("https://${"a".repeat(64)}.com", originOf("https://${"A".repeat(64)}.com/"))
    }
}
