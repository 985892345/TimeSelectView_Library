package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import com.ndhzs.timeplan.weight.timeselectview.utils.TimeSelectViewUtil

@SuppressLint("ViewConstructor")
class RectView(context: Context, util: TimeSelectViewUtil) : View(context) {

    private val mUtil = util
    private val mTimeUtil = util.mTimeUtil
    private val mTextPaint: Paint //任务名称画笔
    private val mDTimePaint: Paint //时间差值画笔
    private val mInsidePaint: Paint //圆角矩形内部画笔
    private val mBorderPaint: Paint //圆角矩形边框画笔
    private val mArrowsPaint: Paint //时间差的箭头线画笔
    private val mTBTimePaint: Paint //上下线时间画笔
    private val mStartTimePaint: Paint //开始时间画笔
    private val mArrowsPath = Path() //箭头的路径

    private val mTBTimeAscent: Float
    private val mTBTimeDescent: Float
    private val mRectMinHeight: Float
    private val mRectLesserHeight: Float
    private val mRectShowStartTimeHeight: Float

    companion object {
        private const val BORDER_WIDTH = 4 //圆角矩形边框厚度
        private const val BORDER_RADIUS = 8 //圆角矩形的圆角半径
    }

    init {
        mArrowsPaint = generatePaint(0x000000)
        mInsidePaint = generatePaint(util.mInsideColor)
        mBorderPaint = generatePaint(util.mBorderColor, BORDER_WIDTH, Paint.Style.STROKE)
        mTextPaint = generateTextPaint(util.mTaskTextSize)
        mDTimePaint = generateTextPaint(0.7F * util.mTimeTextSize, Paint.Align.RIGHT)
        mTBTimePaint = generateTextPaint(0.8F * util.mTimeTextSize, Paint.Align.LEFT)
        mStartTimePaint = generateTextPaint(0.8F * util.mTimeTextSize)

        var fontMetrics = mTextPaint.fontMetrics
        mRectMinHeight = fontMetrics.descent - fontMetrics.ascent
        fontMetrics = mTBTimePaint.fontMetrics
        mTBTimeAscent = fontMetrics.ascent
        mTBTimeDescent = fontMetrics.descent
        mRectLesserHeight = (mTBTimeDescent - mTBTimeAscent) * 2
        fontMetrics = mStartTimePaint.fontMetrics
        mRectShowStartTimeHeight = fontMetrics.descent - fontMetrics.ascent
    }

    private fun generatePaint(color: Int, strokeWidth: Int = 2, style: Paint.Style = Paint.Style.FILL): Paint {
        val paint = Paint()
        paint.style = style
        paint.color = color
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth.toFloat()
        return paint
    }

    private fun generateTextPaint(textSize: Float, align: Paint.Align = Paint.Align.CENTER): Paint {
        val paint = Paint()
        paint.color = 0x000000
        paint.textSize = textSize
        paint.textAlign = align
        paint.isAntiAlias = true
        return paint
    }

    override fun onDraw(canvas: Canvas) {
    }
}