package com.starkbank.android
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.uikit.*


class MainViewController: UIViewController() {

    override fun viewDidLoad() {
        super.viewDidLoad()
    }

    @IBAction(R.id.mainTextfield) fun presentTextController(sender: UIButton) {
        presentViewController(R.layout.textfieldviewcontroller)
    }

    @IBAction(R.id.mainTableview) fun presentTableViewController(sender: UIButton) {
        presentViewController(R.layout.tableviewcontroller)
    }

    //
    // Helpers
    //
    private fun presentViewController(identifier: Int) {
        val controller = this.storyboard!!.instantiateViewControllerWithIdentifier(identifier)
        this.navigationController!!.pushViewController(controller, true)
    }
}