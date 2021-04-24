package com.ndhzs.timeplan.weight.timeselectview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.ndhzs.timeplan.weight.timeselectview.adapter.TSViewVpAdapter
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.BackCardView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/2
 * @description 顶层View，依次包含[BackCardView]、
 * [com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView]
 */
class TimeSelectView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine(position: Int = mViewPager2.currentItem) {
        mVpAdapter.showNowTimeLine(position)
    }

    /**
     * 时间间隔数
     * @param timeInterval 必须为60的因数，若不是，将以15为间隔数
     */
    fun setTimeInterval(timeInterval: Int) {
        if (60 % timeInterval == 0) {
            mData.mTimeInterval = timeInterval
        }else {
            mData.mTimeInterval = 15
        }
    }

    /**
     * 最终的任务区域是否显示时间差
     */
    fun setIsShowDiffTime(boolean: Boolean) {
        if (mData.mIsShowDiffTime != boolean) {
            mData.mIsShowDiffTime = boolean
            notifyAllItemRefresh()
        }
    }

    /**
     * 最终的任务区域是否显示上下边界时间
     */
    fun setIsShowTopBottomTime(boolean: Boolean) {
        if (mData.mIsShowStartEndTime != boolean) {
            mData.mIsShowStartEndTime = boolean
            notifyAllItemRefresh()
        }
    }

    /**
     * 默认通知当前显示页面所有的任务刷新，可输入索引值定向刷新
     */
    fun notifyItemRefresh(position: Int = mViewPager2.currentItem) {
        mVpAdapter.notifyItemChanged(position)
    }

    /**
     * 通知所有item刷新
     */
    fun notifyAllItemRefresh() {
        mVpAdapter.notifyDataSetChanged()
    }

    fun notifyNowItemClear(position: Int = mViewPager2.currentItem) {
        mBeans[position].clear()
        notifyItemRefresh(position)
    }

    /**
     * 点击当前任务的监听，会返回当前点击任务的数据类
     * 注意：修改数据后并不会自己刷新，请手动调用notifyAllTaskRefresh()进行刷新
     */
    fun setOnTSVClickListener(onClick: (bean: TSViewBean) -> Unit) {
        mData.mOnClickListener = onClick
    }

    /**
     * 设置长按监听接口
     */
    fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit)) {
        mData.mOnLongClickStartListener = onStart
        mData.mOnLongClickEndListener = onEnd
    }

    /**
     * 得到当前的TimeSelectView是否处于长按状态，
     * 若你想得到软件中所有的TimeSelectView是否存在处于长按状态的，可以使用TSViewLongClick.sIsLongClick
     */
    fun getIsLongClick(): Boolean {
        return mData.mIsLongClick
    }

    /**
     * 设置数据改变监听
     */
    fun setOnDataListener(l: OnDataChangeListener) {
        mData.mDataChangeListener = l
    }

    /**
     * 初始化数据，传入TSViewBean的数组。
     *
     * 以beans的一维长度为ViewPager2的长度。
     *
     * @param currentItem 默认值为1
     * @param smoothScroll 默认值为false，是快速地滑动到currentItem
     */
    fun initializeBean(beans: ArrayList<ArrayList<TSViewBean>>, currentItem: Int = 1, smoothScroll: Boolean = false) {
        if (childCount == 0) {
            mBeans = beans
            mVpAdapter = TSViewVpAdapter(mBeans, mData, mViewPager2)
            mViewPager2.adapter = mVpAdapter
            mViewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
            setCurrentItem(currentItem, smoothScroll)
            addView(mViewPager2)
        }
    }

    /**
     * 设置ViewPager2的OnPageChangeCallback
     */
    fun registerOnPageChangeCallback(callback: OnPageChangeCallback) {
        mViewPager2.registerOnPageChangeCallback(callback)
    }

    /**
     * 设置当前ViewPager2的页数位置
     */
    fun setCurrentItem(item: Int, smoothScroll: Boolean = true) {
        mViewPager2.setCurrentItem(item, smoothScroll)
    }

    private val mData = TSViewInternalData(context, attrs)
    private lateinit var mBeans: ArrayList<ArrayList<TSViewBean>>
    private val mViewPager2 = ViewPager2(context)
    private lateinit var mVpAdapter: TSViewVpAdapter

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = mData.mAllTimelineWidth + BackCardView.LEFT_RIGHT_MARGIN * 2
        var newWidthMS = widthMeasureSpec
        var newHeightMS = heightMeasureSpec
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                newWidthMS = MeasureSpec.makeMeasureSpec(minWidth, MeasureSpec.EXACTLY)
            }
            MeasureSpec.EXACTLY -> {
                if (MeasureSpec.getSize(widthMeasureSpec) < minWidth) {
                    Log.e("TimeSelectView", "Your layout_width of TimeSelectView is too small to include timeline!!!!!")
                    Log.e("TimeSelectView", "Please enlarge the layout_width or shrink the timelineWidth of attrs.")
                }
                newWidthMS = widthMeasureSpec
            }
        }

        when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                newHeightMS = MeasureSpec.makeMeasureSpec(1000, MeasureSpec.EXACTLY)
            }
            MeasureSpec.EXACTLY -> {
            }
        }

        super.onMeasure(newWidthMS, newHeightMS)
    }

    interface OnDataChangeListener {
        fun onDataAdd(newData: TSViewBean)
        fun onDataDelete(deletedData: TSViewBean)
        fun onDataAlter(alterData: TSViewBean)
    }
}