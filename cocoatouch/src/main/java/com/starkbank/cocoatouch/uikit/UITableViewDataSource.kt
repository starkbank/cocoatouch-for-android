package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.foundation.NSIndexPath


interface UITableViewDataSource {
    fun tableViewNumberOfRowsInSection(tableView: UITableView, section: Int): Int
    fun tableViewCellForRowAtIndexPath(tableView: UITableView, indexPath: NSIndexPath): UITableViewCell
}