package com.ndhzs.timeselectview.utils.rect

import android.graphics.*
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.viewinterface.IRectDraw

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来绘制任务的类
 */
internal class RectDraw(
    attrs: TSViewAttrs
) : IRectDraw {

    private val mAttrs = attrs
    private val mTextPaint: Paint //任务名称画笔
    private val mDTimePaint: Paint //时间差值画笔
    private val mInsidePaint: Paint //圆角矩形内部画笔
    private val mBorderPaint: Paint //圆角矩形边框画笔
    private val mArrowsPaint: Paint //时间差的箭头线画笔
    private val mStartEndTimePaint: Paint //上下线时间画笔
    private val mArrowsPath = Path() //箭头的路径
    private val mRectF = RectF()

    private val mTBTimeAscent: Float
    private val mTBTimeDescent: Float
    private val mRectMinHeight: Float //能生成任务的最小高度
    private val mRectShowStartEndTimeHeight: Float //显示顶部和底部的最小高度
    private val mTextCenter: Float //任务名称的水平线
    private val mDTimeCenter: Float //时间差值的水平线
    private val mDTimeHalfHeight: Float //右侧时间的字体高度的一半

    companion object {
        private const val BORDER_WIDTH = 4 //圆角矩形边框厚度
        private const val BORDER_RADIUS = 8F //圆角矩形的圆角半径
        private const val DIFF_TIME_MULTIPLE = 0.75F //右侧时间差的文字相对于 TimeTextSize 的倍数
        private const val START_END_TIME_MULTIPLE = 0.8F //开始结束时间的文字相对于 TimeTextSize 的倍数
    }

    init {
        mArrowsPaint = generatePaint(0xFF000000.toInt(), 2, Paint.Style.STROKE)
        mInsidePaint = generatePaint(mAttrs.mDefaultInsideColor)
        mBorderPaint = generatePaint(mAttrs.mDefaultBorderColor, BORDER_WIDTH, Paint.Style.STROKE)
        mTextPaint = generateTextPaint(mAttrs.mTaskTextSize)
        mDTimePaint = generateTextPaint(DIFF_TIME_MULTIPLE * mAttrs.mTimeTextSize, Paint.Align.RIGHT)
        mStartEndTimePaint = generateTextPaint(START_END_TIME_MULTIPLE * mAttrs.mTimeTextSize, Paint.Align.LEFT)

        var fontMetrics = mTextPaint.fontMetrics
        mTextCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        mRectMinHeight = fontMetrics.descent - fontMetrics.ascent

        fontMetrics = mStartEndTimePaint.fontMetrics
        mTBTimeAscent = fontMetrics.ascent
        mTBTimeDescent = fontMetrics.descent
        mRectShowStartEndTimeHeight = (mTBTimeDescent - mTBTimeAscent) * 1.8F

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
        paint.color = 0xFF000000.toInt()
        paint.textSize = textSize
        paint.textAlign = align
        paint.isAntiAlias = true
        return paint
    }

    override fun getMinHeight(): Float {
        return mRectMinHeight
    }

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
        if (rect.height() > mRectMinHeight) {
            canvas.drawText(name, mRectF.centerX(), mRectF.centerY() + mTextCenter, mTextPaint)
        }
    }

    override fun drawRect(canvas: Canvas, rect: Rect, name: String, borderColor: Int, insideColor: Int, nameSize: Float) {
        mTextPaint.textSize = nameSize
        drawRect(canvas, rect, name, borderColor, insideColor)
        mTextPaint.textSize = mAttrs.mTaskTextSize
    }

    override fun drawArrows(canvas: Canvas, rect: Rect, dTime: String) {
        if (rect.height() > mRectMinHeight) {
            val timeRight = rect.right - BORDER_WIDTH - 1F
            canvas.drawText(dTime, timeRight, rect.centerY() + mDTimeCenter, mDTimePaint)
            val horizontalInterval = BORDER_WIDTH
            val verticalInterval = BORDER_WIDTH * 2
            val centerX = rect.right - 24
            val l = centerX - horizontalInterval
            val t = rect.top + verticalInterval
            val r = centerX + horizontalInterval
            val b = rect.bottom - verticalInterval
            mArrowsPath.moveTo(centerX.toFloat(), t.toFloat())
            mArrowsPath.lineTo(centerX.toFloat(), rect.centerY() - mDTimeHalfHeight)

            mArrowsPath.moveTo(centerX.toFloat(), rect.centerY() + mDTimeHalfHeight)
            mArrowsPath.lineTo(centerX.toFloat(), b.toFloat())

            mArrowsPath.moveTo(l.toFloat(), (t + verticalInterval).toFloat())
            mArrowsPath.lineTo(centerX.toFloat(), t.toFloat())
            mArrowsPath.lineTo(r.toFloat(), (t + verticalInterval).toFloat())

            mArrowsPath.moveTo(l.toFloat(), (b - verticalInterval).toFloat())
            mArrowsPath.lineTo(centerX.toFloat(), b.toFloat())
            mArrowsPath.lineTo(r.toFloat(), (b - verticalInterval).toFloat())
            canvas.drawPath(mArrowsPath, mArrowsPaint)
            mArrowsPath.rewind()
        }
    }

    override fun drawArrows(canvas: Canvas, rect: Rect, dTime: String, timeSize: Float) {
        mDTimePaint.textSize = timeSize * DIFF_TIME_MULTIPLE
        drawArrows(canvas, rect, dTime)
        mDTimePaint.textSize = mAttrs.mTimeTextSize * DIFF_TIME_MULTIPLE
    }

    override fun drawStartEndTime(canvas: Canvas, rect: Rect, sTime: String, eTime: String) {
        if (rect.height() > mRectShowStartEndTimeHeight) {
            val l = rect.left + BORDER_WIDTH + 1F
            val t = rect.top - mTBTimeAscent
            val b = rect.bottom - mTBTimeDescent
            canvas.drawText(sTime, l, t, mStartEndTimePaint)
            canvas.drawText(eTime, l, b, mStartEndTimePaint)
        }
    }

    override fun drawStartEndTime(canvas: Canvas, rect: Rect, sTime: String, eTime: String, timeSize: Float) {
        mStartEndTimePaint.textSize = timeSize * START_END_TIME_MULTIPLE
        drawStartEndTime(canvas, rect, sTime, eTime)
        mStartEndTimePaint.textSize = mAttrs.mTimeTextSize * START_END_TIME_MULTIPLE
    }
}