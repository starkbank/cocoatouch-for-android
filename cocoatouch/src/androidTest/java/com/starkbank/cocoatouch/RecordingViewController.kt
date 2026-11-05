package com.starkbank.cocoatouch

import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIViewController


open class RecordingViewController : UIViewController() {

    val events = mutableListOf<String>()

    override fun viewDidLoad() {
        super.viewDidLoad()
        events.add("viewDidLoad")
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        events.add("viewWillAppear")
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        events.add("viewDidAppear")
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        events.add("viewWillDisappear")
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        events.add("viewDidDisappear")
    }

    @IBAction(DefaultActions.onBackPressed)
    fun back(sender: UIButton) {
        events.add("back")
    }
}

class PushedViewController : RecordingViewController()
