package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectDraw
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick.*
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectViewRectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout]之下
 */
@SuppressLint("ViewConstructor")
class RectView(context: Context, data: TSViewInternalData,
               time: ITSViewTime, draw: IRectDraw,
               iRectViewRectManger: IRectViewRectManger,
               iRectView: IRectView, position: Int) : View(context) {

    /**
     * 用于在TimeScrollView滑动过程和自身MOVE事件中绘制矩形，此时长按已经确认，所以Rect的起始高度已经确定。
     *
     * 注意：只有在自身处于触摸时调用该方法才有效。
     *
     * @param rectInsideY Rect结束的高度
     */
    fun slideDrawRect(rectInsideY: Int) {
        if (rectInsideY > mInitialSideY) {
            mInitialRect.top = mInitialSideY
            mInitialRect.bottom = min(rectInsideY, mLowerLimit)
        }else {
            mInitialRect.top = max(rectInsideY, mUpperLimit)
            mInitialRect.bottom = mInitialSideY - 1 // 减1是CorrectTopHeight与CorrectBottomHeight的差值
        }
        invalidate()
    }

    /**
     * 通知RectView的所有矩形重新绘制，一般用于在设置任务显示的颜色、时间等属性时调用
     */
    fun notifyRectRedraw() {
        mRectWithBean = mIRectManger.getRectWithBeanMap()
        invalidate()
    }

    /**
     * 让RectView增加一个之前被删除的矩形，但位置已经改变，用的原来的Bean
     */
    fun addRectFromDeleted(rect: Rect) {
        mIRectManger.addRectFromDeleted(rect)
        notifyRectRedraw()
    }

    /**
     * 用于在长按后设置动态绘图的矩形和bean
     */
    fun clickTopAndBottomStart(initialRect: Rect, deletedBean: TSViewBean, initialSideY: Int, upperLimit: Int, lowerLimit: Int) {
        mInitialSideY = initialSideY
        mUpperLimit = upperLimit
        mLowerLimit = lowerLimit
        when (mData.mCondition) {
            TOP, BOTTOM -> {
                mInitialRect.set(initialRect)
                mDeletedBean = deletedBean
            }
            else -> {}
        }
        mIRectView.notifyAllRectViewRefresh()
    }

    /**
     * 设置开始点击的Rect的某一边的高度值和上下限值
     */
    fun clickEmptyStart(initialSideY: Int, upperLimit: Int, lowerLimit: Int) {
        mInitialSideY = mTime.getCorrectTopHeight(initialSideY, upperLimit, mPosition)
        mUpperLimit = upperLimit
        mLowerLimit = lowerLimit
    }

    private val mData = data
    private val mTime = time
    private val mDraw = draw
    private val mIRectManger = iRectViewRectManger
    private val mIRectView = iRectView
    private val mPosition = position
    private val mInitialRect = Rect() //动态绘图的矩形
    private var mInitialSideY = 0
    private var mUpperLimit = 0
    private var mLowerLimit = 0
    private var mDeletedBean: TSViewBean? = null
    private var mRectWithBean: HashMap<Rect, TSViewBean>? = null

    override fun onDraw(canvas: Canvas) {
        if (!mInitialRect.isEmpty) {
            when (mData.mCondition) {
                EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                    val startTime = mTime.getTime(mInitialRect.top, mPosition)
                    val endTime = mTime.getTime(mInitialRect.bottom, mPosition)
                    val diffTime = mTime.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                    mDraw.drawRect(canvas, mInitialRect, mData.mDefaultTaskName, mData.mDefaultBorderColor, mData.mDefaultInsideColor)
                    mDraw.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
                    if (!mIsInRectChangeAnimator) {
                        mDraw.drawArrows(canvas, mInitialRect, diffTime)
                        mDraw.drawStartTime(canvas, mInitialRect, startTime)
                    }
                }
                TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN,
                BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                    val bean = mDeletedBean!!
                    val name = bean.name
                    val borderColor = bean.borderColor
                    val insideColor = bean.insideColor
                    val startTime = mTime.getTime(mInitialRect.top, mPosition)
                    val endTime = mTime.getTime(mInitialRect.bottom, mPosition)
                    val diffTime = mTime.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                    mDraw.drawRect(canvas, mInitialRect, name, borderColor, insideColor)
                    mDraw.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
                    if (!mIsInRectChangeAnimator) {
                        mDraw.drawArrows(canvas, mInitialRect, diffTime)
                    }
                }
                else -> {}
            }
        }
        mRectWithBean?.forEach {
            val rect = it.key
            val bean = it.value
            mDraw.drawRect(canvas, rect, bean.name, bean.borderColor, bean.insideColor)
            if (mData.mIsShowDiffTime) {
                mDraw.drawArrows(canvas, rect, bean.diffTime)
            }
            if (mData.mIsShowStartEndTime) {
                mDraw.drawStartEndTime(canvas, rect, bean.startTime, bean.endTime)
            }
        }
    }

    private var mInitialX = 0
    private var mInitialY = 0
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = x
                mInitialY = y
                mInitialRect.right = mData.mRectViewWidth
            }
            MotionEvent.ACTION_MOVE -> {
                when (mData.mCondition) {
                    TOP, BOTTOM, EMPTY_AREA -> {
                        slideDrawRect(y)
                    }
                    else -> {} //在TimeScrollView滑动时不在此处绘制矩形
                }
            }
            MotionEvent.ACTION_UP -> {
                when (mData.mCondition) {
                    TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN -> {
                        mIRectView.setIsCanLongClick(false)
                        val rect = getCorrectTopHeight(mInitialRect) {
                            rectChangeEnd()
                        }
                        if (rect != null) {
                            val bean = mDeletedBean!!
                            bean.startTime = mTime.getTime(rect.top, mPosition)
                            bean.diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            mIRectManger.addRectFromDeleted(rect)
                        }
                    }
                    BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                        mIRectView.setIsCanLongClick(false)
                        val rect = getCorrectBottomHeight(mInitialRect) {
                            rectChangeEnd()
                        }
                        if (rect != null) {
                            val bean = mDeletedBean!!
                            bean.endTime = mTime.getTime(rect.bottom, mPosition)
                            bean.diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            mIRectManger.addRectFromDeleted(rect)
                        }
                    }
                    EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                        mIRectView.setIsCanLongClick(false)
                        val rect = getCorrectEmptyRect(mInitialRect) {
                            rectChangeEnd()
                        }
                        if (rect != null) {
                            val name = mData.mDefaultTaskName
                            val startTime = mTime.getTime(rect.top, mPosition)
                            val endTime = mTime.getTime(rect.bottom, mPosition)
                            val diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            val borderColor = mData.mDefaultBorderColor
                            val insideColor = mData.mDefaultInsideColor
                            val bean = TSViewBean(name, startTime, endTime, diffTime, borderColor, insideColor)
                            mIRectManger.addNewRect(rect, bean)
                        }
                    }
                    else -> {}
                }
            }
        }
        return true
    }

    private fun rectChangeEnd() {
        mInitialRect.setEmpty()
        mInitialRect.right = mData.mRectViewWidth
        mIRectView.notifyAllRectViewRefresh()
        mData.mCondition = NULL
        mIRectView.setIsCanLongClick(true)
    }

    private var mIsInRectChangeAnimator = false
    private fun getCorrectTopHeight(rect: Rect, rectChangeEndCallbacks: () -> Unit): Rect? {
        val newRect = Rect()
        newRect.left = rect.left
        newRect.top = mTime.getCorrectTopHeight(rect.top, mUpperLimit, mPosition)
        newRect.right = rect.right
        newRect.bottom = rect.bottom
        if (newRect.height() > mDraw.getMinHeight()) {
            mIsInRectChangeAnimator = true
            val animator = ValueAnimator.ofInt(rect.top, newRect.top)
            animator.addUpdateListener {
                val top = it.animatedValue as Int
                mInitialRect.top = top
                invalidate()
            }
            animator.addListener(onEnd = {
                mIsInRectChangeAnimator = false
                rectChangeEndCallbacks.invoke()
            })
            animator.duration = 500
            animator.interpolator = OvershootInterpolator()
            animator.start()
        }else {
            rectChangeEndCallbacks.invoke()
            return null
        }
        return newRect
    }

    private fun getCorrectBottomHeight(rect: Rect, rectChangeEndCallbacks: () -> Unit): Rect? {
        val newRect = Rect()
        newRect.left = rect.left
        newRect.top = rect.top
        newRect.right = rect.right
        newRect.bottom = mTime.getCorrectBottomHeight(rect.bottom, mLowerLimit, mPosition)
        if (newRect.height() > mDraw.getMinHeight()) {
            mIsInRectChangeAnimator = true
            val animator = ValueAnimator.ofInt(rect.bottom, newRect.bottom)
            animator.addUpdateListener {
                val bottom = it.animatedValue as Int
                mInitialRect.bottom = bottom
                invalidate()
            }
            animator.addListener(onEnd = {
                mIsInRectChangeAnimator = false
                rectChangeEndCallbacks.invoke()
            })
            animator.duration = 500
            animator.interpolator = OvershootInterpolator()
            animator.start()
        }else {
            rectChangeEndCallbacks.invoke()
            return null
        }
        return newRect
    }

    private fun getCorrectEmptyRect(rect: Rect, rectChangeEndCallbacks: () -> Unit): Rect? {
        val newRect = Rect()
        newRect.left = rect.left
        newRect.top = mTime.getCorrectTopHeight(rect.top, mUpperLimit, mPosition)
        newRect.right = rect.right
        newRect.bottom = mTime.getCorrectBottomHeight(rect.bottom, mLowerLimit, mPosition)
        if (newRect.height() > mDraw.getMinHeight()) {
            mIsInRectChangeAnimator = true
            val dTop = rect.top - newRect.top
            val dBottom = rect.bottom - newRect.bottom
            val totalDistance = sqrt((dTop * dTop + dBottom * dBottom).toFloat())
            val animator = ValueAnimator.ofFloat(totalDistance, 0F)
            animator.addUpdateListener {
                val nowDistance = it.animatedValue as Float
                mInitialRect.top = (nowDistance / totalDistance * dTop).toInt() + newRect.top
                mInitialRect.bottom = (nowDistance / totalDistance * dBottom).toInt() + newRect.bottom
                invalidate()
            }
            animator.addListener(onEnd = {
                mIsInRectChangeAnimator = false
                rectChangeEndCallbacks.invoke()
            })
            animator.duration = 500
            animator.interpolator = OvershootInterpolator()
            animator.start()
        }else {
            rectChangeEndCallbacks.invoke()
            return null
        }
        return newRect
    }
}