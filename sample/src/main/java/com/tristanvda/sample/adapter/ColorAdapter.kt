package com.tristanvda.sample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tristanvda.combinewrapperlistadapter.adapter.CombineWrapperListAdapter
import com.tristanvda.sample.R

class ColorAdapter : ListAdapter<Int, RecyclerView.ViewHolder>(diffCallback),
    CombineWrapperListAdapter.WrappedListAdapter<Int> {

    private var items: List<Int> = emptyList()

    fun setItems(colors: List<Int>) {
        this.items = colors
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorAdapter.ColorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_color, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindListItemViewHolder(holder, items[position], emptyList())
    }

    override fun bindListItemViewHolder(holder: RecyclerView.ViewHolder, item: Int, payloads: List<Any>) {
        (holder as ColorViewHolder).bind(item)
    }

    override fun getItemViewType(item: Int): Int = VIEW_TYPE_COLOR

    override fun getListItems(): List<Int> = items

    override fun getDiffCallback(): DiffUtil.ItemCallback<Int> = diffCallback

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.color_view)

        fun bind(color: Int) {
            colorView.setBackgroundColor(color)
        }
    }

    companion object {

        private const val VIEW_TYPE_COLOR: Int = 2

        private val diffCallback = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean = true
        }
    }
}
