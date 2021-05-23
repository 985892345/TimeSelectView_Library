package com.ndhzs.timeselectview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
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
import kotlin.math.abs

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/2
 * @description 顶层View，依次包含[BackCardView]、
 * [com.ndhzs.timeselectview.layout.TimeScrollView]
 */
class TimeSelectView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * 初始化数据，传入 TSViewDayBean 的数组
     *
     * **注意：** 暂不支持嵌套在竖向的 ViewPager2 中，但支持横向 ViewPager2，且解决滑动冲突请使用 [isDealWithTouchEvent]；
     * 也不支持镶嵌在 RecyclerView 中；嵌套在 ViewPager 中可能也会出现问题
     * @param dayBeans 以 beans 的一维长度为 ViewPager2 的 item 数
     * @param showNowTimeLinePosition 显示时间线的位置，从0开始，传入负数将不会显示
     * @param currentItem 内部 ViewPager2 的 item 位置，默认值为0
     * @param smoothScroll 设置上方的 currentItem 后，在初始化时是否显示移动动画，默认值为 false
     */
    fun initializeBean(dayBeans: List<TSViewDayBean>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false) {
        if (childCount == 0) {
            mVpAdapter = TSViewVpAdapter(dayBeans, mData, mViewPager2, showNowTimeLinePosition)
            mViewPager2.adapter = mVpAdapter
            mViewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
            setCurrentItem(currentItem, smoothScroll)
            addView(mViewPager2)
            post {
                val location = IntArray(2)
                getLocationOnScreen(location)
                mOnScreenRect.left = location[0]
                mOnScreenRect.top = location[1]
                mOnScreenRect.right = location[0] + width
                mOnScreenRect.bottom = location[1] + height
                var viewParent = parent
                var distance = 0
                while (viewParent is View) {
                    if (viewParent is ViewPager2) {
                        mOuterViewPager2 = viewParent
                        // 因为镶嵌在 ViewPager2 中，会出现滑动时调用 getLocationOnScreen() 方法
                        // 而出现左右值偏差
                        mOuterViewPager2.getLocationOnScreen(location)
                        mOnScreenRect.left = distance + location[0]
                        mOnScreenRect.right = distance + width + location[0]
                        break
                    }
                    if (viewParent.getParent() !is RecyclerView) {
                        /*
                        * RecyclerView 的下一层还有一个 ViewGroup。如果 TimeSelectView 镶嵌在 ViewPager2 中，
                        * 且你设置了 ViewPager2#setOffscreenPageLimit，这时会使 TimeSelectView#getLocationOnScreen
                        * 的左右值出现问题，具体问题就出现在 RecyclerView 下一层的 ViewGroup 中，因为此时它的 left 值会把
                        * 前一页加载 width 值加上。所以，如果下一层的 ViewParent 是 RecyclerView，就不能加上此时 ViewParent
                        * 的 left 值
                        * */
                        distance += viewParent.left
                    }
                    viewParent = viewParent.getParent()
                }
            }
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
     * **注意：** 对[TSViewTaskBean]修改数据后并不会自己刷新，请手动调用[notifyItemRefresh]进行刷新
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
     * **注意：** 在任务被移至删除区域被删除或长按添加新任务时传进来的数组同样也会改变，所以在数据改变后的回调中不需删掉或增加数据
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
     * **注意：** 在任务增加或被删掉时调用此方法并不会有刷新作用，请调用[notifyItemDataChanged]
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

    /**
     * 返回 TimeSelectView 是否要处理触摸事件，只能用于 HorizontalScrollView 中，
     * 解决滑动冲突问题，具体如何解决请看 README
     */
    fun isDealWithTouchEvent(ev: MotionEvent): Boolean {
        val rawX = ev.rawX.toInt()
        val rawY = ev.rawY.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mOnScreenRect.contains(rawX, rawY)) {
                    postDelayed({
                        isLongClickTimeOut = true
                    }, LONG_CLICK_TIMEOUT + 10)
                    mInitialRawX = rawX
                    mInitialRawY = rawY
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isLongClickTimeOut) {
                    if (abs(rawX - mInitialRawX) <= MOVE_THRESHOLD
                        || abs(rawY - mInitialRawY) <= MOVE_THRESHOLD) {
                        return true
                    }
                }else {
                    return getIsLongClick()
                }
            }
        }
        return false
    }
    private var mInitialRawX = 0
    private var mInitialRawY = 0
    private var isLongClickTimeOut = false
    private lateinit var mOuterViewPager2: ViewPager2

    /**
     * 返回 TimeSelectView 是否要处理触摸事件，只能用于父 View 为横向 ViewPager2 中，
     * 解决滑动冲突问题，具体如何解决请看 README
     *
     * @param myItemPosition 表示 TimeSelectView 在 ViewPager2 中的页面 position
     */
    fun isDealWithTouchEvent(ev: MotionEvent, myItemPosition: Int): Boolean {
        if (mOuterViewPager2.currentItem == myItemPosition) {
            return isDealWithTouchEvent(ev)
        }
        return false
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

        /**
         * 判定为长按所需要的时间，默认为300毫秒，暂不支持修改
         */
        const val LONG_CLICK_TIMEOUT = 300L
    }

    private val mData = TSViewInternalData(context, attrs)
    private val mViewPager2 = ViewPager2(context)
    private lateinit var mVpAdapter: TSViewVpAdapter

    private val mOnScreenRect = Rect()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount == 0) {
            Log.e("TimeSelectView", "You must invoke function of initializeBean!")
        }
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