package com.starkbank.cocoatouch.sample.tableview
import com.starkbank.cocoatouch.sample.R
import com.starkbank.cocoatouch.annotation.IBOutlet
import com.starkbank.cocoatouch.uikit.UILabel
import com.starkbank.cocoatouch.uikit.UITableViewCell


class TableViewCell: UITableViewCell() {

    @IBOutlet(R.id.tableviewcellLabel) lateinit var nameLabel: UILabel

    fun setName(name: String) {
        this.nameLabel.text = name
    }
}