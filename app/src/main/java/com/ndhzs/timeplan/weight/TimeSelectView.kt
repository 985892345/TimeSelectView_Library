package com.ndhzs.timeplan.weight

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import com.ndhzs.timeplan.R

class TimeSelectView(context: Context, attrs: AttributeSet? = null) : ScrollView(context, attrs) {

    private val mEndHour: Int//结束时间
    private val mStartHour: Int//起始时间
    private val mBorderColor: Int //矩形边框颜色
    private val mInsideColor: Int //矩形内部颜色
    private val mExtraHeight: Int //上方和下方多的高度
    private val mIntervalLeft: Int //左边的文字间隔宽度
    private val mIntervalHeight: Int //一个小时的间隔高度
    private val mCenterTime: Float//当前时间线，支持小数
    private val mTimeTextSide: Float //时间字体大小
    private val mTaskTextSize: Float //任务字体大小
    private var mIsShowDiffTime = false
    private var mIsShowTopBottomTime: Boolean = true

    init {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView)
        mEndHour = ty.getInteger(R.styleable.TimeSelectView_endHour, 26)
        mStartHour = ty.getInteger(R.styleable.TimeSelectView_startHour, 2)
        mBorderColor = ty.getColor(R.styleable.TimeSelectView_borderColor, 0xFFFF0000.toInt())
        mInsideColor = ty.getColor(R.styleable.TimeSelectView_insideColor, 0xFFDCCC48.toInt())
        mIntervalLeft = ty.getDimension(R.styleable.TimeSelectView_intervalLeft, 110f).toInt()
        mIntervalHeight = ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 194f).toInt()
        mExtraHeight = (mIntervalHeight * 0.5f).toInt()
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, -1f)
        mTimeTextSide = ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 40f)
        mTaskTextSize = ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 40f)
        mIsShowDiffTime = ty.getBoolean(R.styleable.TimeSelectView_isShowDiffTime, false)
        mIsShowTopBottomTime = ty.getBoolean(R.styleable.TimeSelectView_isShowTopBottomTime, true)
        ty.recycle()
    }
}