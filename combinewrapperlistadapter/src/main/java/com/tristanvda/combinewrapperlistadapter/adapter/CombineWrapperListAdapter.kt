package com.tristanvda.combinewrapperlistadapter.adapter

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalStateException

class CombineWrapperListAdapter :
    ListAdapter<CombineWrapperListAdapter.WrapperAdapterItem, RecyclerView.ViewHolder>(diffCallback) {

    private var adapters: List<RecyclerView.Adapter<RecyclerView.ViewHolder>> = emptyList()
    private val viewTypes: SparseArrayCompat<WrappedListAdapter<Any>> = SparseArrayCompat()
    private val dataObservers: MutableMap<RecyclerView.Adapter<RecyclerView.ViewHolder>, RecyclerView.AdapterDataObserver> =
        mutableMapOf()

    data class WrapperAdapterItem(val adapter: WrappedListAdapter<Any>, val item: Any)

    /**
     * Implement this in an adapter to make it support the CombineWrapperListAdapter
     **/
    interface WrappedListAdapter<T> {

        fun bindListItemViewHolder(holder: RecyclerView.ViewHolder, item: T, payloads: List<Any> = emptyList())

        fun getItemViewType(item: T): Int

        fun getListItems(): List<T>

        fun getDiffCallback(): DiffUtil.ItemCallback<T>
    }

    fun add(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        addAdapter(adapter)
    }

    fun remove(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        adapters = adapters.filter { it != adapter }
        dataObservers[adapter]?.let { adapter.unregisterAdapterDataObserver(it) }
        dataObservers.remove(adapter)
    }

    fun getAdapters(): List<RecyclerView.Adapter<RecyclerView.ViewHolder>> = adapters

    fun getItemForPosition(position: Int): Any? {
        val wrappedItem = getItem(position)
        return if (wrappedItem is WrapperAdapterItem) return wrappedItem.item else null
    }

    fun removeAllAdapters() {
        viewTypes.clear()
        adapters.forEach { adapter -> dataObservers[adapter]?.let { adapter.unregisterAdapterDataObserver(it) } }
        dataObservers.clear()
        adapters = emptyList()
    }

    private fun addAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        val wrapperDataObserver = WrapperDataObserver(adapter)
        adapter.registerAdapterDataObserver(wrapperDataObserver)
        dataObservers[adapter] = wrapperDataObserver

        adapters = adapters + listOf(adapter)
    }

    @Suppress("UNCHECKED_CAST")
    fun notifyAdaptersChanged() {
        val adapterList = ArrayList<WrapperAdapterItem>()

        adapters.forEach { adapter ->
            try {
                adapter as WrappedListAdapter<Any>
                val items = adapter.getListItems()
                adapterList.addAll(items.map { WrapperAdapterItem(adapter, it) })
            } catch (e: ClassCastException) {
                throw ClassCastException("$adapter must implement WrappedListAdapter")
            }
        }

        submitList(adapterList)
    }

    override fun getItemViewType(position: Int): Int {
        val wrappedItem: WrapperAdapterItem = getItem(position)

        val viewType = wrappedItem.adapter.getItemViewType(wrappedItem.item)

        if (viewTypes.containsKey(viewType)) {
            val existingAdapter = viewTypes[viewType]
            if (adapters.find { it == existingAdapter } != null && existingAdapter != wrappedItem.adapter) {
                throw IllegalStateException("Duplicate viewTypes are not allowed ($viewType)")
            }
        }
        viewTypes.put(viewType, wrappedItem.adapter)
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapter = adapters.single { viewTypes.get(viewType) == it }
        return adapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item.adapter.bindListItemViewHolder(holder, item.item)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        for (adapter in adapters) {
            adapter.onAttachedToRecyclerView(recyclerView)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        for (adapter in adapters) {
            adapter.onDetachedFromRecyclerView(recyclerView)
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)

        for (adapter in adapters) {
            adapter.setHasStableIds(hasStableIds)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.single { wrappedAdapter == it }
        adapter.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.single { wrappedAdapter == it }
        adapter.onViewDetachedFromWindow(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.single { wrappedAdapter == it }
        adapter.onViewAttachedToWindow(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.single { wrappedAdapter == it }
        return adapter.onFailedToRecycleView(holder)
    }

    inner class WrapperDataObserver(private val adapter: RecyclerView.Adapter<*>) : RecyclerView.AdapterDataObserver() {
        private val wrapperAdapter = this@CombineWrapperListAdapter

        override fun onChanged() {
            notifyAdaptersChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onAdapterPosition { start -> wrapperAdapter.notifyItemRangeRemoved(start + positionStart, itemCount) }
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onAdapterPosition { start -> wrapperAdapter.notifyItemMoved(start + fromPosition, start + toPosition) }
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onAdapterPosition { start -> wrapperAdapter.notifyItemRangeInserted(start + positionStart, itemCount) }
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onAdapterPosition { start -> wrapperAdapter.notifyItemRangeChanged(start + positionStart, itemCount) }
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            onAdapterPosition { start -> wrapperAdapter.notifyItemRangeChanged(start + positionStart, itemCount, payload) }
        }

        private inline fun onAdapterPosition(task: (Int) -> Unit) {
            for (i in 0..wrapperAdapter.itemCount) {
                if (wrapperAdapter.getItem(i).adapter == adapter) {
                    task(i)
                }
            }
        }
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<WrapperAdapterItem>() {

            override fun areItemsTheSame(oldItem: WrapperAdapterItem, newItem: WrapperAdapterItem): Boolean =
                oldItem.adapter == newItem.adapter && newItem.adapter.getDiffCallback().areItemsTheSame(oldItem.item, newItem.item)

            override fun areContentsTheSame(oldItem: WrapperAdapterItem, newItem: WrapperAdapterItem): Boolean =
                oldItem.adapter == newItem.adapter && newItem.adapter.getDiffCallback().areContentsTheSame(oldItem.item, newItem.item)

            override fun getChangePayload(oldItem: WrapperAdapterItem, newItem: WrapperAdapterItem): Any? {
                return if (oldItem.adapter == newItem.adapter) {
                    newItem.adapter.getDiffCallback().getChangePayload(oldItem.item, newItem.item)
                } else {
                    null
                }
            }
        }
    }

}
