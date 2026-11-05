package com.starkbank.cocoatouch.webkit

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
                    override fun onLoadResource(view: WebView?, url: String?) {
                        super.onLoadResource(view, url)
                        if (script.injectionTime == WKUserScript.WKUserScriptInjectionTime.atDocumentEnd) {
                            view?.let { webView ->
                                webView.evaluateJavascript(
                                    script.source
                                ) {}
                            }
                        }
                    }
                }
            }
            field = value
        }

    private fun getWebView(): WebView {
        return (widget as WebView)
    }

    fun load(request: URLRequest) {
        getWebView().loadUrl(request.url)
    }

}