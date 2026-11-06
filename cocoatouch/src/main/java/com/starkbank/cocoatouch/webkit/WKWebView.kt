package com.starkbank.cocoatouch.webkit

import android.webkit.WebView
import android.webkit.WebViewClient
import com.starkbank.cocoatouch.foundation.URLRequest
import com.starkbank.cocoatouch.uikit.UIView
import org.json.JSONObject
import java.net.IDN
import java.util.Locale


open class WKWebView: UIView() {

    var configuration: WKWebViewConfiguration? = null
        set(value) {
            value?.userContentController?.script?.let { script ->
                getWebView().settings.javaScriptEnabled = true
                getWebView().webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (view == null || url == null) return
                        if (script.injectionTime != WKUserScript.WKUserScriptInjectionTime.atDocumentEnd) return
                        val origin = loadedOrigin ?: return
                        // The script is meant for the page passed to load: a page that navigates or redirects
                        // elsewhere never runs it. The check runs again inside the document that executes the script
                        // because the page may have navigated after this callback, and the sentinel keeps a document
                        // from being injected twice
                        if (originOf(url) != origin) return
                        view.evaluateJavascript(guardedSource(origin, script.source)) {}
                    }
                }
            }
            field = value
        }

    private var loadedOrigin: String? = null

    private fun getWebView(): WebView {
        return (widget as WebView)
    }

    fun load(request: URLRequest) {
        if (!request.url.startsWith("javascript:", ignoreCase = true)) loadedOrigin = originOf(request.url)
        getWebView().loadUrl(request.url)
    }

    private fun guardedSource(origin: String, source: String): String {
        return "if ((location.protocol + '//' + location.host) === ${JSONObject.quote(origin)} && " +
            "!window.__cocoatouchUserScript) { window.__cocoatouchUserScript = true; $source }"
    }

}

private val urlPattern = Regex("^([a-zA-Z][a-zA-Z0-9+.-]*):(?://([^/?#]*))?")

// Mirrors JS location.protocol + '//' + location.host: lowercase scheme, punycode host, port only when written
internal fun originOf(url: String): String {
    val match = urlPattern.find(url) ?: return ""
    val scheme = match.groupValues[1].lowercase(Locale.ROOT)
    val authority = match.groupValues[2].substringAfterLast('@')
    val portIndex = authority.lastIndexOf(':')
    val hasPort = portIndex >= 0 && !authority.endsWith("]")
    val host = if (hasPort) authority.substring(0, portIndex) else authority
    val port = if (hasPort) authority.substring(portIndex) else ""
    return "$scheme://${asciiHost(host)}$port"
}

// toASCII rejects a label longer than 63 characters; such a host cannot be a real origin, so it is kept as written
private fun asciiHost(host: String): String {
    return try {
        IDN.toASCII(host).lowercase(Locale.ROOT)
    } catch (error: IllegalArgumentException) {
        host.lowercase(Locale.ROOT)
    }
}
