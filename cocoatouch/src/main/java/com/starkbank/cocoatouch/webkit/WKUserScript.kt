package com.starkbank.cocoatouch.webkit

class WKUserScript(val source: String, val injectionTime: WKUserScriptInjectionTime) {

    enum class WKUserScriptInjectionTime {
        atDocumentEnd
    }
}