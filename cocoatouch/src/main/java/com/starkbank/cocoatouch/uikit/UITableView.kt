package com.starkbank.cocoatouch.uikit

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.starkbank.cocoatouch.R
import com.starkbank.cocoatouch.annotation.IBActionParser
import com.starkbank.cocoatouch.annotation.IBOutletParser
import com.starkbank.cocoatouch.foundation.NSIndexPath


class UITableView : UIScrollView() {

    override var backgroundColor: UIColor? = null
        set(value) {
            field = value
            value?.let { recyclerView().setBackgroundColor(it.colorInt) }
            reloadData()
        }

    var separatorColor: UIColor? = null
    var separatorInset = UIEdgeInsetsMake()

    var tableHeaderView: UIView? = null
        set(value) {
            field = value
            reloadData()
        }

    var tableFooterView: UIView? = null
        set(value) {
            field = value
            reloadData()
        }

    val numberOfSections: Int
        get() {
            return dataSource?.tableViewNumberOfSections(this@UITableView) ?: 0
        }

    var backgroundView: UIView? = null

    internal var dataSource: UITableViewDataSource? = null
    internal var delegate: UITableViewDelegate? = null

    private var adapter: RecyclerView.Adapter<UIViewHolder>? = null
    private var layoutManager: LinearLayoutManager? = null

    private var lastIndexPathDisplayed: NSIndexPath = NSIndexPath(-99, -99)
    internal var indexPathArray: ArrayList<NSIndexPath> = arrayListOf()

    fun setDataSource(dataSource: UITableViewDataSource?) {
        this.dataSource = dataSource
        reloadData()
    }

    fun setDelegate(delegate: UITableViewDelegate?) {
        this.delegate = delegate
        reloadData()
    }

    fun scrollToRow(at: NSIndexPath, animated: Boolean) {
        val position = getPositionForIndexPath(at)
        if (position >= 0) {
            if (animated)
                recyclerView().smoothScrollToPosition(position)
            else
                recyclerView().scrollToPosition(position)
        }
    }

