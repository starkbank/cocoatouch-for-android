package com.starkbank.android
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.annotation.IBOutlet
import com.starkbank.cocoatouch.compability.DefaultActions
import com.starkbank.cocoatouch.foundation.NSIndexPath
import com.starkbank.cocoatouch.foundation.NSMutableArray
import com.starkbank.cocoatouch.uikit.*


class TableViewController: UIViewController(), UITableViewDataSource {

    @IBOutlet(R.id.tableview) lateinit var tableView: UITableView
    private var tableData: NSMutableArray<String> = NSMutableArray()


    override fun viewDidLoad() {
        super.viewDidLoad()
        tableData.addObject("Daenerys Targaryen")
        tableData.addObject("Jon Snow")
        tableData.addObject("Arya Stark")
        tableData.addObject("Tyrion Lannister")
        tableData.addObject("Cersei Lannister")
        tableView.setDataSource(this)
    }

    //
    // Actions
    //
    @IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {
        this.navigationController?.popViewController(true)
    }

    //
    // UITableViewDataSource
    //
    override fun tableViewNumberOfRowsInSection(tableView: UITableView, section: Int): Int {
        return this.tableData.count()
    }

    override fun tableViewCellForRowAtIndexPath(tableView: UITableView, indexPath: NSIndexPath): UITableViewCell {
        val cell = tableView.dequeueReusableCellWithIdentifierForIndexPath(R.layout.tableviewcell, indexPath) as TableViewCell
        cell.setName(this.tableData.objectAtIndex(indexPath.row))
        return cell
    }
}