package com.starkbank.cocoatouch.sample.textfield
import com.starkbank.cocoatouch.sample.R
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.annotation.IBOutlet
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIControlState
import com.starkbank.cocoatouch.uikit.UILabel


class TextFieldViewController: BaseTextFieldViewController() {

    @IBOutlet(R.id.textFieldHeader) override var header: UILabel? = null

    @IBOutlet(R.id.textField2)  lateinit var textField2: HintTextField

    @IBOutlet(R.id.textfield_rfr) private lateinit var resignButton: UIButton


    override fun viewDidLoad() {
        super.viewDidLoad()
        this.header?.text = "Text fields"
        this.resignButton.setTitle("Resign", UIControlState.Normal)
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

    @IBAction(R.id.textfield_rfr) private fun resignFirstResponder(sender: UIButton) {
        this.textField1.resignFirstResponder()
    }
}