package com.ndhzs.timeselectview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.ndhzs.timeselectview.adapter.TSViewVpAdapter
import com.ndhzs.timeselectview.bean.TSViewDayBean
import com.ndhzs.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.layout.BackCardView
import com.ndhzs.timeselectview.layout.view.RectImgView
import com.ndhzs.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeselectview.utils.tscrollview.TScrollViewTouchEvent
import kotlin.collections.ArrayList

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/2
 * @description 顶层View，依次包含[BackCardView]、
 * [com.ndhzs.timeselectview.layout.TimeScrollView]
 */
class TimeSelectView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * 初始化数据，传入TSViewDayBean的数组
     *
     * 以beans的一维长度为ViewPager2的item数
     * @param showNowTimeLinePosition 显示时间线的位置，从0开始，传入负数将不会显示
     * @param currentItem 内部 ViewPager2 的 item 位置，默认值为0
     * @param smoothScroll 设置上方的 currentItem 后，在初始化时是否显示移动动画，默认值为false
     */
    fun initializeBean(dayBeans: ArrayList<TSViewDayBean>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false) {
        if (childCount == 0) {
            mVpAdapter = TSViewVpAdapter(dayBeans, mData, mViewPager2, showNowTimeLinePosition)
            mViewPager2.adapter = mVpAdapter
            mViewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
            setCurrentItem(currentItem, smoothScroll)
            addView(mViewPager2)
        }else {
            throw Exception("TimeSelectView has been initialized!")
        }
    }

    /**
     * 当前页面时间轴的滑动回调，不是ViewPager2的滑动回调
     *
     * 若你想监听ViewPager2的滑动，请使用[registerOnPageChangeCallback]
     */
    fun setOnScrollListener(l: (scrollY: Int, itemPosition: Int) -> Unit) {
        mVpAdapter.setOnScrollListener(l)
    }

    /**
     * 设置时间间隔数，必须为60的因数，若不是，将以15为间隔数
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
     * 点击当前任务的监听，会返回当前点击任务的数据类
     *
     * 注意：对[TSViewTaskBean]修改数据后并不会自己刷新，请手动调用[notifyItemRefresh]进行刷新
     */
    fun setOnTSVClickListener(onClick: (taskBean: TSViewTaskBean) -> Unit) {
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
     * 对数据改变进行监听
     *
     * 注意：在任务被移至删除区域被删除或长按添加新任务时传进来的数组同样也会改变，所以在数据改变后的回调中不需删掉或增加数据
     */
    fun setOnDataListener(l: OnDataChangeListener) {
        mData.mDataChangeListener = l
    }

    /**
     * 得到当前页面的TimeSelectView是否处于长按状态。
     * 若你想得到软件中所有的TimeSelectView是否存在处于长按状态的，可以使用[TSViewLongClick.sHasLongClick]
     */
    fun getIsLongClick(): Boolean {
        return mData.mIsLongClick
    }

    /**
     * 得到当前页面的时间轴的ScrollY
     */
    fun getTimeLineScrollY(): Int {
        return mVpAdapter.getTimeLineScrollY()
    }

    /**
     * 默认通知当前页面所有的任务刷新，可输入索引值定向刷新
     *
     * 注意：在任务增加或被删掉时调用此方法并不会有刷新作用，请调用[notifyItemDataChanged]
     * @param isBackToCurrentTime 是否回到xml中设置的CurrentTime
     */
    fun notifyItemRefresh(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false) {
        mVpAdapter.notifyItemRefresh(position, isBackToCurrentTime)
    }

    /**
     * 该方法用于任务在外面被增加或删除时提醒控件重新读取数据
     *
     * @param isBackToCurrentTime 是否回到xml中设置的CurrentTime
     */
    fun notifyItemDataChanged(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false) {
        mVpAdapter.notifyItemDataChanged(position, isBackToCurrentTime)
    }

    /**
     * 通知ViewPager2的所有item刷新
     */
    fun notifyAllItemRefresh() {
        mVpAdapter.notifyAllItemRefresh()
    }

    /**
     * 设置内部ViewPager2的OnPageChangeCallback
     */
    fun registerOnPageChangeCallback(callback: OnPageChangeCallback) {
        mViewPager2.registerOnPageChangeCallback(callback)
    }

    /**
     * 使时间轴瞬移，与ScrollTo相同
     */
    fun timeLineScrollTo(scrollY: Int) {
        mVpAdapter.timeLineScrollTo(scrollY)
    }

    /**
     * 与ScrollBy相同
     * @param dy dy > 0，向上瞬移；dy < 0，向下瞬移
     */
    fun timeLineScrollBy(dy: Int) {
        timeLineScrollTo(getTimeLineScrollY() + dy)
    }

    /**
     * 使时间轴较缓慢地滑动，并有回弹动画
     */
    fun timeLineSlowlyScrollTo(scrollY: Int) {
        mVpAdapter.timeLineSlowlyScrollTo(scrollY)
    }

    /**
     * 当前页面回到xml中设置的CurrentTime
     */
    fun backCurrentTime() {
        mVpAdapter.backCurrentTime()
    }

    /**
     * 取消当前页面自动回到xml中设置的CurrentTime的延时。延时是在每次手指离开时间轴就会开启
     */
    fun cancelAutoBackCurrent() {
        mVpAdapter.cancelAutoBackCurrent()
    }

    /**
     * 设置内部ViewPager2显示的页面位置
     */
    fun setCurrentItem(item: Int, smoothScroll: Boolean = true) {
        mViewPager2.setCurrentItem(item, smoothScroll)
    }

    /**
     * 设置相邻时间轴中拖动任务的阻力值
     * @param resistance 不填入值时还原为初始化值
     */
    fun setDragResistance(resistance: Int = DEFAULT_DRAG_RESISTANCE) {
        mData.mDragResistance = resistance
    }

    /**
     * 得到内部ViewPager2的当前item索引
     */
    fun getCurrentItem(): Int {
        return mViewPager2.currentItem
    }

    companion object {
        /**
         * 识别是长按而能移动的阈值
         */
        const val MOVE_THRESHOLD = TScrollViewTouchEvent.MOVE_THRESHOLD

        /**
         * 在多个时间轴中左右拖动时的默认阻力值
         */
        const val DEFAULT_DRAG_RESISTANCE = RectImgView.DEFAULT_DRAG_RESISTANCE
    }

    private val mData = TSViewInternalData(context, attrs)
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
        fun onDataAdd(newData: TSViewTaskBean)
        fun onDataDelete(deletedData: TSViewTaskBean)
        fun onDataAlter(alterData: TSViewTaskBean)
    }
}