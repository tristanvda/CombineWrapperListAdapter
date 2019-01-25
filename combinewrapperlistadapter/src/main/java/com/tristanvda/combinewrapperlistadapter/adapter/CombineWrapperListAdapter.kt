package com.tristanvda.combinewrapperlistadapter.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.frakbot.jumpingbeans.JumpingBeans
import java.lang.IllegalStateException

class CombineWrapperListAdapter(private val themeManager: ThemeManager)
    : ListAdapter<CombineWrapperListAdapter.WrapperAdapterItem, RecyclerView.ViewHolder>(diffCallback),
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    private var adapters: List<Pair<String?, RecyclerView.Adapter<RecyclerView.ViewHolder>>> = emptyList()
    private val viewTypes: SparseArrayCompat<WrappedListAdapter<Any>> = SparseArrayCompat()

    var hideTitlesWhenEmpty = false
    var isPending = false

    sealed class WrapperAdapterItem {

        data class WrappedItem(val adapter: WrappedListAdapter<Any>, val item: Any) : WrapperAdapterItem()

        data class TitleItem(val title: String) : WrapperAdapterItem()

        class PendingItem : WrapperAdapterItem()
    }

    /**
     * Implement this in an adapter to make it support the CombineWrapperListAdapter
     **/
    interface WrappedListAdapter<T> {

        fun bindListItemViewHolder(holder: RecyclerView.ViewHolder, item: T, payloads: List<Any> = emptyList())

        fun getItemViewType(item: T): Int

        fun getListItems(): List<T>

        fun getDiffCallback(): DiffUtil.ItemCallback<T>

    }

    interface ItemTouchWrapperListHelperAdapter<T> : WrappedListAdapter<T> {

        fun onItemMove(fromItem: Any, toItem: Any)

    }

    fun add(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, title: String? = null) {
        addAdapter(adapter, title)
    }

    fun remove(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        adapters = adapters.filter { it.second != adapter }
    }

    fun getAdapters(): List<RecyclerView.Adapter<RecyclerView.ViewHolder>> = adapters.map { it.second }

    fun getItemForPosition(position: Int): Any? {
        val wrappedItem = getItem(position)
        return if (wrappedItem is WrapperAdapterItem.WrappedItem) return wrappedItem.item else null
    }

    fun removeAllAdapters() {
        viewTypes.clear()
        adapters = emptyList()
    }

    private fun addAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, title: String?) {
        adapters += listOf(Pair(title, adapter))
    }

    fun notifyAdaptersChanged(pending: Boolean = isPending) {
        val adapterList = ArrayList<WrapperAdapterItem>()
        isPending = pending

        adapters.forEach { pair ->
            val title = pair.first
            val adapter = pair.second

            try {
                adapter as WrappedListAdapter<Any>
                val items = adapter.getListItems()
                if (!(hideTitlesWhenEmpty && items.isEmpty()) && title != null && title.isNotBlank()) {
                    adapterList.add(WrapperAdapterItem.TitleItem(title))
                }
                adapterList.addAll(items.map { WrapperAdapterItem.WrappedItem(adapter, it) })
            } catch (e: ClassCastException) {
                throw ClassCastException(adapter.toString() + " must implement WrappedListAdapter")
            }
        }

        if (isPending) {
            adapterList.add(WrapperAdapterItem.PendingItem())
        }

        submitList(adapterList)
    }

    override fun getItemViewType(position: Int): Int {
        val wrappedItem: WrapperAdapterItem = getItem(position)

        return when (wrappedItem) {
            is WrapperAdapterItem.PendingItem -> VIEW_TYPE_PENDING
            is WrapperAdapterItem.TitleItem -> VIEW_TYPE_TITLE
            is WrapperAdapterItem.WrappedItem -> {
                val viewType = wrappedItem.adapter.getItemViewType(wrappedItem.item)
                if (viewType == VIEW_TYPE_PENDING || viewType == VIEW_TYPE_TITLE) {
                    throw IllegalStateException("ViewTypes $VIEW_TYPE_PENDING and $VIEW_TYPE_PENDING are not allowed")
                }
                if (viewTypes.containsKey(viewType)) {
                    val existingAdapter = viewTypes[viewType]
                    if (adapters.find { it.second == existingAdapter } != null && existingAdapter != wrappedItem.adapter) {
                        throw IllegalStateException("Duplicate viewTypes are not allowed ($viewType)")
                    }
                }
                viewTypes.put(viewType, wrappedItem.adapter)
                return viewType
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TITLE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                TitleViewHolder(layoutInflater.inflate(R.layout.list_item_sub_title, parent, false))
            }
            VIEW_TYPE_PENDING -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                LoadingViewHolder(layoutInflater.inflate(R.layout.list_item_progress_indicator, parent, false))
            }
            else -> {
                val adapter = adapters.single { viewTypes.get(viewType) == it.second }.second
                adapter.onCreateViewHolder(parent, viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when (item) {
            is WrapperAdapterItem.TitleItem -> (holder as TitleViewHolder).bindView(item.title, themeManager.currentAppTheme)
            is WrapperAdapterItem.WrappedItem -> item.adapter.bindListItemViewHolder(holder, item.item)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //recyclerView.addItemDecoration(stickyHeaderItemDecoration) //TODO: sticky headers

        for (pair in adapters) {
            pair.second.onAttachedToRecyclerView(recyclerView)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        //recyclerView.removeItemDecoration(stickyHeaderItemDecoration) //TODO: sticky headers

        for (pair in adapters) {
            pair.second.onDetachedFromRecyclerView(recyclerView)
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)

        for (pair in adapters) {
            pair.second.setHasStableIds(hasStableIds)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.find { wrappedAdapter == it.second }?.second
        if (adapter != null) {
            adapter.onViewRecycled(holder)
        } else {
            super.onViewRecycled(holder)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is LoadingViewHolder) {
            holder.jumpingBeans?.stopJumping()
            return
        }

        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.find { wrappedAdapter == it.second }?.second
        if (adapter != null) {
            adapter.onViewDetachedFromWindow(holder)
        } else {
            super.onViewDetachedFromWindow(holder)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is LoadingViewHolder) {
            holder.jumpingBeans = JumpingBeans.with(holder.progressDots)
                    .appendJumpingDots()
                    .setLoopDuration(Typing.JUMPING_BEANS_LOOP_DURATION)
                    .build()
            return
        }

        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.find { wrappedAdapter == it.second }?.second
        if (adapter != null) {
            adapter.onViewAttachedToWindow(holder)
        } else {
            super.onViewAttachedToWindow(holder)
        }
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        val wrappedAdapter = viewTypes.get(holder.itemViewType)
        val adapter = adapters.find { wrappedAdapter == it.second }?.second
        return adapter?.onFailedToRecycleView(holder) ?: super.onFailedToRecycleView(holder)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val wrapperItemFrom = getItem(fromPosition)
        val wrapperItemTo = getItem(toPosition)

        if (wrapperItemFrom is WrapperAdapterItem.WrappedItem
                && wrapperItemTo is WrapperAdapterItem.WrappedItem
                && wrapperItemFrom.adapter == wrapperItemTo.adapter
                && wrapperItemTo.adapter is ItemTouchWrapperListHelperAdapter) {

            wrapperItemTo.adapter.onItemMove(wrapperItemFrom.item, wrapperItemTo.item)
        }
    }

    private inner class TitleViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: TextView = itemView.findViewById(R.id.title)

        internal fun bindView(titleText: String, appTheme: AppTheme) {
            bindSubTitleView(itemView.context, titleText, appTheme, itemView, titleTextView)
        }
    }

    internal class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var progressDots: TextView = itemView.findViewById(R.id.progress_dots)
        var jumpingBeans: JumpingBeans? = null

        fun bind(themeManager: ThemeManager) {
            val color = Color.parseColor(themeManager.currentAppTheme.ui.controls.standardControlTint.primaryColor)
            progressDots.setTextColor(color)
        }
    }

    companion object {

        private const val VIEW_TYPE_TITLE = 1000
        private const val VIEW_TYPE_PENDING = 1001

        private val diffCallback = object : DiffUtil.ItemCallback<WrapperAdapterItem>() {

            override fun areItemsTheSame(oldItem: WrapperAdapterItem, newItem: WrapperAdapterItem): Boolean {
                return if (oldItem is WrapperAdapterItem.WrappedItem && newItem is WrapperAdapterItem.WrappedItem) {
                    oldItem.adapter == newItem.adapter && newItem.adapter.getDiffCallback().areItemsTheSame(oldItem.item, newItem.item)
                } else if (oldItem is WrapperAdapterItem.TitleItem && newItem is WrapperAdapterItem.TitleItem) {
                    oldItem.title == newItem.title
                } else oldItem is WrapperAdapterItem.PendingItem && newItem is WrapperAdapterItem.PendingItem
            }

            override fun areContentsTheSame(oldItem: WrapperAdapterItem, newItem: WrapperAdapterItem): Boolean {
                return if (oldItem is WrapperAdapterItem.WrappedItem && newItem is WrapperAdapterItem.WrappedItem) {
                    oldItem.adapter == newItem.adapter && newItem.adapter.getDiffCallback().areContentsTheSame(oldItem.item, newItem.item)
                } else oldItem::class == newItem::class
            }

            override fun getChangePayload(oldItem: WrapperAdapterItem, newItem: WrapperAdapterItem): Any? {
                return if (oldItem is WrapperAdapterItem.WrappedItem && newItem is WrapperAdapterItem.WrappedItem) {
                    if (oldItem.adapter == newItem.adapter) newItem.adapter.getDiffCallback().getChangePayload(oldItem.item, newItem.item) else null
                } else null
            }
        }

        private fun bindSubTitleView(
                context: Context,
                titleText: String,
                appTheme: AppTheme,
                view: View,
                titleTextView: TextView) {

            val lubalinBoldFontType = Typeface.createFromAsset(context.assets, "fonts/lubalin/Lubalin-Graph-Bold.ttf")
            titleTextView.typeface = lubalinBoldFontType
            titleTextView.setTextColor(Color.parseColor(appTheme.ui.textColors.titleTextColor))
            titleTextView.text = titleText
            view.setBackgroundColor(Color.parseColor(appTheme.ui.backgroundColors.secondaryBackgroundColor))
        }
    }

}
