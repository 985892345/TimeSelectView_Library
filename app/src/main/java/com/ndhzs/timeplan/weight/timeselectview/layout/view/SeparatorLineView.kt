package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import com.ndhzs.timeplan.R
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout]之下
 */
@SuppressLint("ViewConstructor")
class SeparatorLineView(context: Context, data: TSViewInternalData, position: Int) : View(context) {
    companion object {
        /**
         * 垂直分割线厚度
         */
        const val VERTICAL_LINE_WIDTH = 2

        /**
         * 水平分割线厚度
         */
        const val HORIZONTAL_LINE_WIDTH = 1
    }

    private val mData = data
    private val mPosition = position
    private val mVLinePaint = Paint() //Vertical Line 垂直线画笔
    private val mHLinePaint = Paint() //Horizontal Line 水平线画笔
    private val mLeftTimePaint = Paint() //左侧时间画笔
    private val mLeftTimeCenter: Float

    init {
        //Vertical Line 垂直线画笔
        mVLinePaint.color = 0xFFC8C8C8.toInt()
        mVLinePaint.strokeWidth = VERTICAL_LINE_WIDTH.toFloat()

        //Horizontal Line 水平线画笔
        mHLinePaint.color = 0x809C9C9C.toInt()
        mHLinePaint.strokeWidth = HORIZONTAL_LINE_WIDTH.toFloat()

        //左侧时间画笔
        mLeftTimePaint.color = 0xFF505050.toInt()
        mLeftTimePaint.isAntiAlias = true
        mLeftTimePaint.textAlign = Paint.Align.CENTER
        mLeftTimePaint.textSize = mData.mTimeTextSize
        val fontMetrics = mLeftTimePaint.fontMetrics
        mLeftTimeCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }

    override fun onDraw(canvas: Canvas) {
        val startHour = mData.mTimeRangeArray[mPosition][0]
        val extraHeight = mData.mExtraHeight.toFloat()
        val intervalLeft = mData.mIntervalLeft.toFloat()
        val intervalRight = mData.mIntervalRight.toFloat()
        val intervalHeight = mData.mIntervalHeight.toFloat()
        val verticalLineX = intervalLeft - VERTICAL_LINE_WIDTH/2
        canvas.drawLine(verticalLineX, 0F, verticalLineX, mData.mInsideTotalHeight.toFloat(), mVLinePaint)
        for (i in mData.mTimeRangeArray[mPosition][0]..mData.mTimeRangeArray[mPosition][1]) {
            val hour = when {
                i < 10 -> {
                    "0$i:00"
                }i < 24 -> {
                    "$i:00"
                }else -> {
                    "0${i%24}:00"
                }
            }
            val y = extraHeight + intervalHeight * (i - startHour)
            val baseline = y + mLeftTimeCenter //时间文字相对于矩形的水平线高度
            canvas.drawText(hour, intervalLeft/2F, baseline, mLeftTimePaint)
            val lineHeight = y + HORIZONTAL_LINE_WIDTH/2F
            canvas.drawLine(intervalLeft, lineHeight, width - intervalRight, lineHeight, mHLinePaint)
        }
    }
}