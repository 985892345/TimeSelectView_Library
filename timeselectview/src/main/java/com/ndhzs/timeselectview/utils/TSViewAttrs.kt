package com.ndhzs.timeselectview.utils

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import com.ndhzs.timeselectview.R
import com.ndhzs.timeselectview.layout.view.RectImgView
import com.ndhzs.timeselectview.layout.view.SeparatorLineView
import kotlin.math.abs

/**
 * .....
 * @author 985892345
 * @email 2767465918@qq.com
 * @data 2021/6/10
 */
class TSViewAttrs private constructor() {

    companion object {
        internal const val Library_name = "TimeSelectView"
    }

    internal var mTSViewAmount = 1 //时间轴个数
        private set
    internal var mCardCornerRadius = 28F //时间轴背景的圆角度数
        private set
    internal var mCenterTime = TSViewTimeUtil.CENTER_TIME_CENTER //当前时间线，支持小数

    private var mTimeRangeString: String? = null
    internal var mTimelineRangeArray = Array(mTSViewAmount) { //时间轴的时间范围值
        val timelineRange = 24 / (mTSViewAmount + 1)
        intArrayOf(it * timelineRange, (it + 1) * timelineRange)
    }
        private set
    internal var mTimelineWidth = 440 //时间轴宽度
        private set
    internal var mTimelineInterval = 30 //相邻时间轴间隔
        private set
    internal var mTimeInterval = 15 //时间间隔数
    internal var mIntervalLeft = 0 //左边时间刻度文字间隔宽度
        private set
    internal var mIntervalRight = 10 //右侧水平线空出来的宽度
        private set

    internal var mIsSuitableIntervalHeight = false // 是否设置成合适的一个小时的间隔高度
        private set
    internal var mIntervalHeight = 0 //一个小时的间隔高度
        private set
    internal var mDefaultBorderColor = 0xFFFF0000.toInt() //默认矩形边框颜色
    internal var mDefaultInsideColor = 0xFFDCCC48.toInt() //默认矩形内部颜色
    internal var mDefaultTaskName = "任务名称" //默认任务名称
        private set
    internal var mTimeTextSize = 0F //时间字体大小
        private set
    internal var mTaskTextSize = 0F //任务字体大小
        private set
    internal var mIsShowDiffTime = true //最终的任务区域是否显示时间差
    internal var mIsShowStartEndTime = true //最终的任务区域是否显示上下边界时间

    // 以下为内部属性
    internal var mTimelineRange = 0 //单个时间轴的时长范围
        private set
    internal var mExtraHeight = 0 //上方和下方多的高度
        private set
    internal var mAllTimelineWidth = 0 //全部时间轴加上了间隔的总宽度
        private set
    internal var mInsideTotalHeight = 0 //总高度
        private set
    internal var mRectViewWidth = 0 //矩形绘制宽度
        private set
    internal var mRectViewTop = 0 //RectView实际绘制区域的顶部值
        private set
    internal var mRectViewBottom = 0 //RectView实际绘制区域的底部值
        private set
    internal var mDragResistance = RectImgView.DEFAULT_DRAG_RESISTANCE //拖动阻力值

    internal var mStartAutoSlide = false //是否处于自动滑动

    internal var mIsLongClick = false //是否处于长按状态
        set(value) {
            if (value) {
                TSViewLongClick.sIsLongClickCount++
            }else {
                TSViewLongClick.sIsLongClickCount--
            }
            field = value
        }
    private var onConditionEndListener: ((condition: TSViewLongClick) -> Unit)? = null //长按结束的回调
    internal var mCondition = TSViewLongClick.NULL //长按状态
        set(value) {
            if (value == TSViewLongClick.NULL) {
                onConditionEndListener?.invoke(mCondition)
            }
            field = value
        }

    /**
     * **WARNING：** 用于内部设置长按结束的监听
     */
    internal fun setOnConditionEndListener(l: ((condition: TSViewLongClick) -> Unit)) {
        onConditionEndListener = l
    }

