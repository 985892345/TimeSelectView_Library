package com.ndhzs.timeplan.weight.timeselectview.utils

import android.content.Context
import android.util.AttributeSet
import com.ndhzs.timeplan.R

class TimeSelectViewUtil(context: Context, attrs: AttributeSet? = null) {

    val mTimeUtil = TSViewTimeUtil(this)
    val mEndHour: Int  //结束时间
    val mStartHour:Int  //起始时间
    val mCenterTime: Float //当前时间线，支持小数
    val mBorderColor:Int //矩形边框颜色
    val mInsideColor:Int //矩形内部颜色
    val mExtraHeight:Int //上方和下方多的高度
    val mIntervalLeft: Int //左边的文字间隔宽度
    val mIntervalHeight: Int //一个小时的间隔高度
    val mTotalHeight: Int //总高度
    val mTimeTextSize: Float //时间字体大小
    val mTaskTextSize: Float //任务字体大小
    var mIsShowDiffTime: Boolean //最终的任务区域是否显示时间差
    var mIsShowTopBottomTime: Boolean //最终的任务区域是否显示上下边界时间

    init {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView)
        mEndHour = ty.getInteger(R.styleable.TimeSelectView_endHour, 26)
        mStartHour = ty.getInteger(R.styleable.TimeSelectView_startHour, 2)
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, -1f)
        mBorderColor = ty.getColor(R.styleable.TimeSelectView_borderColor, 0xFF0000)
        mInsideColor = ty.getColor(R.styleable.TimeSelectView_insideColor, 0xDCCC48)
        mIntervalLeft = ty.getDimension(R.styleable.TimeSelectView_intervalLeft, 110f).toInt()
        mIntervalHeight = ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 194f).toInt()
        mTimeTextSize = ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 40f)
        mTaskTextSize = ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 40f)
        mIsShowDiffTime = ty.getBoolean(R.styleable.TimeSelectView_isShowDiffTime, false)
        mIsShowTopBottomTime = ty.getBoolean(R.styleable.TimeSelectView_isShowTopBottomTime, true)
        mExtraHeight = mIntervalHeight/2
        mTotalHeight = (mEndHour - mStartHour) * mIntervalHeight + 2 * mExtraHeight
        ty.recycle()
    }
}