    fun reloadData() {
        indexPathArray = indexPathArray()

        if (layoutManager == null) {
            createLayoutManager()
        }

        if (adapter == null) {
            createAdapter()
        }

        recyclerView().removeItemDecorations()

        separatorColor?.let {
            val dividerItemDecoration = DividerItemDecoration(
                recyclerView().context,
                layoutManager?.orientation ?: RecyclerView.VERTICAL
            )
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setSize(1, 1.dpToPx)
            shape.setColor(it.colorInt)
            val insetDrawable = InsetDrawable(shape, separatorInset.left.toInt().dpToPx, separatorInset.top.toInt().dpToPx, separatorInset.right.toInt().dpToPx, separatorInset.bottom.toInt().dpToPx)
            dividerItemDecoration.setDrawable(insetDrawable)
            recyclerView().addItemDecoration(dividerItemDecoration)
        }

        recyclerView().clearOnScrollListeners()
        recyclerView().addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastPosition = layoutManager?.findLastCompletelyVisibleItemPosition()
                lastPosition?.let {
                    if (lastPosition >= indexPathArray.size || lastPosition < 0) {
                        return
                    }
                    val lastIndexPath = indexPathArray[lastPosition]
                    if (lastIndexPath.row < 0) {
                        return
                    }
                    val viewHolder = recyclerView().findViewHolderForAdapterPosition(lastPosition) as? UIViewHolder

                    if (lastIndexPathDisplayed.row != lastIndexPath.row || lastIndexPathDisplayed.section != lastIndexPath.section) {
                        lastIndexPathDisplayed = lastIndexPath
                        delegate?.tableViewWillDisplayForRowAt(
                            this@UITableView,
                            viewHolder?.tableViewCell ?: UITableViewCell(),
                            lastIndexPath
                        )
                    }
                }
            }
        })

        adapter?.notifyDataSetChanged()
    }

    fun reloadSections(sections: Int) {
        val startPosition = this.getPositionForIndexPath(NSIndexPath(section = sections, 0))
        val endPosition = startPosition + numberOfRows(sections)
        adapter?.notifyItemRangeChanged(startPosition, endPosition)
    }

    fun reloadRows(at: NSIndexPath) {
        val position = this.getPositionForIndexPath(at)
        adapter?.notifyItemChanged(position)
    }

    fun dequeueReusableCellWithIdentifierForIndexPath(
        id: Int,
        indexPath: NSIndexPath
    ): UITableViewCell {
        val storyboard = UIApplication.sharedApplication().storyboard()
        val cell = storyboard.viewCellForIdentifier(id)
        val context = UIApplication.sharedApplication().context()
        val inflater = LayoutInflater.from(context)
        cell.widget = inflater.inflate(id, recyclerView(), false)
        IBOutletParser.parse(cell)
        IBActionParser.parse(cell)
        return cell
    }

    fun numberOfRows(inSection: Int): Int {
        return dataSource?.tableViewNumberOfRowsInSection(this@UITableView, inSection) ?: 0
    }

    //
    // Private Instance Methods
    //
    internal fun indexPathArray(): ArrayList<NSIndexPath> {
        val indexPathArr = arrayListOf<NSIndexPath>()

        tableHeaderView?.let {
            indexPathArr.add(NSIndexPath(0, UIViewHolder.GLOBAL_HEADER))
        }

        for (section in 0 until (dataSource?.tableViewNumberOfSections(this) ?: 0)) {
            val rows = (dataSource?.tableViewNumberOfRowsInSection(this, section) ?: 0)
            if (delegate?.tableViewViewForHeaderInSection(this, section) != null) {
                indexPathArr.add(NSIndexPath(section, UIViewHolder.HEADER))
            } else {
                if (dataSource?.tableViewTitleForHeaderInSection(this, section) != null) {
                    indexPathArr.add(NSIndexPath(section, UIViewHolder.TITLE_HEADER))
                }
            }

            for (row in 0 until rows) {
                indexPathArr.add(NSIndexPath(section, row))
            }

            if (delegate?.tableViewViewForFooterInSection(this, section) != null) {
                indexPathArr.add(NSIndexPath(section, UIViewHolder.FOOTER))
            } else {
                if (dataSource?.tableViewTitleForFooterInSection(this, section) != null) {
                    indexPathArr.add(NSIndexPath(section, UIViewHolder.TITLE_FOOTER))
                }
            }
        }

        tableFooterView?.let {
            indexPathArr.add(NSIndexPath(0, UIViewHolder.GLOBAL_FOOTER))
        }

        return indexPathArr
    }

    internal fun getIndexPathForPosition(position: Int): NSIndexPath {
        return indexPathArray.getOrNull(position) ?: NSIndexPath(0, 0)
    }

    internal fun getPositionForIndexPath(indexPath: NSIndexPath): Int {
        return indexPathArray.indexOfFirst { it.section == indexPath.section && it.row == indexPath.row }
    }

    private fun createAdapter() {
        val tableView = this
        adapter = object : RecyclerView.Adapter<UIViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): UIViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.cocoatouch_view_holder_vertical, parent, false)
                return UIViewHolder(view)
            }

            override fun onBindViewHolder(holder: UIViewHolder, position: Int) {
                cleanViewHolder(holder)
                val viewType = getItemViewType(position)
                val indexPath = getIndexPathForPosition(position)
                bindTableViewCell(tableView, indexPath, holder, viewType)
            }

            override fun getItemCount(): Int {
                return indexPathArray.size
            }

            override fun getItemViewType(position: Int): Int {
                val indexPath = indexPathArray[position]
                return if (indexPath.row < 0) indexPath.row else UIViewHolder.CELL
            }
        }
        recyclerView().adapter = adapter
    }

    internal fun cleanViewHolder(view: UIViewHolder) {
        view.container.removeAllViews()
        view.container.requestLayout()
        view.container.setOnClickListener { }
        backgroundColor?.let { view.container.setBackgroundColor(it.colorInt) }
    }

    private fun createTitleCell(text: String, holder: UIViewHolder) {
        val context = UIApplication.sharedApplication().context()
        val textView = TextView(holder.container.context)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.marginStart = 16.dpToPx
        params.marginEnd = 16.dpToPx
        textView.text = text
        textView.layoutParams = params
        holder.container.addView(textView)
        backgroundColor?.let { holder.container.setBackgroundColor(it.colorInt) }
    }

    internal fun bindTableViewCell(
        tableView: UITableView,
        indexPath: NSIndexPath,
        holder: UIViewHolder,
        viewType: Int
    ) {
        when (viewType) {
            UIViewHolder.TITLE_HEADER -> {
                dataSource?.tableViewTitleForHeaderInSection(tableView, indexPath.section)
                    ?.let { text ->
                        createTitleCell(text, holder)
                    }
            }
            UIViewHolder.TITLE_FOOTER -> {
                dataSource?.tableViewTitleForFooterInSection(tableView, indexPath.section)
                    ?.let { text ->
                        createTitleCell(text, holder)
                    }
            }
            UIViewHolder.HEADER -> {
                val widget = delegate?.tableViewViewForHeaderInSection(
                    tableView,
                    indexPath.section
                )?.widget
                widget?.removeFromParent()
                holder.container.addView(widget)
            }
            UIViewHolder.FOOTER -> {
                val widget = delegate?.tableViewViewForFooterInSection(
                    tableView,
                    indexPath.section
                )?.widget
                widget?.removeFromParent()
                holder.container.addView(widget)
            }
            UIViewHolder.GLOBAL_HEADER -> {
                val widget = tableHeaderView?.widget
                widget?.removeFromParent()
                holder.container.addView(widget)
            }
            UIViewHolder.GLOBAL_FOOTER -> {
                val widget = tableFooterView?.widget
                widget?.removeFromParent()
                holder.container.addView(widget)
            }
            UIViewHolder.CELL -> {
                val cell = dataSource?.tableViewCellForRowAtIndexPath(tableView, indexPath)
                val widget = cell?.widget
                holder.container.setOnClickListener {
                    delegate?.tableViewDidSelectRowAtIndexPath(tableView, indexPath)
                }
                holder.container.addView(widget)
                holder.tableViewCell = cell
            }
        }
    }

    private fun createLayoutManager() {
        val context = UIApplication.sharedApplication().context()
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView().layoutManager = layoutManager
    }

    internal fun recyclerView(): RecyclerView {
        return widget as RecyclerView
    }

    private fun RecyclerView.removeItemDecorations(){
        while (this.itemDecorationCount > 0) {
            this.removeItemDecorationAt(0)
        }
    }
}