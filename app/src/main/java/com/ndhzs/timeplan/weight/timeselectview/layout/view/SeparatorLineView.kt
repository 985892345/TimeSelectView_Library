package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.ndhzs.timeplan.weight.timeselectview.utils.TimeSelectViewUtil

@SuppressLint("ViewConstructor")
class SeparatorLineView(context: Context, util: TimeSelectViewUtil) : View(context) {
    companion object {
        /**
         * 右边的间隔宽度
         */
        const val INTERVAL_RIGHT = 10

        /**
         * 垂直分割线厚度
         */
        const val VERTICAL_LINE_WIDTH = 2

        /**
         * 水平分割线厚度
         */
        const val HORIZONTAL_LINE_WIDTH = 1
    }

    private val mUtil = util
    private val mVLinePaint: Paint = Paint() //Vertical Line 垂直线画笔
    private val mHLinePaint: Paint = Paint() //Horizontal Line 水平线画笔
    private val mLeftTimePaint: Paint = Paint() //左侧时间画笔
    private val mLeftTimeCenter: Float

    init {
        //Vertical Line 垂直线画笔
        mVLinePaint.color = 0x373738
        mVLinePaint.strokeWidth = VERTICAL_LINE_WIDTH.toFloat()

        //Horizontal Line 水平线画笔
        mHLinePaint.color = 0x636364
        mHLinePaint.strokeWidth = HORIZONTAL_LINE_WIDTH.toFloat()

        //左侧时间画笔
        mLeftTimePaint.color = 0xAFAFB0
        mLeftTimePaint.isAntiAlias = true
        mLeftTimePaint.textAlign = Paint.Align.CENTER
        mLeftTimePaint.textSize = util.mTimeTextSize
        val fontMetrics = mLeftTimePaint.fontMetrics
        mLeftTimeCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }

    override fun onDraw(canvas: Canvas) {
        val startHour = mUtil.mStartHour
        val extraHeight = mUtil.mExtraHeight
        val intervalLeft = mUtil.mIntervalLeft
        val intervalHeight = mUtil.mIntervalHeight
        val vLineX = (intervalHeight - VERTICAL_LINE_WIDTH/2).toFloat()
        canvas.drawLine(vLineX, 0F, vLineX, mUtil.mTotalHeight.toFloat(), mVLinePaint)
        for (i in mUtil.mStartHour..mUtil.mEndHour) {
            val hour = when {
                i < 10 -> {
                    "0$i:00"
                }i < 24 -> {
                    "$i:00"
                }else -> {
                    "0${i%24}:00"
                }
            }
            val y = (extraHeight + intervalHeight * (i - startHour)).toFloat()
            val baseline = y + mLeftTimeCenter //时间文字相对于矩形的水平线高度
            canvas.drawText(hour, intervalLeft/2F, baseline, mLeftTimePaint)
            val lineHeight = y + HORIZONTAL_LINE_WIDTH/2F
            canvas.drawLine(intervalLeft.toFloat(), lineHeight, (width - INTERVAL_RIGHT).toFloat(), lineHeight, mHLinePaint)
        }
    }
}