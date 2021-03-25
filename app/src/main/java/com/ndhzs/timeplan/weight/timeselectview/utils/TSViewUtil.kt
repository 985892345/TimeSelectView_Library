package com.ndhzs.timeplan.weight.timeselectview.utils

import android.content.Context
import android.util.AttributeSet
import com.ndhzs.timeplan.R
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.rectview.RectViewDrawUtil
import com.ndhzs.timeplan.weight.timeselectview.utils.rectview.RectViewRectUtil

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
class TSViewUtil(context: Context, attrs: AttributeSet? = null) {

    val mEndHour: Int  //结束时间
    val mStartHour:Int  //起始时间
    val mCenterTime: Float //当前时间线，支持小数
    val mDefaultBorderColor:Int //默认矩形边框颜色
    val mDefaultInsideColor:Int //默认矩形内部颜色
    val mDefaultTaskName: String //默认任务名称
    val mExtraHeight:Int //上方和下方多的高度
    val mIntervalLeft: Int //左边的文字间隔宽度
    val mIntervalHeight: Int //一个小时的间隔高度
    val mTotalHeight: Int //总高度
    val mTimeTextSize: Float //时间字体大小
    val mTaskTextSize: Float //任务字体大小
    var mIsShowDiffTime: Boolean //最终的任务区域是否显示时间差
    var mIsShowStartEndTime: Boolean //最终的任务区域是否显示上下边界时间
    val mTimeUtil: TSViewTimeUtil
    val mDrawUtil: RectViewDrawUtil
    val mRectUtil: RectViewRectUtil
    var mCondition = TSViewLongClick.NULL
        set(value) {
            if (value == TSViewLongClick.NULL) {
                onConditionEndListener?.invoke(mCondition)
                TSViewLongClick.sIsNotLongClickCount--
            }else {
                TSViewLongClick.sIsNotLongClickCount++
            }
            field = value
        }
    private var onConditionEndListener: ((condition: TSViewLongClick) -> Unit)? = null

    init {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView)
        mEndHour = ty.getInteger(R.styleable.TimeSelectView_endHour, 26)
        mStartHour = ty.getInteger(R.styleable.TimeSelectView_startHour, 2)
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, -1F)
        mDefaultBorderColor = ty.getColor(R.styleable.TimeSelectView_defaultBorderColor, 0xFFFF0000.toInt())
        mDefaultInsideColor = ty.getColor(R.styleable.TimeSelectView_defaultInsideColor, 0xFFDCCC48.toInt())
        mDefaultTaskName = ty.getString(R.styleable.TimeSelectView_defaultTaskName).toString()
        mIntervalLeft = ty.getDimension(R.styleable.TimeSelectView_intervalLeft, 110F).toInt()
        mIntervalHeight = ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 194F).toInt()
        mTimeTextSize = ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 40F)
        mTaskTextSize = ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 40F)
        mIsShowDiffTime = ty.getBoolean(R.styleable.TimeSelectView_isShowDiffTime, false)
        mIsShowStartEndTime = ty.getBoolean(R.styleable.TimeSelectView_isShowTopBottomTime, true)
        mExtraHeight = mIntervalHeight/2
        mTotalHeight = (mEndHour - mStartHour) * mIntervalHeight + 2 * mExtraHeight
        ty.recycle()
        mTimeUtil = TSViewTimeUtil(this)
        mDrawUtil = RectViewDrawUtil(this)
        mRectUtil = RectViewRectUtil(this)
    }

    /**
     * 返回RectView实际绘制区域的顶部值
     */
    fun getTop(): Int = mExtraHeight + SeparatorLineView.HORIZONTAL_LINE_WIDTH

    /**
     * 返回RectView实际绘制区域的底部值
     */
    fun getBottom(): Int = mTotalHeight - mExtraHeight

    fun setOnConditionEndListener(l: ((condition: TSViewLongClick) -> Unit)) {
        onConditionEndListener = l
    }
}