    /**
     * **WARNING：** 用于内部初始化
     */
    internal fun initialize(context: Context, attrs: AttributeSet) {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView)
        mTSViewAmount = ty.getInt(R.styleable.TimeSelectView_amount, mTSViewAmount)
        mCardCornerRadius = ty.getDimension(R.styleable.TimeSelectView_cardRadius, mCardCornerRadius)
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, mCenterTime)
        mTimeRangeString = ty.getString(R.styleable.TimeSelectView_timeRangeString)
        mTimelineWidth = ty.getDimension(R.styleable.TimeSelectView_timelineWidth,
            mTimelineWidth.toFloat()
        ).toInt()
        mTimelineInterval = ty.getDimension(R.styleable.TimeSelectView_timelineInterval,
            mTimelineInterval.toFloat()
        ).toInt()
        mTimeInterval = ty.getInt(R.styleable.TimeSelectView_timeInterval, mTimeInterval)
        mIntervalLeft = ty.getDimension(R.styleable.TimeSelectView_intervalLeft,
            mIntervalLeft.toFloat()
        ).toInt()
        mIntervalHeight = ty.getLayoutDimension(R.styleable.TimeSelectView_intervalHeight, mIntervalHeight)
        mDefaultBorderColor = ty.getColor(R.styleable.TimeSelectView_defaultBorderColor, mDefaultBorderColor)
        mDefaultInsideColor = ty.getColor(R.styleable.TimeSelectView_defaultInsideColor, mDefaultInsideColor)
        mDefaultTaskName = ty.getString(R.styleable.TimeSelectView_defaultTaskName).toString()
        mTimeTextSize = ty.getDimension(R.styleable.TimeSelectView_timeTextSize, mTimeTextSize)
        mTaskTextSize = ty.getDimension(R.styleable.TimeSelectView_taskTextSize, mTaskTextSize)
        mIsShowDiffTime = ty.getBoolean(R.styleable.TimeSelectView_isShowDiffTime, true)
        mIsShowStartEndTime = ty.getBoolean(R.styleable.TimeSelectView_isShowTopBottomTime, true)
        ty.recycle()
        setAttrs()
    }

    /**
     * **WARNING：** 用于内部调用
     */
    internal fun setAttrs() {
        if (mTSViewAmount < 0 || 24 % mTSViewAmount != 0) {
            throwError("amount")
        }
        setTimeRangeArray(mTimeRangeString)
        if (60 % mTimeInterval != 0) {
            throwError("timeInterval")
        }
        if (mIntervalLeft == 0) {
            if (mTimeTextSize == 0F) {
                val intervalLeft = 0.23F * mTimelineWidth
                mTimeTextSize = intervalLeft / 4
                val paint = Paint()
                var i = 0
                // 对于不同的字体由不同的宽度值，下面的循环是为了找到合适的左侧宽度与文字大小
                // 用循环原因在于我没有找到一个函数能通过宽度值反推字体大小
                while (abs(mIntervalLeft - intervalLeft) > 5 || mIntervalLeft == 0) {
                    i++
                    mTimeTextSize += 1
                    paint.textSize = mTimeTextSize
                    mIntervalLeft = paint.measureText("166:661").toInt()
                    if (i > 50) {
                        mTimeTextSize = intervalLeft * 0.35F
                        break
                    }
                }
            }else {
                val paint = Paint()
                paint.textSize = mTimeTextSize
                mIntervalLeft = paint.measureText("166:661").toInt()
            }
        }else {
            if (mTimeTextSize == 0F) {
                val intervalLeft = mIntervalLeft.toFloat()
                mTimeTextSize = intervalLeft / 4
                val paint = Paint()
                var i = 0
                while (abs(mIntervalLeft - intervalLeft) > 5 || mIntervalLeft == 0) {
                    i++
                    mTimeTextSize += 1
                    paint.textSize = mTimeTextSize
                    mIntervalLeft = paint.measureText("166:661").toInt()
                    if (i > 50) {
                        mTimeTextSize = intervalLeft * 0.35F
                        break
                    }
                }
            }
        }
        if (mIntervalHeight == 0) {
            mIsSuitableIntervalHeight = true
        }
        if (mDefaultTaskName == "null") {
            mDefaultTaskName = "任务名称"
        }
        if (mTaskTextSize == 0F) {
            mTaskTextSize = mTimeTextSize * 1.16F
        }
        mExtraHeight = mIntervalHeight / 2
        mAllTimelineWidth = mTSViewAmount * (mTimelineWidth + mTimelineInterval) - mTimelineInterval
        mInsideTotalHeight = mTimelineRange * mIntervalHeight + 2 * mExtraHeight
        mRectViewWidth = mTimelineWidth - mIntervalLeft - mIntervalRight
        mRectViewTop = mExtraHeight + SeparatorLineView.HORIZONTAL_LINE_WIDTH
        mRectViewBottom = mInsideTotalHeight - mExtraHeight
    }

    private fun setTimeRangeArray(timeRangeArrayString: String?) {
        if (timeRangeArrayString == null) {
            mTimelineRangeArray = Array(mTSViewAmount) {
                val timelineRange = 24 / (mTSViewAmount + 1)
                intArrayOf(it * timelineRange, (it + 1) * timelineRange)
            }
        }else {
            try {
                val times = timeRangeArrayString.split(",")
                if (times.size != mTSViewAmount) {
                    throw Exception()
                }
                mTimelineRangeArray = Array(mTSViewAmount) {
                    val hours = times[it].split("-")
                    val startHour = hours[0].toInt()
                    var endHour = hours[1].toInt()
                    if (startHour >= 24 || endHour > 24) {
                        throw Exception()
                    }
                    if (endHour <= startHour) {
                        endHour += 24
                    }
                    if (mTimelineRange == 0) {
                        mTimelineRange = endHour - startHour
                    }else {
                        if (endHour - startHour != mTimelineRange) {
                            throw Exception()
                        }
                    }
                    intArrayOf(startHour, endHour)
                }
            }catch (e: Exception) {
                throwError("timeRangeArray")
            }
        }
    }

    internal fun setSuitableIntervalHeight(height: Int) {
        mIntervalHeight = height
        mExtraHeight = mIntervalHeight / 2
        mInsideTotalHeight = mTimelineRange * mIntervalHeight + 2 * mExtraHeight
        mRectViewTop = mExtraHeight + SeparatorLineView.HORIZONTAL_LINE_WIDTH
        mRectViewBottom = mInsideTotalHeight - mExtraHeight
    }

    private fun throwError(attrName: String) {
        throw IllegalAccessException("${Library_name}: " +
                "Your attrs of $attrName is wrong!")
    }

    class Builder {

        private val mAttrs = TSViewAttrs()

        /**
         * 设置时间轴个数（建议不超过3个）
         *
         * **WARNING：** 如果在横屏中，请在theme中适配全面屏，务必将刘海区域进行填充，不然点击区域可能出现偏差
         */
        fun setAmount(amount: Int): Builder {
            mAttrs.mTSViewAmount = amount
            return this
        }

        /**
         * 设置时间轴背景的圆角度数
         */
        fun setCardRadius(radius: Float): Builder {
            mAttrs.mCardCornerRadius = radius
            return this
        }

        /**
         * 以输入时间线的为中心线，时间只能在第一个时间轴的范围内（支持小数）。
         *
         * 1、输入 center 为以中心值为中心线
         *
         * 2、输入 now_time 为以当前时间值为中心线
         */
        fun setCenterTime(centerTime: Float): Builder {
            mAttrs.mCenterTime = centerTime
            return this
        }

        /**
         * 设置时间范围。格式为"2-18,12-4"（逗号后没有空格）
         *
         * **WARNING：**
         *
         * 1、时间都必须大于0且小于或等于24；
         *
         * 2、每个时间段的差值必须相等；
         *
         * 3、允许出现重复时间段；
         */
        fun setTimeRangeString(range: String): Builder {
            mAttrs.mTimeRangeString = range
            return this
        }

        /**
         * 设置时间轴宽度
         */
        fun setTimelineWidth(width: Int): Builder {
            mAttrs.mTimelineWidth = width
            return this
        }

        /**
         * 设置相邻时间轴间隔宽度，输入 0 或者不调用该函数可以设置成根据控件外高度自动调整 intervalHeight
         */
        fun setTimelineInterval(interval: Int): Builder {
            mAttrs.mTimelineInterval = interval
            return this
        }

        /**
         * 设置时间默认间隔数
         *
         * **NOTE：** 必须为60的因数，若不是，将以15为间隔数
         */
        fun setTimeInterval(timeInterval: Int): Builder {
            mAttrs.mTimeInterval = timeInterval
            return this
        }

        /**
         * 设置时间轴左侧的时间文字间隔宽度
         */
        fun setIntervalLeft(left: Int): Builder {
            mAttrs.mIntervalLeft = left
            return this
        }

        /**
         * 设置时间轴每小时间的间隔高度
         */
        fun setIntervalHeight(height: Int): Builder {
            mAttrs.mIntervalHeight = height
            return this
        }

        /**
         * 设置默认任务边框颜色
         */
        fun setDefaultBorderColor(color: Int): Builder {
            mAttrs.mDefaultBorderColor = color
            return this
        }

        /**
         * 设置默认任务内部颜色
         */
        fun setDefaultInsideColor(color: Int): Builder {
            mAttrs.mDefaultInsideColor = color
            return this
        }

        /**
         * 设置默认任务名称
         */
        fun setDefaultTaskName(name: String): Builder {
            mAttrs.mDefaultTaskName = name
            return this
        }

        /**
         * 设置时间轴左侧时间文字大小
         */
        fun setTimeTextSize(size: Float): Builder {
            mAttrs.mTimeTextSize = size
            return this
        }

        /**
         * 设置任务名称文字大小
         *
         * **NOTE：** 不设置的情况下随时间轴左侧时间文字大小改变
         */
        fun setTaskTextSize(size: Float): Builder {
            mAttrs.mTaskTextSize = size
            return this
        }

        /**
         * 设置最终的任务区域是否显示时间差
         */
        fun setIsShowDiffTime(boolean: Boolean): Builder {
            mAttrs.mIsShowDiffTime = boolean
            return this
        }

        /**
         * 设置最终的任务区域是否显示上下边界时间
         */
        fun setIsShowTopBottomTime(boolean: Boolean): Builder {
            mAttrs.mIsShowStartEndTime = boolean
            return this
        }

        fun build(): TSViewAttrs {
            return mAttrs
        }
    }
}