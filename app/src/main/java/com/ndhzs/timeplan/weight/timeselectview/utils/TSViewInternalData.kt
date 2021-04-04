package com.ndhzs.timeplan.weight.timeselectview.utils

import android.content.Context
import android.util.AttributeSet
import com.ndhzs.timeplan.R
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/2
 * @description
 */
class TSViewInternalData(context: Context, attrs: AttributeSet? = null) {

    val mEndHour: Int  //结束时间
    val mStartHour:Int  //起始时间
    val mCenterTime: Float //当前时间线，支持小数

    val mTimeTextSize: Float //时间字体大小
    val mTaskTextSize: Float //任务字体大小

    val mDefaultBorderColor:Int //默认矩形边框颜色
    val mDefaultInsideColor:Int //默认矩形内部颜色
    val mDefaultTaskName: String //默认任务名称

    var mTimeInterval: Int //时间间隔数

    val mTSViewAmount: Int //时间轴个数
    val mCardCornerRadius: Float //时间轴背景的圆角度数

    val mTimelineWidth: Int //时间轴宽度
    val mTimelineInterval: Int //相邻时间轴间隔

    val mExtraHeight:Int //上方和下方多的高度
    val mIntervalLeft: Int //左边的文字间隔宽度
    val mIntervalHeight: Int //一个小时的间隔高度
    val mTotalHeight: Int //总高度

    val mRectViewTop: Int //RectView实际绘制区域的顶部值
    val mRectViewBottom: Int //RectView实际绘制区域的底部值

    var mIsShowDiffTime: Boolean //最终的任务区域是否显示时间差
    var mIsShowStartEndTime: Boolean //最终的任务区域是否显示上下边界时间

    var mRectViewToTSViewLeft = 0 //RectView与TimeSelectView的距离宽度

    var mIsLongClick = false //是否处于长按状态
        set(value) {
            if (value) {
                TSViewLongClick.sIsLongClickCount++
            }else {
                TSViewLongClick.sIsLongClickCount--
            }
            field = value
        }
    var mStartAutoSlide = false //是否开启自动滑动

    var mCondition = TSViewLongClick.NULL //长按状态
        set(value) {
            if (value == TSViewLongClick.NULL) {
                onConditionEndListener?.invoke(mCondition)
            }
            field = value
        }
    private var onConditionEndListener: ((condition: TSViewLongClick) -> Unit)? = null //长按结束的回调

    /**
     * 设置长按结束的监听
     */
    fun setOnConditionEndListener(l: ((condition: TSViewLongClick) -> Unit)) {
        onConditionEndListener = l
    }

    init {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView)
        mEndHour = ty.getInteger(R.styleable.TimeSelectView_endHour, 26)
        mStartHour = ty.getInteger(R.styleable.TimeSelectView_startHour, 2)
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, -1F)

        mTimeTextSize = ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 40F)
        mTaskTextSize = ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 40F)

        mDefaultBorderColor = ty.getColor(R.styleable.TimeSelectView_defaultBorderColor, 0xFFFF0000.toInt())
        mDefaultInsideColor = ty.getColor(R.styleable.TimeSelectView_defaultInsideColor, 0xFFDCCC48.toInt())
        mDefaultTaskName = ty.getString(R.styleable.TimeSelectView_defaultTaskName).toString()

        mTSViewAmount = ty.getInt(R.styleable.TimeSelectView_amount, 1)
        mCardCornerRadius = ty.getDimension(R.styleable.TimeSelectView_cardCornerRadius, 28F)

        mTimeInterval = ty.getInt(R.styleable.TimeSelectView_timeInterval, 15)
        if (60 % mTimeInterval != 0) mTimeInterval = 15

        mTimelineWidth = ty.getDimension(R.styleable.TimeSelectView_timelineWidth, 500F).toInt()
        mTimelineInterval = ty.getDimension(R.styleable.TimeSelectView_timelineInterval, 90F).toInt()

        mIntervalLeft = ty.getDimension(R.styleable.TimeSelectView_intervalLeft, 110F).toInt()
        mIntervalHeight = ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 194F).toInt()
        mExtraHeight = mIntervalHeight / 2
        mTotalHeight = (mEndHour - mStartHour) * mIntervalHeight + 2 * mExtraHeight

        mRectViewTop = mExtraHeight + SeparatorLineView.HORIZONTAL_LINE_WIDTH
        mRectViewBottom = mTotalHeight - mExtraHeight

        mIsShowDiffTime = ty.getBoolean(R.styleable.TimeSelectView_isShowDiffTime, false)
        mIsShowStartEndTime = ty.getBoolean(R.styleable.TimeSelectView_isShowTopBottomTime, true)
        ty.recycle()
    }
}