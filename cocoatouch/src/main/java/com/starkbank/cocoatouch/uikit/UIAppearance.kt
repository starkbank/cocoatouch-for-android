package com.starkbank.cocoatouch.uikit


interface UIAppearance {

    fun onActionColor(traitCollection: UITraitCollection): UIColor

    fun borderColor(traitCollection: UITraitCollection): UIColor

    fun secondaryBackgroundColor(traitCollection: UITraitCollection): UIColor

    fun sectionHeaderFont(traitCollection: UITraitCollection): UIFont?
}


object UIDefaultAppearance : UIAppearance {

    override fun onActionColor(traitCollection: UITraitCollection): UIColor {
        return UIColor("#FFFFFF")
    }

    override fun borderColor(traitCollection: UITraitCollection): UIColor {
        return UIColor("#CCCCCC")
    }

    override fun secondaryBackgroundColor(traitCollection: UITraitCollection): UIColor {
        return UIColor("#EFEFF0")
    }

    override fun sectionHeaderFont(traitCollection: UITraitCollection): UIFont? {
        return null
    }
}
