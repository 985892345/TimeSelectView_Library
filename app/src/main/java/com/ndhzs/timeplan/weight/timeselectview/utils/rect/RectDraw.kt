package com.ndhzs.timeplan.weight.timeselectview.utils.rect

import android.graphics.*
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectDraw
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来绘制任务的类
 */
class RectDraw(data: TSViewInternalData) : IRectDraw {

    private val mData = data
    private val mTextPaint: Paint //任务名称画笔
    private val mDTimePaint: Paint //时间差值画笔
    private val mInsidePaint: Paint //圆角矩形内部画笔
    private val mBorderPaint: Paint //圆角矩形边框画笔
    private val mArrowsPaint: Paint //时间差的箭头线画笔
    private val mTBTimePaint: Paint //上下线时间画笔
    private val mStartTimePaint: Paint //开始时间画笔
    private val mArrowsPath = Path() //箭头的路径
    private val mRectF = RectF()

    private val mTBTimeAscent: Float
    private val mTBTimeDescent: Float
    private val mRectMinHeight: Float //能生成任务的最小高度
    private val mRectShowTBTimeHeight: Float //显示顶部和底部的最小高度
    private val mRectShowStartTimeHeight: Float //显示开始时间的最小高度
    private val mTextCenter: Float //任务名称的水平线
    private var mDTimeCenter: Float //时间差值的水平线
    private val mDTimeHalfHeight: Float //右侧时间的字体高度的一半

    companion object {
        private const val BORDER_WIDTH = 4 //圆角矩形边框厚度
        private const val BORDER_RADIUS = 8F //圆角矩形的圆角半径
    }

    init {
        mArrowsPaint = generatePaint(0x000000)
        mInsidePaint = generatePaint(mData.mDefaultInsideColor)
        mBorderPaint = generatePaint(mData.mDefaultBorderColor, BORDER_WIDTH, Paint.Style.STROKE)
        mTextPaint = generateTextPaint(mData.mTaskTextSize)
        mDTimePaint = generateTextPaint(0.7F * mData.mTimeTextSize, Paint.Align.RIGHT)
        mTBTimePaint = generateTextPaint(0.8F * mData.mTimeTextSize, Paint.Align.LEFT)
        mStartTimePaint = generateTextPaint(0.8F * mData.mTimeTextSize)

        var fontMetrics = mTextPaint.fontMetrics
        mTextCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        mRectMinHeight = fontMetrics.descent - fontMetrics.ascent
        fontMetrics = mTBTimePaint.fontMetrics
        mTBTimeAscent = fontMetrics.ascent
        mTBTimeDescent = fontMetrics.descent
        mRectShowTBTimeHeight = (mTBTimeDescent - mTBTimeAscent) * 2
        fontMetrics = mStartTimePaint.fontMetrics
        mRectShowStartTimeHeight = fontMetrics.descent - fontMetrics.ascent
        fontMetrics = mDTimePaint.fontMetrics
        mDTimeHalfHeight = (fontMetrics.bottom - fontMetrics.top) / 2
        mDTimeCenter = mDTimeHalfHeight - fontMetrics.bottom
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

    override fun getMinHeight(): Float = mRectMinHeight

    override fun drawRect(canvas: Canvas, rect: Rect, name: String, borderColor: Int, insideColor: Int) {
        mBorderPaint.color = borderColor
        mInsidePaint.color = insideColor
        val l = rect.left + BORDER_WIDTH / 2F
        val t = rect.top + BORDER_WIDTH / 2F
        val r = rect.right - BORDER_WIDTH / 2F
        val b = rect.bottom - BORDER_WIDTH / 2F
        mRectF.set(l, t, r, b)
        canvas.drawRoundRect(mRectF, BORDER_RADIUS, BORDER_RADIUS, mInsidePaint)
        canvas.drawRoundRect(mRectF, BORDER_RADIUS, BORDER_RADIUS, mBorderPaint)
        canvas.drawText(name, mRectF.centerX(), mRectF.centerY() + mTextCenter, mTextPaint)
    }

    override fun drawArrows(canvas: Canvas, rect: Rect, dTime: String) {
        if (rect.height() > mRectMinHeight) {
            val timeRight = rect.right - BORDER_WIDTH - 1F
            canvas.drawText(dTime, timeRight, rect.centerY() + mDTimeCenter, mDTimePaint)
            val horizontalInterval = BORDER_WIDTH
            val verticalInterval = BORDER_WIDTH * 2
            val verticalCenter = rect.right - 24
            val l = verticalCenter - horizontalInterval
            val t = rect.top + BORDER_WIDTH
            val r = verticalCenter + horizontalInterval
            val b = rect.bottom - BORDER_WIDTH
            mArrowsPath.moveTo(verticalCenter.toFloat(), t.toFloat())
            mArrowsPath.lineTo(verticalCenter.toFloat(), rect.centerY() - mDTimeHalfHeight)
            mArrowsPath.moveTo(verticalCenter.toFloat(), rect.centerY() + mDTimeHalfHeight)
            mArrowsPath.lineTo(verticalCenter.toFloat(), b.toFloat())
            mArrowsPath.moveTo(l.toFloat(), (t + verticalInterval).toFloat())
            mArrowsPath.lineTo(verticalCenter.toFloat(), t.toFloat())
            mArrowsPath.lineTo(r.toFloat(), (t + verticalInterval).toFloat())
            mArrowsPath.moveTo(l.toFloat(), (b - verticalInterval).toFloat())
            mArrowsPath.lineTo(verticalCenter.toFloat(), b.toFloat())
            mArrowsPath.lineTo(r.toFloat(), (b - verticalInterval).toFloat())
            canvas.drawPath(mArrowsPath, mArrowsPaint)
            mArrowsPath.rewind()
        }
    }

    override fun drawStartTime(canvas: Canvas, rect: Rect, sTime: String) {
        val t = rect.top + BORDER_WIDTH / 2F
        canvas.drawText(sTime, mRectF.centerX(), t - mTBTimeAscent, mStartTimePaint)
    }

    override fun drawStartEndTime(canvas: Canvas, rect: Rect, sTime: String, eTime: String) {
        if (rect.height() > mRectShowTBTimeHeight) {
            val l = rect.left + BORDER_WIDTH + 1F
            val t = rect.top - mTBTimeAscent
            val b = rect.bottom - mTBTimeDescent
            canvas.drawText(sTime, l, t, mTBTimePaint)
            canvas.drawText(eTime, l, b, mTBTimePaint)
        }
    }
}