package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
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
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTimeUtil
import kotlin.math.abs
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
               time: ITSViewTimeUtil, draw: IRectDraw,
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
            when (mData.mCondition) {
                TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN -> { //点击上部区域却向下划过了底部高度
                    //这个mInitialSideY是之前矩形的底部值
                    mInitialRect.top = mInitialSideY + 1 // 加1是CorrectTopHeight与CorrectBottomHeight的差值
                }
                else -> mInitialRect.top = mInitialSideY
            }
            mInitialRect.top = mInitialSideY
            mInitialRect.bottom = min(rectInsideY, mLowerLimit)
        }else {
            mInitialRect.top = max(rectInsideY, mUpperLimit)
            when (mData.mCondition) {
                BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN,
                EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> { //点击下部区域或点击空白区域却向上划过了顶部高度
                    //这个mInitialSideY是之前矩形的顶部值
                    mInitialRect.bottom = mInitialSideY - 1 // 减1是CorrectTopHeight与CorrectBottomHeight的差值
                }
                else -> mInitialRect.bottom = mInitialSideY
            }
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
     * 让RectView增加一个之前被删除的矩形，虽位置已经改变，但Bean没变
     */
    fun addRectFromDeleted(rect: Rect) {
        mIRectManger.addRectFromDeleted(rect)
        mIRectView.notifyAllRectViewRefresh()
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
        mInitialSideY = mTime.getCorrectTopHeight(initialSideY, upperLimit, mPosition, mData.mTimeInterval)
        mUpperLimit = upperLimit
        mLowerLimit = lowerLimit
    }


    companion object {
        var UNCONSTRAINED_DISTANCE = 50
    }

    init {
        UNCONSTRAINED_DISTANCE = time.getMinuteBottomHeight(10)
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
    private var mRectWithBean: Map<Rect, TSViewBean>? = null

    override fun onDraw(canvas: Canvas) {
        if (!mInitialRect.isEmpty) {
            when (mData.mCondition) {
                EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                    val startTime = mTime.getTime(mInitialRect.top, mPosition)
                    val endTime = mTime.getTime(mInitialRect.bottom, mPosition)
                    val diffTime = mTime.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                    mDraw.drawRect(canvas, mInitialRect, mData.mDefaultTaskName, mData.mDefaultBorderColor, mData.mDefaultInsideColor)
                    mDraw.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
                    if (!mIsInRectChangeAnimator || mData.mIsShowDiffTime) {
                        mDraw.drawArrows(canvas, mInitialRect, diffTime)
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
                    if (!mIsInRectChangeAnimator || mData.mIsShowDiffTime) {
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
                mIRectView.setIsCanLongClick(false)
                val rect = getCorrectRect(y, mInitialRect) {
                    rectChangeEnd()
                }
                if (rect != null) {
                    when (mData.mCondition) {
                        TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN -> {
                            val bean = mDeletedBean!!
                            bean.startTime = mTime.getTime(rect.top, mPosition)
                            bean.diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            mIRectManger.addRectFromDeleted(rect)
                            mData.mDataChangeListener?.onDataAlter(bean)
                        }
                        BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                            val bean = mDeletedBean!!
                            bean.endTime = mTime.getTime(rect.bottom, mPosition)
                            bean.diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            mIRectManger.addRectFromDeleted(rect)
                            mData.mDataChangeListener?.onDataAlter(bean)
                        }
                        EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                            val name = mData.mDefaultTaskName
                            val startTime = mTime.getTime(rect.top, mPosition)
                            val endTime = mTime.getTime(rect.bottom, mPosition)
                            val diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            val borderColor = mData.mDefaultBorderColor
                            val insideColor = mData.mDefaultInsideColor
                            val bean = TSViewBean(name, startTime, endTime, diffTime, borderColor, insideColor)
                            mIRectManger.addNewRect(rect, bean)
                        }
                        else -> {
                        }
                    }
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

    private fun getCorrectRect(insideUpY: Int, rect: Rect, rectChangeEndCallbacks: () -> Unit): Rect? {
        if (rect.isEmpty) {
            rectChangeEndCallbacks.invoke()
            mIRectManger.deleteRect(mDeletedBean!!)
            return null
        }
        return if (abs(insideUpY - mInitialY) < UNCONSTRAINED_DISTANCE) { //抬起时的高度与按下去的起始高度的距离差在一定的范围内就
            if (rect.height() > mDraw.getMinHeight()) {
                postDelayed({
                    rectChangeEndCallbacks.invoke()
                }, 50)
                if (insideUpY < mInitialSideY) {
                    rect.top = mTime.getCorrectTopHeight(rect.top, mUpperLimit, mPosition, 1)
                    rect
                }else {
                    rect.bottom = mTime.getCorrectBottomHeight(rect.bottom, mLowerLimit, mPosition, 1)
                    rect
                }
            }else {
                deleteRect(rect, rectChangeEndCallbacks)
                null
            }
        }else {
            if (insideUpY < mInitialSideY) { //在上方滑动，只用计算Top值，因为另一边的值是正确的高度值
                getCorrectTopHeight(rect, rectChangeEndCallbacks)
            }else { //在下方滑动，只用计算Bottom值，因为另一边的值是正确的高度值
                getCorrectBottomHeight(rect, rectChangeEndCallbacks)
            }
        }
    }

    private var mIsInRectChangeAnimator = false
    private fun getCorrectTopHeight(rect: Rect, rectChangeEndCallbacks: () -> Unit): Rect? {
        val newRect = Rect()
        newRect.left = rect.left
        newRect.top = mTime.getCorrectTopHeight(rect.top, mUpperLimit, mPosition, mData.mTimeInterval)
        newRect.right = rect.right
        newRect.bottom = rect.bottom
        mIsInRectChangeAnimator = true
        if (newRect.height() > mDraw.getMinHeight()) {
            val bounce = sqrt(mData.mTimeInterval.toDouble() * 5).toInt()
            val animator = ValueAnimator.ofInt(rect.top, newRect.top - bounce, newRect.top)
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
            return newRect
        }else {
            deleteRect(rect, rectChangeEndCallbacks)
            return null
        }
    }

    private fun getCorrectBottomHeight(rect: Rect, rectChangeEndCallbacks: () -> Unit): Rect? {
        val newRect = Rect()
        newRect.left = rect.left
        newRect.top = rect.top
        newRect.right = rect.right
        newRect.bottom = mTime.getCorrectBottomHeight(rect.bottom, mLowerLimit, mPosition, mData.mTimeInterval)
        mIsInRectChangeAnimator = true
        if (newRect.height() > mDraw.getMinHeight()) {
            val bounce = sqrt(mData.mTimeInterval.toDouble() * 5).toInt()
            val animator = ValueAnimator.ofInt(rect.bottom, newRect.bottom + bounce, newRect.bottom)
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
            deleteRect(rect, rectChangeEndCallbacks)
            return null
        }
        return newRect
    }

    /**
     * 当矩形的高度小于能生成最小矩形的高度时调用，用于产生一个矩形被删除的动画
     */
    private fun deleteRect(rect: Rect, rectChangeEndCallbacks: () -> Unit) {
        val centerY = rect.centerY()
        val animator = ValueAnimator.ofInt(rect.height()/2, 6)
        animator.addUpdateListener {
            val d = it.animatedValue as Int
            mInitialRect.top = centerY - d
            mInitialRect.bottom = centerY + d
            invalidate()
        }
        animator.addListener(onEnd = {
            mIsInRectChangeAnimator = false
            rectChangeEndCallbacks.invoke()
        })
        animator.duration = 4 * rect.height().toLong()
        animator.start()
        mIRectManger.deleteRect(mDeletedBean!!)
    }
}