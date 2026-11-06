package com.starkbank.cocoatouch.uikit

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import com.starkbank.cocoatouch.compatibility.UIKeyInput


open class UITextField : UIControl() {

    var isSecureTextEntry: Boolean
        get() {
            return editText().transformationMethod == PasswordTransformationMethod.getInstance()
        }
        set(value) {
            val cursorPos = editText().selectionStart
            if(value)
                editText().transformationMethod = PasswordTransformationMethod.getInstance()
            else
                editText().transformationMethod = HideReturnsTransformationMethod.getInstance()
            editText().setSelection(cursorPos)
        }

    private var delegate: UITextFieldDelegate? = null
    private var observer: TextWatcher? = null
    private var ignoreTextChange: Boolean = false
    private var originalBackground: Drawable? = null

    private fun editText(): BackEditText {
        return this.widget as BackEditText
    }

    fun init() {
        originalBackground = this.editText().background
        addListeners()
    }

    fun setText(text: String) {
        // Restoring the previous value keeps a re-entrant setText from an app watcher from unlocking the outer call
        val wasIgnoring = ignoreTextChange
        ignoreTextChange = true
        try {
            this.editText().setText(text)
            this.editText().setSelection(text.length)
        } finally {
            ignoreTextChange = wasIgnoring
        }
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
        UIKeyInput.getInputMethodManager()?.showSoftInput(this.editText(), InputMethodManager.SHOW_IMPLICIT)
    }

    fun resignFirstResponder() {
        textFieldDidFinishEditing()
        // Before Android P clearFocus hands focus to the first focusable view in touch mode, so the field steps out of
        // that search while it clears and is made reachable again right after
        this.editText().isFocusableInTouchMode = false
        this.editText().clearFocus()
        this.editText().isFocusableInTouchMode = true
        UIKeyInput.getInputMethodManager()?.hideSoftInputFromWindow(this.editText().windowToken, 0)
    }

    //
    // Listeners
    //
    private fun addListeners() {
        removeListeners()
        addTextListener()
        addKeyListener()
        addTouchListener()
        addFocusListener()
        addBackListener()
    }

    private fun removeListeners() {
        this.editText().removeTextChangedListener(observer)
        observer = null
        this.editText().setOnKeyListener(null)
        this.editText().setOnTouchListener(null)
        this.editText().setOnFocusChangeListener(null)
        this.editText().setOnBackListener(null)
    }

    private fun addBackListener() {
        this.editText().setOnBackListener(object : BackEditText.OnBackListener {
            override fun onBack(keyCode: Int, event: KeyEvent?) {
                textFieldDidFinishEditing()
            }
        })
    }

    private fun addTextListener() {
        if (observer != null)
            return
        val textField = this
        val watcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreTextChange)
                    return
                delegate?.textFieldShouldChange(textField, s, start, before, count)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (ignoreTextChange)
                    return
                delegate?.textFieldBeforeChange(textField, s, start, count, after)
            }

            override fun afterTextChanged(s: Editable) {
                if (ignoreTextChange)
                    return
                delegate?.textFieldDidChange(textField)
            }
        }
        observer = watcher
        this.editText().addTextChangedListener(watcher)
    }

    private fun addKeyListener() {
        val textField = this
        this.editText().setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == MotionEvent.ACTION_UP) {
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
        this.editText().setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                becomeFirstResponder()
            }
            false
        }
    }

    private fun addFocusListener() {
        this.editText().onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (!hasFocus)
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

open class BackEditText @JvmOverloads constructor(context: Context?, attrs: AttributeSet?) :
    AppCompatEditText(context!!, attrs) {

    private var listener: OnBackListener? = null

    interface OnBackListener {
        fun onBack(keyCode: Int, event: KeyEvent?)
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