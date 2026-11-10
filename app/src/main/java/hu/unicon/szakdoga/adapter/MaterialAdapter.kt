package hu.unicon.szakdoga.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.unicon.szakdoga.R
import hu.unicon.szakdoga.model.Material

class MaterialAdapter(
    private var items: List<Material> = emptyList()
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    fun updateItems(newItems: List<Material>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_material, parent, false)
        return MaterialViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.materialName)
        private val skuTextView: TextView = itemView.findViewById(R.id.materialSku)
        private val categoryTextView: TextView = itemView.findViewById(R.id.materialCategory)
        private val detailsTextView: TextView = itemView.findViewById(R.id.materialDetails)

        fun bind(material: Material) {
            nameTextView.text = material.name

            if (!material.skuCode.isNullOrEmpty()) {
                skuTextView.text = "SKU: ${material.skuCode}"
                skuTextView.visibility = View.VISIBLE
            } else {
                skuTextView.visibility = View.GONE
            }

            if (!material.categoryName.isNullOrEmpty()) {
                categoryTextView.text = "Kategória: ${material.categoryName}"
                categoryTextView.visibility = View.VISIBLE
            } else {
                categoryTextView.visibility = View.GONE
            }

            if (!material.details.isNullOrEmpty()) {
                detailsTextView.text = material.details
                detailsTextView.visibility = View.VISIBLE
            } else {
                detailsTextView.visibility = View.GONE
            }
        }
    }
}
