package com.ndhzs.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.ndhzs.timeselectview.utils.RunnableManger
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.utils.TSViewTimeUtil
import com.ndhzs.timeselectview.viewinterface.ITSViewTimeUtil

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
@SuppressLint("ViewConstructor")
internal class NowTimeLineView(
        context: Context,
        private val attrs: TSViewAttrs,
        private val time: ITSViewTimeUtil,
        private val position: Int
) : View(context) {

    private val mLineWidth = 3F
    private val mBallRadius = 7
    private val mIntervalLeft = attrs.mIntervalLeft
    private val mTimeLinePaint = Paint()
    private val mRunnableManger = RunnableManger(this)

    init {
        mTimeLinePaint.color = 0xFFE40000.toInt()
        mTimeLinePaint.isAntiAlias = true
        mTimeLinePaint.strokeWidth = mLineWidth
        mTimeLinePaint.style = Paint.Style.FILL
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        /*
        * 为了防止因父布局调用addView()后重新layout()而回到原位置
        * */
        var nowTimeHeight = time.getNowTimeHeight(position)
        if (nowTimeHeight == -1) {
            nowTimeHeight = -100
        }
        super.layout(l, nowTimeHeight - mBallRadius, r, nowTimeHeight + mBallRadius)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(2 * mBallRadius, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = mIntervalLeft - SeparatorLineView.VERTICAL_LINE_WIDTH/2F
        val cy = mBallRadius.toFloat()
        val stopX = (width - attrs.mIntervalRight).toFloat()
        canvas.drawCircle(cx, cy, cy, mTimeLinePaint)
        canvas.drawLine(cx, cy, stopX, cy, mTimeLinePaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mRunnableManger.postDelayed(TSViewTimeUtil.DELAY_NOW_TIME_REFRESH, object : Runnable {
            override fun run() {
                layout(left, 0, right, 0)
                postDelayed(this, TSViewTimeUtil.DELAY_NOW_TIME_REFRESH)
            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mRunnableManger.destroy()
    }
}