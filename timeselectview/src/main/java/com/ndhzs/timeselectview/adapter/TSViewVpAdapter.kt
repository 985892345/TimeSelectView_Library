package com.ndhzs.timeselectview.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeselectview.bean.TSViewDayBean
import com.ndhzs.timeselectview.layout.VpLayout
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.utils.TSViewListeners

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/24
 * @description
 */
internal class TSViewVpAdapter(
        private val dayBeans: List<TSViewDayBean>,
        private val attrs: TSViewAttrs,
        private val listeners: TSViewListeners,
        private val viewPager2: ViewPager2,
        private val showNowTimeLinePosition: Int
) : RecyclerView.Adapter<TSViewVpAdapter.ViewHolder>() {


    /**
     * 设置内部ScrollView的滑动监听
     */
    fun setOnScrollListener(l: (scrollY: Int, itemPosition: Int) -> Unit) {
        listeners.mOnScrollListener = { scrollY, vpPosition ->
            mScrollY = scrollY
            l.invoke(scrollY, vpPosition)
        }
    }

    /**
     * 得到当前item的ScrollView的滑动量
     */
    fun getTimeLineScrollY(): Int {
        return mScrollY
    }

    fun notifyAllItemRefresh() {
        notifyDataSetChanged()
    }

    /**
     * 让某个位置的item刷新
     */
    fun notifyItemRefresh(position: Int, isBackToCurrentTime: Boolean) {
        notifyItemChanged(position, listOf(NOTIFY_ITEM_REFRESH, isBackToCurrentTime))
    }

    /**
     * 该方法用于在任务在外部被增加或删除时调用
     */
    fun notifyItemDataChanged(position: Int, isBackToCurrentTime: Boolean) {
        notifyItemChanged(position, listOf(NOTIFY_ITEM_DATA_CHANGED, isBackToCurrentTime))
    }

    /**
     * 使内部的ScrollView直接瞬移，调用的ScrollView的scrollTo方法
     */
    fun timeLineScrollTo(scrollY: Int) {
        notifyItemChanged(viewPager2.currentItem, listOf(NOTIFY_ITEM_SCROLL_TO, scrollY))
    }

    /**
     * 使内部的ScrollView较缓慢地滑动，并有回弹动画
     */
    fun timeLineSlowlyScrollTo(scrollY: Int) {
        notifyItemChanged(viewPager2.currentItem, listOf(NOTIFY_ITEM_SLOWLY_SCROLL_TO, scrollY))
    }

    /**
     * 手动调用当前页面的时间轴回到xml中设置的CurrentTime
     */
    fun backCurrentTime() {
        notifyItemChanged(viewPager2.currentItem, NOTIFY_ITEM_BACK_CURRENT_ITEM)
    }

    /**
     * 取消当前页面的时间轴自动回到xml中设置的CurrentTime的延时，延时是在每次手指离开时间轴就会开启
     */
    fun cancelAutoBackCurrent() {
        notifyItemChanged(viewPager2.currentItem, NOTIFY_ITEM_CANCEL_AUTO_BACK_CURRENT_ITEM)
    }

    companion object {

        /**
         * 用于在[getItemViewType]，返回哪个position显示时间线的
         */
        private const val SHOW_NOW_TIME_LINE_POSITION = 0

        /**
         * 用于在[getItemViewType]，返回哪些position不显示时间线的
         */
        private const val NOT_SHOW = 1

        /**
         * 用于[onBindViewHolder]中判断，此时说明是[notifyItemRefresh]调用的notifyItemChanged
         */
        private const val NOTIFY_ITEM_REFRESH = 0

        /**
         * 用于[onBindViewHolder]中判断，此时说明是[notifyItemDataChanged]调用的notifyItemChanged
         */
        private const val NOTIFY_ITEM_DATA_CHANGED = 1

        /**
         * 用于[onBindViewHolder]中判断，此时说明是[timeLineScrollTo]调用的notifyItemChanged
         */
        private const val NOTIFY_ITEM_SCROLL_TO = 2

        /**
         * 用于[onBindViewHolder]中判断，此时说明是[timeLineSlowlyScrollTo]调用的notifyItemChanged
         */
        private const val NOTIFY_ITEM_SLOWLY_SCROLL_TO = 3

        /**
         * 用于[onBindViewHolder]中判断，此时说明是[backCurrentTime]调用的notifyItemChanged
         */
        private const val NOTIFY_ITEM_BACK_CURRENT_ITEM = 4

        /**
         * 用于[onBindViewHolder]中判断，此时说明是[cancelAutoBackCurrent]调用的notifyItemChanged
         */
        private const val NOTIFY_ITEM_CANCEL_AUTO_BACK_CURRENT_ITEM = 5

    }

    private var mScrollY = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = VpLayout(parent.context, attrs, listeners, viewPager2, dayBeans[0].date, viewType == SHOW_NOW_TIME_LINE_POSITION)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        }else {
            payloads.forEach {
                val list = it as List<*>
                when (list[0] as Int) {
                    NOTIFY_ITEM_REFRESH -> {
                        holder.mVpLayout.refresh()
                        if (list[1] as Boolean) {
                            holder.mVpLayout.backCurrentTime()
                        }
                    }
                    NOTIFY_ITEM_DATA_CHANGED -> {
                        holder.mVpLayout.refreshAfterDataChanged()
                        if (list[1] as Boolean) {
                            holder.mVpLayout.backCurrentTime()
                        }
                    }
                    NOTIFY_ITEM_SCROLL_TO -> {
                        holder.mVpLayout.timeLineScrollTo(list[1] as Int)
                    }
                    NOTIFY_ITEM_SLOWLY_SCROLL_TO -> {
                        holder.mVpLayout.timeLineSlowlyScrollTo(list[1] as Int)
                    }
                    NOTIFY_ITEM_BACK_CURRENT_ITEM -> {
                        holder.mVpLayout.backCurrentTime()
                    }
                    NOTIFY_ITEM_CANCEL_AUTO_BACK_CURRENT_ITEM -> {
                        holder.mVpLayout.cancelAutoBackCurrentTime()
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vpLayout = holder.mVpLayout
        vpLayout.initialize(dayBeans[position], position)
    }

    override fun getItemCount(): Int {
        return dayBeans.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == showNowTimeLinePosition) {
            return SHOW_NOW_TIME_LINE_POSITION
        }
        return NOT_SHOW
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.mVpLayout.onViewDetachedFromWindow()
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.mVpLayout.onViewRecycled()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mVpLayout = itemView as VpLayout
    }
}