package com.starkbank.cocoatouch.webkit

import android.webkit.WebView
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.foundation.URLRequest
import com.starkbank.cocoatouch.onMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class WKWebViewTest {

    private val pageA = "file:///android_asset/cocoatouch_test_web_a.html"
    private val countScript = "String(window.injected)"
    private val countingSource = "window.injected = (window.injected || 0) + 1"
    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var webView: WKWebView
    private lateinit var widget: WebView

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
        scenario.onActivity { activity ->
            widget = WebView(activity)
            activity.addContentView(widget, FrameLayout.LayoutParams(400, 400))
            webView = WKWebView()
            webView.widget = widget
        }
    }

    @After
    fun close() {
        onMain { widget.destroy() }
        scenario.close()
    }

    @Test
    fun theScriptRunsExactlyOnceOnTheLoadedPage() {
        configure()
        onMain { webView.load(URLRequest(pageA)) }
        assertEquals("\"1\"", awaitResult(countScript, "\"1\""))
        Thread.sleep(500)
        assertEquals("\"1\"", evaluate(countScript))
    }

    @Test
    fun sameDocumentNavigationDoesNotInjectAgain() {
        configure()
        onMain { webView.load(URLRequest(pageA)) }
        assertEquals("\"1\"", awaitResult(countScript, "\"1\""))
        evaluate("location.hash = 'x'")
        Thread.sleep(500)
        assertEquals("\"1\"", evaluate(countScript))
    }

    @Test
    fun aPageThatNavigatesAwayOnLoadDoesNotLeakTheScript() {
        configure()
        onMain { webView.load(URLRequest("file:///android_asset/cocoatouch_test_web_c.html")) }
        val onBlankPage = "location.href === 'about:blank' ? String(window.__cocoatouchUserScript) : 'waiting'"
        assertEquals("\"undefined\"", awaitResult(onBlankPage, "\"undefined\""))
        Thread.sleep(500)
        assertEquals("\"undefined\"", evaluate(onBlankPage))
        assertEquals("\"undefined\"", evaluate(countScript))
    }

    @Test
    fun aNavigationWithinTheLoadedOriginInjectsTheNewPageOnce() {
        configure()
        onMain { webView.load(URLRequest(pageA)) }
        assertEquals("\"1\"", awaitResult(countScript, "\"1\""))
        evaluate("document.getElementById('go').click()")
        val onPageB = "location.pathname.endsWith('web_b.html') ? $countScript : 'waiting'"
        assertEquals("\"1\"", awaitResult(onPageB, "\"1\""))
        Thread.sleep(500)
        assertEquals("\"1\"", evaluate(onPageB))
    }

    @Test
    fun aPageFromAnotherOriginIsNeverInjected() {
        configure()
        onMain { webView.load(URLRequest(pageA)) }
        assertEquals("\"1\"", awaitResult(countScript, "\"1\""))
        onMain { widget.loadUrl("data:text/html,<html><body>x</body></html>") }
        val onDataPage = "location.protocol === 'data:' ? $countScript : 'waiting'"
        awaitResult(onDataPage, "\"undefined\"")
        Thread.sleep(1000)
        assertEquals("\"undefined\"", evaluate(onDataPage))
    }

    @Test
    fun aUserScriptEndingInALineCommentRuns() {
        configure("window.injected = 1 // notify")
        onMain { webView.load(URLRequest(pageA)) }
        assertEquals("\"1\"", awaitResult(countScript, "\"1\""))
    }

    @Test
    fun aUserScriptTopLevelDeclarationStaysInsideTheGuardBlock() {
        configure("const cocoatouchApi = 7; window.cocoatouchExposed = 7; window.injected = 1")
        onMain { webView.load(URLRequest(pageA)) }
        assertEquals("\"1\"", awaitResult(countScript, "\"1\""))
        assertEquals("\"undefined\"", evaluate("typeof cocoatouchApi"))
        assertEquals("\"7\"", evaluate("String(window.cocoatouchExposed)"))
    }

    @Test
    fun aUseStrictDirectiveIsPreserved() {
        configure("\"use strict\"; window.strictThis = (function () { return this === undefined })()")
        onMain { webView.load(URLRequest(pageA)) }
        assertEquals("\"true\"", awaitResult("String(window.strictThis)", "\"true\""))
    }

    @Test
    fun theGuardKeepsFunctionsFromAnotherOrigin() {
        onMain {
            widget.settings.javaScriptEnabled = true
            webView.load(URLRequest(pageA))
        }
        awaitResult("document.readyState", "\"complete\"")
        evaluate(guardedSource("https://other.test", "function leaked() { return 1 }"))
        assertEquals("\"undefined\"", evaluate("typeof leaked"))
    }

    @Test
    fun theGuardRefusesAnotherOriginInsideTheDocument() {
        onMain {
            widget.settings.javaScriptEnabled = true
            webView.load(URLRequest(pageA))
        }
        awaitResult("document.readyState", "\"complete\"")
        evaluate(guardedSource("https://other.test", countingSource))
        assertEquals("\"undefined\"", evaluate(countScript))
        evaluate(guardedSource("file://", countingSource))
        assertEquals("\"1\"", evaluate(countScript))
        evaluate(guardedSource("file://", countingSource))
        assertEquals("\"1\"", evaluate(countScript))
    }

    private fun configure(source: String = countingSource) {
        onMain {
            webView.configuration = WKWebViewConfiguration().also {
                it.userContentController = WKUserContentController().also { controller ->
                    controller.addUserScript(WKUserScript(source, WKUserScript.WKUserScriptInjectionTime.atDocumentEnd))
                }
            }
        }
    }

    private fun evaluate(script: String): String {
        val latch = CountDownLatch(1)
        var result = ""
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            widget.evaluateJavascript(script) {
                result = it
                latch.countDown()
            }
        }
        if (!latch.await(2, TimeUnit.SECONDS)) fail("evaluateJavascript did not answer: $script")
        return result
    }

    private fun awaitResult(script: String, expected: String): String {
        val deadline = System.currentTimeMillis() + 5000
        var last = evaluate(script)
        while (last != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
            last = evaluate(script)
        }
        return last
    }
}
