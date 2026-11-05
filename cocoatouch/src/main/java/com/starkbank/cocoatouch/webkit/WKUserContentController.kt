package com.starkbank.cocoatouch.webkit

class WKUserContentController {

    var script: WKUserScript? = null

    fun addUserScript(script: WKUserScript) {
        this.script = script
    }

}