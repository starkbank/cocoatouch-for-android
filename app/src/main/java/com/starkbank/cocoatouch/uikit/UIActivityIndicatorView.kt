package com.starkbank.cocoatouch.uikit


class UIActivityIndicatorView: UIView() {

    fun stopAnimating() {
        this.setHidden(true)
    }

    fun startAnimating() {
        this.setHidden(false)
    }
}