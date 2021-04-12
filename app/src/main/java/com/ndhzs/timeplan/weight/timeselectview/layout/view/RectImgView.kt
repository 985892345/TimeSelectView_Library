package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.ParentLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectDraw
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectImgView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [com.ndhzs.timeplan.weight.timeselectview.layout.ScrollLayout]之下
 */
@SuppressLint("ViewConstructor")
class RectImgView(context: Context, iRectImgView: IRectImgView, data: TSViewInternalData, time: ITSViewTime, draw: IRectDraw) : View(context) {

    /**
     * 设置将要绘制的Rect和Bean，其中Rect会自动转换坐标系
     * (记得调用over()结束)
     */
    fun start(rect: Rect, bean: TSViewBean, position: Int) {
        mRect.left = rect.left + mIRectImgView.getRectViewToRectImgViewDistance(position)
        mRect.top = rect.top - mIRectImgView.getScrollY()
        mRect.right = rect.right + mIRectImgView.getRectViewToRectImgViewDistance(position)
        mRect.bottom = rect.bottom - mIRectImgView.getScrollY()
        mBean = bean
        invalidate()
    }

    fun over(outerRectTop: Int, outerRectLeft: Int, onEndListener : () -> Unit) {
        val dTop = mRect.top - outerRectTop
        val dLeft = mRect.left - outerRectLeft
        val rectWidth = mRect.width()
        val rectHeight = mRect.height()
        val animator = ValueAnimator.ofInt(mRect.top, outerRectTop)
        animator.addUpdateListener {
            mRect.top = it.animatedValue as Int
            mRect.left = ((mRect.top - outerRectTop) / dTop.toFloat() * dLeft + outerRectLeft).roundToInt()
            mRect.bottom = mRect.top + rectWidth
            mRect.right = mRect.left + rectHeight
            invalidate()
        }
        animator.addListener(onEnd = {
            mRect.setEmpty()
            onEndListener.invoke()
        })
        animator.duration = sqrt((dTop * dTop + dLeft * dLeft) * 0.6).toLong()
        animator.interpolator = OvershootInterpolator()
        animator.start()
    }

    fun getInsideTop(): Int {
        return mRect.top
    }

    fun getInsideBottom(): Int {
        return mRect.bottom
    }

    fun slideRectImgView(x: Int, y: Int) {

    }

    private val mData = data
    private val mDraw = draw
    private val mTime = time
    private val mIRectImgView = iRectImgView
    private val mRect = Rect()
    private lateinit var mBean: TSViewBean
    private val mDividerLines = IntArray(data.mTSViewAmount + 1)

    companion object {
        private const val X_KEEP_THRESHOLD = 30
    }

    override fun onDraw(canvas: Canvas) {
        if (!mRect.isEmpty) {
            mDraw.drawRect(canvas, mRect, mBean.name, mBean.borderColor, mBean.insideColor)
            val dividerLines = mIRectImgView.getDividerLines()
            if (mData.mTSViewAmount > 1) {
                val top = mRect.top + mIRectImgView.getScrollY()
                val bottom = mRect.bottom + mIRectImgView.getScrollY()
                if (mRect.left < mIRectImgView.getRectViewToRectImgViewDistance(1) - mData.mIntervalLeft) {
                    mDraw.drawStartEndTime(canvas, mRect, mTime.getTime(top), mTime.getTime(bottom))
                }else {
                    if (mData.mTSViewAmount > 2) {
                        if ()
                    }
                    mDraw.drawStartEndTime(canvas, mRect, mTime.getTime(mRect.top ), mTime.getTime(mRect.bottom))
                }
            }

            if (mData.mIsShowDiffTime) {
                mDraw.drawArrows(canvas, mRect, mBean.diffTime)
            }
        }
    }
}