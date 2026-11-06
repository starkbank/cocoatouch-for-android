package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.foundation.NSIndexPath


interface UITableViewDelegate {
    fun tableViewDidSelectRowAtIndexPath(tableView: UITableView, indexPath: NSIndexPath) {}
    fun tableViewViewForHeaderInSection(tableView: UITableView, section: Int): UIView? { return null }
    fun tableViewViewForFooterInSection(tableView: UITableView, section: Int): UIView? { return null }
    fun tableViewWillDisplayForRowAt(tableView: UITableView, willDisplay: UITableViewCell, forRowAt: NSIndexPath){}
}
