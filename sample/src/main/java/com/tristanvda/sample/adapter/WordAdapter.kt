package com.tristanvda.sample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tristanvda.combinewrapperlistadapter.adapter.CombineWrapperListAdapter
import com.tristanvda.sample.R

class WordAdapter
    : RecyclerView.Adapter<WordAdapter.WordViewHolder>(),
    CombineWrapperListAdapter.WrappedListAdapter<String, WordAdapter.WordViewHolder> {

    private var list: List<String> = emptyList()

    fun setItems(words: List<String>) {
        list = words
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_word, parent, false))
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        bindListItemViewHolder(holder, list[position], emptyList())
    }

    override fun bindListItemViewHolder(holder: WordAdapter.WordViewHolder, item: String, payloads: List<Any>) {
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = getItemViewType(list[position])

    override fun getItemViewType(item: String): Int = VIEW_TYPE_WORD

    override fun getListItems(): List<String> = list

    override fun getDiffCallback(): DiffUtil.ItemCallback<String> = diffCallback

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordText: TextView = itemView.findViewById(R.id.word_text)

        fun bind(word: String) {
            wordText.text = word
        }
    }

    companion object {

        private const val VIEW_TYPE_WORD: Int = 1

        private val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = true
        }
    }
}