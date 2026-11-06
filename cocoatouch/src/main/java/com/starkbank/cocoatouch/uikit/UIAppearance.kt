package com.starkbank.cocoatouch.uikit


interface UIAppearance {

    fun onActionColor(traitCollection: UITraitCollection): UIColor = UIDefaultAppearance.onActionColor(traitCollection)

    fun borderColor(traitCollection: UITraitCollection): UIColor = UIDefaultAppearance.borderColor(traitCollection)

    fun secondaryBackgroundColor(traitCollection: UITraitCollection): UIColor =
        UIDefaultAppearance.secondaryBackgroundColor(traitCollection)

    fun sectionHeaderFont(traitCollection: UITraitCollection): UIFont? =
        UIDefaultAppearance.sectionHeaderFont(traitCollection)
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
