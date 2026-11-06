package com.starkbank.cocoatouch.uikit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.starkbank.cocoatouch.R
import com.starkbank.cocoatouch.annotation.IBActionParser
import com.starkbank.cocoatouch.annotation.IBOutletParser
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi
import com.starkbank.cocoatouch.foundation.NSIndexPath

class UICollectionView : UIScrollView() {

    enum class ScrollDirection {
        horizontal,
        vertical
    }

    var dataSource: UICollectionViewDataSource? = null
    var delegate: UICollectionViewDelegate? = null
    var scrollDirection = ScrollDirection.horizontal
        set(value) {
            field = value
            createLayoutManager()
            reloadData()
        }
    var isScrollEnabled = true
        set(value) {
            field = value
            recyclerView().isNestedScrollingEnabled = value
        }

    private var adapter: Adapter<UIViewHolder>? = null
    private var layoutManager: LayoutManager? = null
    private var indexPathArray: ArrayList<NSIndexPath> = arrayListOf()

    fun scrollToItem(at: Int, animated: Boolean) {
        if(animated)
            recyclerView().smoothScrollToPosition(at)
        else
            recyclerView().scrollToPosition(at)
    }

    fun reloadData() {
        indexPathArray = indexPathArray()

        if (layoutManager == null) {
            createLayoutManager()
        }

        if (adapter == null) {
            return createAdapter()
        }

        recyclerView().adapter?.notifyDataSetChanged()
    }

    fun dequeueReusableCellWithIdentifierForIndexPath(
        id: Int,
        indexPath: NSIndexPath
    ): UICollectionViewCell {
        val storyboard = UIApplication.sharedApplication().storyboard()
        val cell = storyboard.collectionViewCellForIdentifier(id)
        val context = UIApplication.sharedApplication().context()
        val inflater = LayoutInflater.from(context)
        cell.widget = inflater.inflate(id, this.recyclerView(), false)
        IBOutletParser.parse(cell)
        IBActionParser.parse(cell)
        return cell
    }

    //
    // Private Instance Methods
    //
    private fun recyclerView(): RecyclerView {
        return this.widget as RecyclerView
    }

    private fun indexPathArray(): ArrayList<NSIndexPath> {
        val indexPathArr = arrayListOf<NSIndexPath>()
        for (section in 0 until (dataSource?.numberOfSections(this) ?: 0)) {
            for (row in 0 until (dataSource?.collectionViewNumberOfItemsInSection(this, section)
                ?: 0)) {
                indexPathArr.add(NSIndexPath(section, row))
            }
        }
        return indexPathArr
    }

    private fun getIndexPathForPosition(position: Int): NSIndexPath {
        return indexPathArray.getOrNull(position) ?: NSIndexPath(0, 0)
    }

    private fun createAdapter() {
        val collectionView = this
        adapter = object : Adapter<UIViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UIViewHolder {
                val layout = if(scrollDirection == ScrollDirection.vertical) R.layout.cocoatouch_view_holder_vertical else R.layout.cocoatouch_view_holder_horizontal
                val view = LayoutInflater.from(parent.context)
                    .inflate(layout, parent, false)
                return UIViewHolder(view)
            }

            override fun onBindViewHolder(holder: UIViewHolder, position: Int) {
                holder.container.removeAllViews()
                holder.container.requestLayout()
                val indexPath = getIndexPathForPosition(position)
                val widget =
                    dataSource?.collectionViewCellForItemAt(collectionView, indexPath)?.widget
                holder.container.setOnClickListener {
                    delegate?.collectionViewDidSelectItemAt(collectionView, indexPath)
                }
                val size = delegate?.collectionViewLayoutSizeForItem(collectionView, indexPath)
                if(size != null && size.width != 0 && size.height != 0) {
                    widget?.layoutParams?.width = size.width
                    widget?.layoutParams?.height = size.height
                }
                holder.container.addView(widget)
            }

            override fun getItemCount(): Int {
                return indexPathArray.size
            }
        }
        recyclerView().adapter = adapter
    }

    private fun createLayoutManager() {
        val context = UIApplication.sharedApplication().context()
        layoutManager = object : LinearLayoutManager(context, scrollDirection.ordinal, false){
            override fun canScrollHorizontally(): Boolean {
                return isScrollEnabled
            }

            override fun canScrollVertically(): Boolean {
                return isScrollEnabled
            }
        }
        recyclerView().layoutManager = layoutManager
    }

}

@InternalCocoaTouchApi
class UIViewHolder(itemView: View) :
    ViewHolder(itemView) {
    companion object {
        const val CELL = 0
        const val HEADER = -1
        const val FOOTER = -2
        const val TITLE_HEADER = -3
        const val TITLE_FOOTER = -4
        const val GLOBAL_HEADER = -5
        const val GLOBAL_FOOTER = -6
    }
    val container: LinearLayout = itemView.findViewById(R.id.cocoatouch_view_holder_container)
    var tableViewCell: UITableViewCell? = null
}
