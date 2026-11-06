package com.starkbank.cocoatouch.sample.textfield
import com.starkbank.cocoatouch.sample.R
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.annotation.IBOutlet
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton


class TextFieldViewController: BaseTextFieldViewController() {

    @IBOutlet(R.id.textField2)  lateinit var textField2: HintTextField


    override fun viewDidLoad() {
        super.viewDidLoad()
        this.textField1.setPlaceholder("Random TextField1")
        this.textField1.setText("")

        this.textField2.setPlaceholder("Random TextField2")
        this.textField2.setText("")
        this.textField2.becomeFirstResponder()
    }

    //
    // Actions
    //
    @IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {
        this.navigationController?.popViewController(true)
    }

    @IBAction(R.id.textfield_bfr) fun becomeFirstResponder(sender: UIButton) {
        this.textField1.becomeFirstResponder()
    }

    @IBAction(R.id.textfield_rfr) fun resignFirstResponder(sender: UIButton) {
        this.textField1.resignFirstResponder()
    }
}