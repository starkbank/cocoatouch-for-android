package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.foundation.NSIndexPath


interface UITableViewDataSource {
    fun tableViewNumberOfSections(tableView: UITableView): Int { return 1 }
    fun tableViewNumberOfRowsInSection(tableView: UITableView, section: Int): Int
    fun tableViewCellForRowAtIndexPath(tableView: UITableView, indexPath: NSIndexPath): UITableViewCell
    fun tableViewTitleForHeaderInSection(tableView: UITableView, section: Int): String? { return null }
    fun tableViewTitleForFooterInSection(tableView: UITableView, section: Int): String? { return null }
}