package com.starkbank.cocoatouch.uikit


interface UIAppearance {

    fun onActionColor(traitCollection: UITraitCollection): UIColor = UIColor("#FFFFFF")

    fun borderColor(traitCollection: UITraitCollection): UIColor = UIColor("#CCCCCC")

    fun secondaryBackgroundColor(traitCollection: UITraitCollection): UIColor = UIColor("#EFEFF0")

    fun sectionHeaderFont(traitCollection: UITraitCollection): UIFont? = null
}


object UIDefaultAppearance : UIAppearance
