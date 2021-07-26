package com.ndhzs.timeselectview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.ndhzs.timeselectview.adapter.TSViewVpAdapter
import com.ndhzs.timeselectview.bean.TSViewDayBean
import com.ndhzs.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.layout.BackCardView
import com.ndhzs.timeselectview.layout.view.RectImgView
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.utils.TSViewListeners
import com.ndhzs.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeselectview.utils.tscrollview.TScrollViewTouchEvent
import kotlin.math.abs

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/2
 * @description 顶层View，依次包含[BackCardView]、
 * [com.ndhzs.timeselectview.layout.TimeScrollView]
 */
class TimeSelectView : FrameLayout {

    private val mAttrs: TSViewAttrs
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mAttrs = TSViewAttrs.Builder().build()
        mAttrs.initialize(context, attrs)
    }

    constructor(context: Context, tsViewAttrs: TSViewAttrs) : super(context) {
        mAttrs = tsViewAttrs
        mAttrs.setAttrs()
    }

    /**
     * 初始化数据，传入 TSViewDayBean 的数组
     *
     * @param dayBeans 以 beans 的一维长度为 ViewPager2 的 item 数
     * @param showNowTimeLinePosition 显示时间线的位置，从0开始，传入负数将不会显示
     * @param currentItem 内部 ViewPager2 的 item 位置，默认值为0
     * @param smoothScroll 设置上方的 currentItem 后，在初始化时是否显示移动动画，默认值为 false
     */
    fun initializeBean(dayBeans: List<TSViewDayBean>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false) {
        if (childCount == 0) {
            mVpAdapter = TSViewVpAdapter(dayBeans, mAttrs, mListeners, mViewPager2, showNowTimeLinePosition)
            mViewPager2.adapter = mVpAdapter
            mViewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
            setCurrentItem(currentItem, smoothScroll)
            attachViewToParent(mViewPager2, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }else {
            throw IllegalAccessException("${TSViewAttrs.Library_name}: ${TSViewAttrs.Library_name} has been initialized!")
        }
    }

    /**
     * 当前页面时间轴的滑动回调，不是 ViewPager2 的滑动回调
     *
     * **NOTE：** 若你想监听 ViewPager2 的滑动，请使用 [registerOnPageChangeCallback]
     */
    fun setOnScrollListener(l: (scrollY: Int, itemPosition: Int) -> Unit) {
        mVpAdapter.setOnScrollListener(l)
    }

    /**
     * 设置时间间隔数，必须为 60 的因数，若不是，将以 15 为间隔数
     * @param timeInterval 必须为 60 的因数，若不是，将以 15 为间隔数
     */
    fun setTimeInterval(timeInterval: Int) {
        if (60 % timeInterval == 0) {
            mAttrs.mTimeInterval = timeInterval
        }else {
            mAttrs.mTimeInterval = 15
        }
    }

    /**
     * 最终的任务区域是否显示时间差
     */
    fun setIsShowDiffTime(boolean: Boolean) {
        if (mAttrs.mIsShowDiffTime != boolean) {
            mAttrs.mIsShowDiffTime = boolean
            notifyAllItemRefresh()
        }
    }

    /**
     * 最终的任务区域是否显示上下边界时间
     */
    fun setIsShowTopBottomTime(boolean: Boolean) {
        if (mAttrs.mIsShowStartEndTime != boolean) {
            mAttrs.mIsShowStartEndTime = boolean
            notifyAllItemRefresh()
        }
    }

    /**
     * 点击当前任务的监听，会返回当前点击任务的数据类
     *
     * **WARNING：** 对 [TSViewTaskBean] 修改数据后并不会自己刷新，请手动调用 [notifyItemRefresh] 进行刷新
     */
    fun setOnTSVClickListener(onClick: (taskBean: TSViewTaskBean) -> Unit) {
        mListeners.mOnClickListener = onClick
    }

    /**
     * 设置长按监听接口
     */
    fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit)) {
        mListeners.mOnLongClickStartListener = onStart
        mListeners.mOnLongClickEndListener = onEnd
    }

    /**
     * 对数据改变进行监听
     *
     * **WARNING：** 在任务被移至删除区域被删除或长按添加新任务时传进来的数组同样也会改变，所以在数据改变后的回调中不需删掉或增加数据
     */
    fun setOnDataListener(l: OnDataChangeListener) {
        mListeners.mOnDataChangeListener = l
    }

    /**
     * 得到当前页面的 TimeSelectView 是否处于长按状态。
     * 若你想得到软件中所有的 TimeSelectView 是否存在处于长按状态的，可以使用 [TSViewLongClick.sHasLongClick]
     */
    fun getIsLongClick(): Boolean {
        return mAttrs.mIsLongClick
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
     * **WARNING：** 在任务增加或被删掉时调用此方法并不会有刷新作用，请调用 [notifyItemDataChanged]
     * @param isBackToCurrentTime 是否回到 xml 中设置的 CurrentTime
     */
    fun notifyItemRefresh(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false) {
        mVpAdapter.notifyItemRefresh(position, isBackToCurrentTime)
    }

    /**
     * 该方法用于任务在外面被增加或删除时提醒控件重新读取数据
     *
     * @param isBackToCurrentTime 是否回到 xml 中设置的 CurrentTime
     */
    fun notifyItemDataChanged(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false) {
        mVpAdapter.notifyItemDataChanged(position, isBackToCurrentTime)
    }

    /**
     * 通知 ViewPager2 的所有 item 刷新
     */
    fun notifyAllItemRefresh() {
        mVpAdapter.notifyAllItemRefresh()
    }

    /**
     * 设置内部 ViewPager2 的 OnPageChangeCallback
     */
    fun registerOnPageChangeCallback(callback: OnPageChangeCallback) {
        mViewPager2.registerOnPageChangeCallback(callback)
    }

    /**
     * 使时间轴瞬移，与 ScrollTo 类似
     */
    fun timeLineScrollTo(scrollY: Int) {
        mVpAdapter.timeLineScrollTo(scrollY)
    }

    /**
     * 与 ScrollBy 类似
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
     * 当前页面回到 xml 中设置的 CurrentTime
     */
    fun backCurrentTime() {
        mVpAdapter.backCurrentTime()
    }

    /**
     * 取消当前页面自动回到 xml 中设置的 CurrentTime 的延时。延时是在每次手指离开时间轴就会开启
     */
    fun cancelAutoBackCurrent() {
        mVpAdapter.cancelAutoBackCurrent()
    }

    /**
     * 设置内部 ViewPager2 显示的页面位置
     */
    fun setCurrentItem(item: Int, smoothScroll: Boolean = true) {
        mViewPager2.setCurrentItem(item, smoothScroll)
    }

    /**
     * 设置相邻时间轴中拖动任务的阻力值
     * @param resistance 不填入值时还原为初始化值
     */
    fun setDragResistance(resistance: Int = DEFAULT_DRAG_RESISTANCE) {
        mAttrs.mDragResistance = resistance
    }

    /**
     * 得到内部 ViewPager2 的当前 item 索引
     */
    fun getCurrentItem(): Int {
        return mViewPager2.currentItem
    }

    companion object {
        /**
         * 识别是长按而能移动的阈值，默认为 5
         */
        const val MOVE_THRESHOLD = TScrollViewTouchEvent.MOVE_THRESHOLD

        /**
         * 在多个时间轴中左右拖动时的默认阻力值，默认为 20
         */
        const val DEFAULT_DRAG_RESISTANCE = RectImgView.DEFAULT_DRAG_RESISTANCE

        /**
         * 判定为长按所需要的时间，默认为 300 毫秒
         */
        @JvmStatic
        var LONG_CLICK_TIMEOUT = TScrollViewTouchEvent.LONG_CLICK_TIMEOUT
            set(value) {
                TScrollViewTouchEvent.LONG_CLICK_TIMEOUT = value
                field = value
            }
    }

    private val mListeners = TSViewListeners()
    private val mViewPager2 = ViewPager2(context)
    private lateinit var mVpAdapter: TSViewVpAdapter

    private var mWidth = 0
    private var mHeight = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        val minWidth = mAttrs.mAllTimelineWidth + BackCardView.LEFT_RIGHT_MARGIN * 2
        var newWidthMS = widthMeasureSpec
        var newHeightMS = heightMeasureSpec
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                newWidthMS = MeasureSpec.makeMeasureSpec(minWidth, MeasureSpec.EXACTLY)
                width = minWidth
            }
            MeasureSpec.EXACTLY -> {
            }
        }

        when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                newHeightMS = MeasureSpec.makeMeasureSpec(1000, MeasureSpec.EXACTLY)
                height = 1000
            }
            MeasureSpec.EXACTLY -> {
            }
        }
        super.onMeasure(newWidthMS, newHeightMS)
        if (mWidth != width || mHeight != height) {
            mWidth = width
            mHeight = height
            measureOver()
        }
    }

    private fun measureOver() {
        calculateIntervalHeight()
    }

    private fun calculateIntervalHeight() {
        if (mAttrs.mIsSuitableIntervalHeight) {
            var intervalHeight = mAttrs.mTimelineWidth / 1.6F
            val m = mHeight / intervalHeight
            val n = m - m.toInt()
            val lower = 0.85F
            val upper = 0.99F
            if (n !in lower..upper) {
                val p = if (abs(n - lower) < abs(n - upper)) {
                    m.toInt() + lower
                }else m.toInt() + upper
                intervalHeight = mHeight / p
            }
            mAttrs.setSuitableIntervalHeight(intervalHeight.toInt())
        }
    }

    interface OnDataChangeListener {
        fun onDataAdd(newData: TSViewTaskBean)
        fun onDataDelete(deletedData: TSViewTaskBean)
        fun onDataAlter(alterData: TSViewTaskBean)
    }
}