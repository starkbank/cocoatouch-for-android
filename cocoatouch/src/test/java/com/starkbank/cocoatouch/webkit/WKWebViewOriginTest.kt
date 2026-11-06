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
    fun aFilePageIsIdentifiedByItsPath() {
        assertEquals("file:///android_asset/a.html", originOf("file:///android_asset/a.html"))
    }

    @Test
    fun fileLocalhostIsTheSameAsNoHost() {
        assertEquals("file:///x", originOf("file://localhost/x"))
    }

    @Test
    fun fileQueryAndFragmentAreDropped() {
        assertEquals("file:///android_asset/a.html", originOf("file:///android_asset/a.html?q#h"))
        assertEquals("file:///android_asset/a.html", originOf("file:///android_asset/a.html#h?q"))
    }

    @Test
    fun aDataUrlHasNoOrigin() {
        assertEquals("", originOf("data:text/html,x"))
    }

    @Test
    fun aboutBlankHasNoOrigin() {
        assertEquals("", originOf("about:blank"))
    }

    @Test
    fun anOverlongLabelDoesNotThrow() {
        assertEquals("https://${"a".repeat(64)}.com", originOf("https://${"A".repeat(64)}.com/"))
    }

    @Test
    fun thePortIsParsedAsANumber() {
        assertEquals("https://a", originOf("https://a:/x"))
        assertEquals("https://a", originOf("https://a:0443/x"))
        assertEquals("https://a:8443", originOf("https://a:08443/"))
        assertEquals("", originOf("https://a:x/"))
    }
}
