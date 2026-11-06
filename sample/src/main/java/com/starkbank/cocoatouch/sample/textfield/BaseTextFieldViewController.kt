package com.starkbank.cocoatouch.sample.textfield
import com.starkbank.cocoatouch.sample.R
import com.starkbank.cocoatouch.annotation.IBOutlet
import com.starkbank.cocoatouch.uikit.UITextField
import com.starkbank.cocoatouch.uikit.UIViewController


open class BaseTextFieldViewController : UIViewController() {

    @IBOutlet(R.id.textField1) lateinit var textField1: UITextField
}
