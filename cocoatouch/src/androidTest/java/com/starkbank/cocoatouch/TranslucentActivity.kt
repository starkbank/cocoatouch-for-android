package com.starkbank.cocoatouch

import com.starkbank.cocoatouch.test.R
import com.starkbank.cocoatouch.uikit.UIActivity


class TranslucentActivity : UIActivity() {

    override fun storyboardId(): Int {
        return R.layout.cocoatouch_test_root
    }
}
