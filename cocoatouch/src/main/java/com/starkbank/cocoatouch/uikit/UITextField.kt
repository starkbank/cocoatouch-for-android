package com.starkbank.cocoatouch.uikit
import androidx.appcompat.widget.AppCompatEditText
import android.view.inputmethod.InputMethodManager
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.text.TextWatcher
import android.view.MotionEvent
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.view.KeyEvent
import android.app.Activity
import android.view.View


class UITextField: UIControl() {

    private var delegate: UITextFieldDelegate? = null
    private var observer: TextWatcher? = null
    private var originalBackground: Drawable? = null

    private fun editText(): BackEditText {
        return this.widget as BackEditText
    }

    fun init() {
        originalBackground = this.editText().background
    }

    fun setText(text: String) {
        this.editText().setText(text)
        this.editText().setSelection(text.length)
    }

    fun text(): String {
        return this.editText().text.toString()
    }

    fun setDelegate(delegate: UITextFieldDelegate?) {
        this.delegate = delegate
        if (this.delegate != null)
            return addListeners()
        removeListeners()
    }

    fun setPlaceholder(placeholder: String) {
        this.editText().hint = placeholder
    }

    fun placeholder(): String {
        return this.editText().hint.toString()
    }

    override fun setHidden(hidden: Boolean) {
        super.setHidden(hidden)
        this.editText().visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }

    fun becomeFirstResponder() {
        textFieldDidStartEditing()
        this.editText().isFocusableInTouchMode = true
        this.editText().requestFocus()
        val activity: Activity = UIApplication.sharedApplication().activity()
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun resignFirstResponder() {
        textFieldDidFinishEditing()
        val activity: Activity = UIApplication.sharedApplication().activity()
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.editText().windowToken, 0)
    }

    //
    // Listeners
    //
    private fun addListeners() {
        addTextListener()
        addKeyListener()
        addTouchListener()
        addFocusListener()
        addBackListener()
    }

    private fun removeListeners() {
        this.editText().removeTextChangedListener(observer)
        this.editText().setOnKeyListener(null)
        this.editText().setOnTouchListener(null)
        this.editText().setOnFocusChangeListener(null)
        this.editText().setOnBackListener(null)
    }

    private fun addBackListener() {
        this.editText().setOnBackListener(object: BackEditText.OnBackListener {
            override fun onBack(keyCode: Int, event: KeyEvent?) {
                textFieldDidFinishEditing()
            }
        })
    }

    private fun addTextListener() {
        val delegate = delegate
        val textField = this
        this.editText().addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                delegate?.textFieldShouldChange(textField, s, start, before, count)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                delegate?.textFieldBeforeChange(textField, s, start, count, after)
            }

            override fun afterTextChanged(s: Editable) {
                delegate?.textFieldDidChange(textField)
            }
        })
    }

    private fun addKeyListener() {
        val textField = this
        this.editText().setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    var shouldResign = true
                    if (delegate != null)
                        shouldResign = delegate!!.textFieldShouldReturn(textField)
                    if (shouldResign)
                        resignFirstResponder()
                    return shouldResign
                }
                return false
            }
        })
    }

    private fun addTouchListener() {
        this.editText().setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    becomeFirstResponder()
                }
                return true
            }
        })
    }

    private fun addFocusListener() {
        this.editText().onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if(!hasFocus)
                    textFieldDidFinishEditing()
            }
        }
    }

    //
    // Style + Delegate Notifications
    //
    private fun textFieldDidFinishEditing() {
        this.editText().isCursorVisible = false
        this.editText().setBackgroundColor(Color.TRANSPARENT)
        delegate?.textFieldDidEndEditing(this)
    }

    private fun textFieldDidStartEditing() {
        this.editText().isCursorVisible = true
        this.editText().background = originalBackground
        delegate?.textFieldDidBeginEditing(this)
    }
}

open class BackEditText: AppCompatEditText {

    private var listener: OnBackListener? = null

    interface OnBackListener {
        fun onBack(keyCode: Int, event: KeyEvent?)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {

    }

    open fun setOnBackListener(`object`: OnBackListener?) {
        this.listener = `object`
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            listener?.onBack(keyCode, event)
        }
        return super.onKeyPreIme(keyCode, event)
    }
}