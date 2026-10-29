package hu.unicon.szakdoga.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.unicon.szakdoga.R
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder

sealed class ExplorerItem {
    object Back : ExplorerItem()
    data class FolderItem(val folder: Folder) : ExplorerItem()
    data class DocumentItem(val document: Document) : ExplorerItem()
}

class ExplorerAdapter(
    private var items: List<ExplorerItem> = emptyList(),
    private val onItemClick: (ExplorerItem) -> Unit
) : RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder>() {

    fun updateItems(newItems: List<ExplorerItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_explorer, parent, false)
        return ExplorerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ExplorerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: TextView = itemView.findViewById(R.id.itemIcon)
        private val titleView: TextView = itemView.findViewById(R.id.itemTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.itemSubtitle)

        fun bind(item: ExplorerItem) {
            when (item) {
                is ExplorerItem.Back -> {
                    iconView.text = "⬅️"
                    titleView.text = "Vissza"
                    subtitleView.visibility = View.GONE
                }
                is ExplorerItem.FolderItem -> {
                    iconView.text = "📁"
                    titleView.text = item.folder.name
                    subtitleView.visibility = View.GONE
                }
                is ExplorerItem.DocumentItem -> {
                    iconView.text = "📄"
                    titleView.text = item.document.title
                    if (!item.document.fileName.isNullOrEmpty()) {
                        subtitleView.text = item.document.fileName
                        subtitleView.visibility = View.VISIBLE
                    } else {
                        subtitleView.visibility = View.GONE
                    }
                }
            }
        }
    }
}
