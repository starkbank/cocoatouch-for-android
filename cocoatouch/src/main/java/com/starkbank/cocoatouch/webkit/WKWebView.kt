package com.starkbank.cocoatouch.webkit

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import com.starkbank.cocoatouch.foundation.URLRequest
import com.starkbank.cocoatouch.uikit.UIView


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
                        if (originOf(url) != loadedOrigin) return
                        view.evaluateJavascript(script.source) {}
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
        loadedOrigin = originOf(request.url)
        getWebView().loadUrl(request.url)
    }

    // The script is meant for the page passed to load: a page that navigates or redirects elsewhere never runs it
    private fun originOf(url: String): String {
        val uri = Uri.parse(url)
        return "${uri.scheme}://${uri.host.orEmpty()}"
    }

}
