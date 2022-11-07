package com.starkbank.cocoatouch.foundation


class NSIndexPath(section: Int, row: Int) {

    var row = 0
    var section = 0

    init {
        this.section = section
        this.row = row
    }
}