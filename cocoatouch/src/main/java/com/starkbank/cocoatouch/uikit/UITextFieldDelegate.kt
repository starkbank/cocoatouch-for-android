package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.annotation.Optional


interface UITextFieldDelegate {
    @Optional fun textFieldShouldReturn(textField: UITextField): Boolean
    @Optional fun textFieldDidEndEditing(textField: UITextField)
    @Optional fun textFieldDidBeginEditing(textField: UITextField)
    @Optional fun textFieldDidChange(textField: UITextField)
    @Optional fun textFieldShouldChange(textField: UITextField, s: CharSequence, start: Int, before: Int, count: Int)
    @Optional fun textFieldBeforeChange(textField: UITextField, s: CharSequence, start: Int, count: Int, after: Int)
}