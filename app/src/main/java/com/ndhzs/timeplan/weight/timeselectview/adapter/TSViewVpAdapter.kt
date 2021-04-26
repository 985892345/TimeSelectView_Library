package com.ndhzs.timeplan.weight.timeselectview.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewDayBean
import com.ndhzs.timeplan.weight.timeselectview.layout.VpLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import kotlin.collections.ArrayList

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/24
 * @description
 */
class TSViewVpAdapter(dayBeans: ArrayList<TSViewDayBean>, data: TSViewInternalData, viewPager2: ViewPager2, showNowTimeLinePosition: Int) : RecyclerView.Adapter<TSViewVpAdapter.ViewHolder>() {

    fun notifyItemRefresh(position: Int, isBackToCurrentTime: Boolean) {
        mRefreshPosition = position
        notifyItemChanged(position, listOf(NOTIFY_ITEM_REFRESH, isBackToCurrentTime))
    }

    fun setOnScrollListener(l: (scrollY: Int) -> Unit) {
        mOnScrollListener = l
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
    }

    private val mDayBeans = dayBeans
    private val mData = data
    private val mViewPager2 = viewPager2
    private val mShowNowTimeLinePosition = showNowTimeLinePosition

    private var mOnScrollListener: ((scrollY: Int) -> Unit)? = null

    private var mRefreshPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = VpLayout(parent.context, mData, mViewPager2, mDayBeans[0].day, viewType == SHOW_NOW_TIME_LINE_POSITION)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        }else {
            val list = payloads[0] as List<*>
            if ((list[0] as Int) == NOTIFY_ITEM_REFRESH) {
                holder.mVpLayout.refresh()
                if (list[1] as Boolean) {
                    holder.mVpLayout.backCurrentTime()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vpLayout = holder.mVpLayout
        vpLayout.initialize(mDayBeans[position], position)
        vpLayout.setOnScrollListener { scrollY, vpPosition ->
            mOnScrollListener?.invoke(scrollY)
        }
    }

    override fun getItemCount(): Int {
        return mDayBeans.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == mShowNowTimeLinePosition) {
            return SHOW_NOW_TIME_LINE_POSITION
        }
        return NOT_SHOW
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.mVpLayout.onViewRecycled()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mVpLayout = itemView as VpLayout
    }
}