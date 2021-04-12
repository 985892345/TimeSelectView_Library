package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewTimeUtil
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
@SuppressLint("ViewConstructor")
class NowTimeLineView(context: Context, data: TSViewInternalData, time: ITSViewTime, position: Int) : View(context) {

    private val mLineWidth = 3F
    private val mBallRadius = 7
    private val mTime = time
    private val mPosition = position
    private val mIntervalLeft = data.mIntervalLeft
    private val mTimeLinePaint: Paint = Paint()

    init {
        mTimeLinePaint.color = 0xFFE40000.toInt()
        mTimeLinePaint.isAntiAlias = true
        mTimeLinePaint.strokeWidth = mLineWidth
        mTimeLinePaint.style = Paint.Style.FILL
        postDelayed(object : Runnable {
            override fun run() {
                layout(0, 0, 0, 0)
                postDelayed(this, TSViewTimeUtil.DELAY_NOW_TIME_REFRESH)
            }
        }, TSViewTimeUtil.DELAY_NOW_TIME_REFRESH)
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        /*
        * 为了防止因父布局调用addView()后重新layout()而回到原位置
        * */
        val nowTimeHeight = mTime.getNowTimeHeight(mPosition)
        super.layout(l, nowTimeHeight - mBallRadius, r, nowTimeHeight + mBallRadius)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(2 * mBallRadius, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = mIntervalLeft - SeparatorLineView.VERTICAL_LINE_WIDTH/2F
        val cy = mBallRadius.toFloat()
        val stopX = (width - SeparatorLineView.INTERVAL_RIGHT_WIDTH).toFloat()
        canvas.drawCircle(cx, cy, cy, mTimeLinePaint)
        canvas.drawLine(cx, cy, stopX, cy, mTimeLinePaint)
    }
}