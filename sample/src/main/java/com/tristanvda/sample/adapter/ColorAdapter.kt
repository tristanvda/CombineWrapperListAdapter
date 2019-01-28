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

class ColorAdapter : ListAdapter<Int, ColorAdapter.ColorViewHolder>(diffCallback),
    CombineWrapperListAdapter.WrappedListAdapter<Int, ColorAdapter.ColorViewHolder> {

    private var colors: List<Int> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorAdapter.ColorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_color, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ColorAdapter.ColorViewHolder, position: Int) {
        bindListItemViewHolder(holder, colors[position], emptyList())
    }

    override fun bindListItemViewHolder(holder: ColorViewHolder, item: Int, payloads: List<Any>) {
        holder.bind(item)
    }

    override fun getItemViewType(item: Int): Int = VIEW_TYPE_COLOR

    override fun getListItems(): List<Int> = colors

    override fun getDiffCallback(): DiffUtil.ItemCallback<Int> = diffCallback

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: TextView = itemView.findViewById(R.id.color_view)

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