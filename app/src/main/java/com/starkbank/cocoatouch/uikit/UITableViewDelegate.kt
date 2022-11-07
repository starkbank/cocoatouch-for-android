package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.foundation.NSIndexPath


interface UITableViewDelegate {
    fun tableViewDidSelectRowAtIndexPath(tableView: UITableView, indexPath: NSIndexPath)
}
