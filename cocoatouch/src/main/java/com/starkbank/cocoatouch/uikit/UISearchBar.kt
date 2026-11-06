package com.starkbank.cocoatouch.uikit

import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import com.starkbank.cocoatouch.compatibility.UIKeyInput
import com.starkbank.cocoatouch.foundation.weakReference


class UISearchBar : UIView() {

    var tintColor: UIColor? = null
        set(value) {
            field = value
            value?.let {
                getEditText().setCursorDrawableColor(value.colorInt)
                getEditText().setHandlesColor(value.colorInt)
            }
        }

    var textColor: UIColor? = null
        set(value) {
            field = value
            value?.let {
                getEditText().setTextColor(value.colorInt)
                getEditText().setHintTextColor(value.colorInt)

                val searchIcon =
                    getSearchView().findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
                searchIcon.setColorFilter(value.colorInt)

                val closeIcon =
                    getSearchView().findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
                closeIcon.setColorFilter(value.colorInt)
            }
        }

    var barTintColor: UIColor? = UIApplication.appearance.secondaryBackgroundColor(UITraitCollection.current)
        set(value) {
            field = value
            backgroundColor = value
        }

    var text: String? = null
        get() {
            return getSearchView().query.toString()
        }
        set(value) {
            field = value
            getSearchView().setQuery(value, false)
        }

    var placeholder: String? = null
        set(value) {
            field = value
            getSearchView().queryHint = placeholder
        }

    private var weakDelegate: UISearchBarDelegate? by weakReference()

    var delegate: UISearchBarDelegate?
        get() = weakDelegate
        set(value) {
            weakDelegate = value
            installFocusListener()
        }

    var placeholderTextColor: UIColor? = null
        set(value) {
            field = value
            field?.let { getEditText().setHintTextColor(it.colorInt) }
        }


    private fun getSearchView(): SearchView {
        return (widget as SearchView)
    }

    internal fun getEditText(): EditText {
        return getSearchView().findViewById(androidx.appcompat.R.id.search_src_text) as EditText
    }

    private fun installFocusListener() {
        getSearchView().setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus && delegate?.searchBarShouldBeginEditing(this) == false) {
                v.clearFocus()
            }
        }
    }

    fun resignFirstResponder() {
        this.getEditText().clearFocus()
        this.getSearchView().clearFocus()
        UIKeyInput.getInputMethodManager()
            ?.hideSoftInputFromWindow(this.getEditText().windowToken, 0)
    }

    fun becomeFirstResponder() {
        this.getEditText().requestFocus()
        this.getSearchView().requestFocus()
        UIKeyInput.getInputMethodManager()
            ?.showSoftInput(this.getEditText(), InputMethodManager.SHOW_IMPLICIT)
    }

}