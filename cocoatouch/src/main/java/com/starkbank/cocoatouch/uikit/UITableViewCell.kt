package com.starkbank.cocoatouch.uikit


open class UITableViewCell: UIView() {

    override var backgroundColor: UIColor? = null
        set(value) {
            field = value
            value?.let { widget.setBackgroundColor(it.colorInt) }
        }

    var isSelected: Boolean = false
    set(value) {
        field = value
        setSelected(value, true)
    }

    open fun setSelected(selected: Boolean, animated: Boolean) {}

}