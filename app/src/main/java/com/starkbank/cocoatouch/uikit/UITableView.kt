package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.annotation.IBOutletParser
import com.starkbank.cocoatouch.foundation.NSIndexPath
import android.widget.AdapterView.OnItemClickListener
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ListView
import android.view.ViewGroup
import android.view.View
import com.starkbank.cocoatouch.annotation.IBActionParser


class UITableView: UIScrollView() {

    private var dataSource: UITableViewDataSource? = null
    private var delegate: UITableViewDelegate? = null

    fun setDataSource(dataSource: UITableViewDataSource?) {
        this.dataSource = dataSource
        reloadData()
    }

    fun setDelegate(delegate: UITableViewDelegate?) {
        this.delegate = delegate
        addTouchItems()
    }

    fun reloadData() {
        val delegate = this.dataSource
        val tableView = this
        val numberOfElements = if (this.dataSource == null) 0 else dataSource!!.tableViewNumberOfRowsInSection(this, 0)
        val array = arrayOfNulls<String>(numberOfElements)
        val context = UIApplication.sharedApplication().context()
        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(context, 0, array) {
            override fun getView(index: Int, convertView: View?, parent: ViewGroup): View {
                val widget = delegate!!.tableViewCellForRowAtIndexPath(tableView, NSIndexPath(0, index)).widget
                return widget as View
            }
        }
        this.listView().adapter = adapter
    }

    fun dequeueReusableCellWithIdentifierForIndexPath(id: Int, indexPath: NSIndexPath): UITableViewCell {
        val storyboard = UIApplication.sharedApplication().storyboard()
        val cell = storyboard.viewCellForIdentifier(id)
        val context = UIApplication.sharedApplication().context()
        val inflater = LayoutInflater.from(context)
        cell.widget = inflater.inflate(id, this.listView(), false)
        IBOutletParser.parse(cell)
        return cell
    }

    //
    // Private Instance Methods
    //
    private fun addTouchItems() {
        val tableView = this
        this.listView().onItemClickListener = OnItemClickListener { parent, view, index, arg3 ->
            view.isSelected = true
            delegate?.tableViewDidSelectRowAtIndexPath(tableView, NSIndexPath(0, index))
        }
    }

    private fun listView(): ListView {
        return this.widget as ListView
    }
}