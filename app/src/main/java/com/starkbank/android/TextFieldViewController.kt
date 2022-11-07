package com.starkbank.android
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.annotation.IBOutlet
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UITextField
import com.starkbank.cocoatouch.uikit.UIViewController


class TextFieldViewController: UIViewController() {

    @IBOutlet(R.id.textField1)  lateinit var textField1: UITextField
    @IBOutlet(R.id.textField2)  lateinit var textField2: UITextField


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
    @IBAction(R.id.textfield_bfr) fun becomeFirstResponder(sender: UIButton) {
        this.textField1.becomeFirstResponder()
    }

    @IBAction(R.id.textfield_rfr) fun resignFirstResponder(sender: UIButton) {
        this.textField1.resignFirstResponder()
    }
}