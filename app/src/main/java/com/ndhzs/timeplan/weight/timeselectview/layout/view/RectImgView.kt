package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
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
     * 整体移动开始时调用
     * @param rect 内部坐标值的Rect
     */
    fun start(rect: Rect, bean: TSViewBean, position: Int) {
        mPosition = position
        val distance = mIRectImgView.getRectViewToRectImgViewDistance(position)
        mRect.left = rect.left + distance
        mRect.top = rect.top
        mRect.right = rect.right + distance
        mRect.bottom = rect.bottom
        mBean = bean
        for (i in mDividerLines.indices) {
            if (i == 0) {
                mDividerLines[i] = Int.MIN_VALUE
            }else if (i == mDividerLines.size - 1) {
                mDividerLines[i] = Int.MAX_VALUE
            }else {
                mDividerLines[i] = mIRectImgView.getRectViewToRectImgViewDistance(i) - mData.mIntervalLeft
            }
        }
        invalidate()
    }

    /**
     * 整体移动结束时调用
     */
    fun over(rawFinalLeft: Int, insideFinalTop: Int, onEndListener : () -> Unit?) {
        val location = IntArray(2)
        getLocationInWindow(location)
        val insideFinalLeft = rawFinalLeft - location[0]
        val dTop = mRect.top - insideFinalTop
        val dLeft = mRect.left - insideFinalLeft
        val rectWidth = mRect.width()
        val rectHeight = mRect.height()
        val animator = ValueAnimator.ofInt(mRect.top, insideFinalTop)
        animator.addUpdateListener {
            mRect.top = it.animatedValue as Int
            mRect.left = ((mRect.top - insideFinalTop) / dTop.toFloat() * dLeft + insideFinalLeft).roundToInt()
            mRect.bottom = mRect.top + rectWidth
            mRect.right = mRect.left + rectHeight
            invalidate()
        }
        animator.addListener(onEnd = {
            onEndListener.invoke()
            mRect.setEmpty()
            invalidate()
        })
        animator.duration = sqrt((dTop * dTop + dLeft * dLeft) * 0.6).toLong()
        animator.interpolator = OvershootInterpolator()
        animator.start()
    }

    /**
     * 整体移动到删除区域时调用，会有一个删除动画
     */
    fun delete(onEndListener : () -> Unit?) {
        val animator = ValueAnimator.ofInt(mRect.width(), 0)
        animator.addUpdateListener {
            val dWidth = it.animatedValue as Int
            val dHeight = (dWidth / mRect.width().toFloat() * mRect.height()).roundToInt()
            mRect.left += dWidth/2
            mRect.right -= dWidth/2
            mRect.top += dHeight/2
            mRect.bottom -= dHeight/2
            invalidate()
        }
        animator.addListener(onEnd = {
            onEndListener.invoke()
            mRect.setEmpty()
            invalidate()
        })
        animator.duration = 350
        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }

    /**
     * 返回整体移动矩形的顶部高度值，为内部坐标值
     * @return 返回内部坐标值
     */
    fun getInsideTop(): Int {
        return mRect.top
    }

    /**
     * 返回整体移动矩形的底部高度值，为内部坐标值
     * @return 返回内部坐标值
     */
    fun getInsideBottom(): Int {
        return mRect.bottom
    }

    /**
     * 返回left、right为相对于屏幕的值，top、bottom为insideY值的Rect
     * @return Rect的left、right值是相对于屏幕的值，top、bottom为内部值
     */
    fun getRawRect(): Rect {
        val location = IntArray(2)
        getLocationInWindow(location)
        return Rect(mRect.left + location[0], mRect.top, mRect.right + location[0], mRect.bottom)
    }

    /**
     * 整体滑动时调用
     * @param dx 大于0向右移动
     * @param dy 大于0向右移动
     */
    fun slideRectImgView(dx: Int, dy: Int) {
        mRect.left += dx
        mRect.top += dy
        mRect.right += dx
        mRect.bottom += dy
        invalidate()
    }

    private val mData = data
    private val mDraw = draw
    private val mTime = time
    private val mIRectImgView = iRectImgView
    private val mRect = Rect()
    private var mPosition = 0
    private lateinit var mBean: TSViewBean
    private val mDividerLines = IntArray(data.mTSViewAmount + 1)

    companion object {
        private const val X_KEEP_THRESHOLD = 30
    }

    override fun onDraw(canvas: Canvas) {
        if (!mRect.isEmpty) {
            mDraw.drawRect(canvas, mRect, mBean.name, mBean.borderColor, mBean.insideColor)
            if (mData.mTSViewAmount > 1) {
                val top = mRect.top
                val bottom = mRect.bottom
                for (i in 0 until mDividerLines.size - 2) {
                    if (mRect.left in mDividerLines[i]..mDividerLines[i + 1]) {
                        mDraw.drawStartEndTime(canvas, mRect, mTime.getTime(top, i), mTime.getTime(bottom, i))
                    }
                }
            }
            if (mData.mIsShowDiffTime) {
                mDraw.drawArrows(canvas, mRect, mBean.diffTime)
            }
        }
    }
}