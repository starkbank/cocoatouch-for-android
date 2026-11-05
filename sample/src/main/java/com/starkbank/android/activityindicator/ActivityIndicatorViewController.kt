package com.starkbank.android.activityindicator
import com.starkbank.android.R
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.annotation.IBOutlet
import com.starkbank.cocoatouch.compability.DefaultActions
import com.starkbank.cocoatouch.uikit.UIActivityIndicatorView
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIViewController


class ActivityIndicatorViewController: UIViewController() {

    @IBOutlet(R.id.spinner) lateinit var spinner: UIActivityIndicatorView


    override fun viewDidLoad() {
        super.viewDidLoad()
        this.spinner.startAnimating()
    }

    //
    // Actions
    //
    @IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {
        this.navigationController?.popViewController(true)
    }

    @IBAction(R.id.buttonStopSpinner) fun stop(sender: UIButton) {
        this.spinner.stopAnimating()
    }

    @IBAction(R.id.buttonStartSpinner) fun start(sender: UIButton) {
        this.spinner.startAnimating()
    }
}