package com.ndhzs.timeselectview.layout.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import com.ndhzs.timeselectview.viewinterface.IRectDraw
import com.ndhzs.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.utils.TSViewListeners
import com.ndhzs.timeselectview.utils.TSViewLongClick.*
import com.ndhzs.timeselectview.viewinterface.IRectView
import com.ndhzs.timeselectview.viewinterface.IRectViewRectManger
import com.ndhzs.timeselectview.viewinterface.ITSViewTimeUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [com.ndhzs.timeselectview.layout.ChildLayout]之下
 */
@SuppressLint("ViewConstructor")
internal class RectView(
        context: Context,
        private val attrs: TSViewAttrs,
        private val listeners: TSViewListeners,
        private val time: ITSViewTimeUtil,
        private val draw: IRectDraw,
        private val iRectViewRectManger: IRectViewRectManger,
        private val iRectView: IRectView,
        private val position: Int
) : View(context) {

    /**
     * 用于在TimeScrollView滑动过程和自身MOVE事件中绘制矩形，此时长按已经确认，所以Rect的起始高度已经确定。
     *
     * 注意：只有在自身处于触摸时调用该方法才有效。
     *
     * @param rectInsideY Rect结束的高度
     */
    fun slideDrawRect(rectInsideY: Int) {
        if (rectInsideY > mInitialSideY) {
            when (attrs.mCondition) {
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
            when (attrs.mCondition) {
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
        mRectWithTaskBean = iRectViewRectManger.getRectWithBeanMap()
        invalidate()
    }

    /**
     * 让RectView增加一个之前被删除的矩形，虽位置已经改变，但Bean没变
     */
    fun addRectFromDeleted(rect: Rect) {
        iRectViewRectManger.addRectFromDeleted(rect)
        iRectView.notifyAllRectViewRefresh()
    }

    /**
     * 恢复被删掉的矩形
     */
    fun recoverRectFromDeleted() {
        addRectFromDeleted(iRectViewRectManger.getDeletedRect())
    }

    /**
     * 用于在长按后设置动态绘图的矩形和bean
     */
    fun clickTopAndBottomStart(initialRect: Rect, deletedTaskBean: TSViewTaskBean, initialSideY: Int, upperLimit: Int, lowerLimit: Int) {
        mInitialSideY = initialSideY
        mUpperLimit = upperLimit
        mLowerLimit = lowerLimit
        when (attrs.mCondition) {
            TOP, BOTTOM -> {
                mInitialRect.set(initialRect)
                mDeletedTaskBean = deletedTaskBean
            }
            else -> {}
        }
        iRectView.notifyAllRectViewRefresh()
    }

    /**
     * 设置开始点击的Rect的某一边的高度值和上下限值
     */
    fun clickEmptyStart(initialSideY: Int, upperLimit: Int, lowerLimit: Int) {
        mInitialSideY = time.getCorrectTopHeight(initialSideY, upperLimit, position, attrs.mTimeInterval)
        mUpperLimit = upperLimit
        mLowerLimit = lowerLimit
    }

    /**
     * 在能够摆脱时间间隔数控制的最小移动距离
     */
    val mUnconstrainedDistance: Int = time.getMinuteBottomHeight(10)


    private val mInitialRect = Rect() //动态绘图的矩形
    private var mInitialSideY = 0
    private var mUpperLimit = 0
    private var mLowerLimit = 0
    private var mDeletedTaskBean: TSViewTaskBean? = null
    private var mRectWithTaskBean: Map<Rect, TSViewTaskBean>? = null

    override fun onDraw(canvas: Canvas) {
        if (!mInitialRect.isEmpty) {
            when (attrs.mCondition) {
                EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                    val startTime = time.getTime(mInitialRect.top, position)
                    val endTime = time.getTime(mInitialRect.bottom, position)
                    val diffTime = time.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                    draw.drawRect(canvas, mInitialRect, attrs.mDefaultTaskName, attrs.mDefaultBorderColor, attrs.mDefaultInsideColor)
                    draw.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
                    if (!mIsInRectChangeAnimator || attrs.mIsShowDiffTime) {
                        draw.drawArrows(canvas, mInitialRect, diffTime)
                    }
                }
                TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN,
                BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                    val bean = mDeletedTaskBean!!
                    val name = bean.name
                    val borderColor = bean.borderColor
                    val insideColor = bean.insideColor
                    val startTime = time.getTime(mInitialRect.top, position)
                    val endTime = time.getTime(mInitialRect.bottom, position)
                    val diffTime = time.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                    draw.drawRect(canvas, mInitialRect, name, borderColor, insideColor)
                    draw.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
                    if (!mIsInRectChangeAnimator || attrs.mIsShowDiffTime) {
                        draw.drawArrows(canvas, mInitialRect, diffTime)
                    }
                }
                else -> {}
            }
        }
        mRectWithTaskBean?.forEach {
            val rect = it.key
            val bean = it.value
            draw.drawRect(canvas, rect, bean.name, bean.borderColor, bean.insideColor)
            if (attrs.mIsShowDiffTime) {
                draw.drawArrows(canvas, rect, bean.diffTime)
            }
            if (attrs.mIsShowStartEndTime) {
                draw.drawStartEndTime(canvas, rect, bean.startTime, bean.endTime)
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
                mInitialRect.right = attrs.mRectViewWidth
            }
            MotionEvent.ACTION_MOVE -> {
                when (attrs.mCondition) {
                    TOP, BOTTOM, EMPTY_AREA -> {
                        slideDrawRect(y)
                    }
                    else -> {} //在TimeScrollView滑动时不在此处绘制矩形
                }
            }
            MotionEvent.ACTION_UP -> {
                iRectView.setIsCanLongClick(false)
                val rect = getCorrectRect(y, mInitialRect) {
                    rectChangeEnd()
                }
                if (rect != null) {
                    when (attrs.mCondition) {
                        TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN -> {
                            val bean = mDeletedTaskBean!!
                            bean.startTime = time.getTime(rect.top, position)
                            bean.diffTime = time.getDiffTime(rect.top, rect.bottom)
                            iRectViewRectManger.addRectFromDeleted(rect)
                            iRectView.notifyTimeScrollViewScrollToSuitableHeight()
                            listeners.mOnDataChangeListener?.onDataAlter(bean)
                        }
                        BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                            val bean = mDeletedTaskBean!!
                            bean.endTime = time.getTime(rect.bottom, position)
                            bean.diffTime = time.getDiffTime(rect.top, rect.bottom)
                            iRectViewRectManger.addRectFromDeleted(rect)
                            iRectView.notifyTimeScrollViewScrollToSuitableHeight()
                            listeners.mOnDataChangeListener?.onDataAlter(bean)
                        }
                        EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                            val name = attrs.mDefaultTaskName
                            val startTime = time.getTime(rect.top, position)
                            val endTime = time.getTime(rect.bottom, position)
                            val diffTime = time.getDiffTime(rect.top, rect.bottom)
                            val borderColor = attrs.mDefaultBorderColor
                            val insideColor = attrs.mDefaultInsideColor
                            val bean = TSViewTaskBean(iRectView.getDay(), name, startTime, endTime, diffTime, borderColor, insideColor)
                            iRectViewRectManger.addNewRect(rect, bean)
                            iRectView.notifyTimeScrollViewScrollToSuitableHeight()
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
        mInitialRect.right = attrs.mRectViewWidth
        iRectView.notifyAllRectViewRefresh()
        attrs.mCondition = NULL
        iRectView.setIsCanLongClick(true)
    }

    private fun getCorrectRect(insideUpY: Int, rect: Rect, rectChangeEndCallbacks: () -> Unit): Rect? {
        if (rect.isEmpty) {
            rectChangeEndCallbacks.invoke()
            if (mDeletedTaskBean != null) {
                iRectViewRectManger.deleteRect(mDeletedTaskBean!!)
            }
            return null
        }
        return if (abs(insideUpY - mInitialY) < mUnconstrainedDistance) { //抬起时的高度与按下去的起始高度的距离差在一定的范围内就可以不受时间间隔数约束
            if (rect.height() > draw.getMinHeight()) {
                postDelayed({
                    rectChangeEndCallbacks.invoke()
                }, 50)
                if (insideUpY < mInitialSideY) {
                    rect.top = time.getCorrectTopHeight(rect.top, mUpperLimit, position, 1)
                    rect
                }else {
                    rect.bottom = time.getCorrectBottomHeight(rect.bottom, mLowerLimit, position, 1)
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
        newRect.top = time.getCorrectTopHeight(rect.top, mUpperLimit, position, attrs.mTimeInterval)
        newRect.right = rect.right
        newRect.bottom = rect.bottom
        mIsInRectChangeAnimator = true
        if (newRect.height() > draw.getMinHeight()) {
            val bounce = sqrt(attrs.mTimeInterval.toDouble() * 5).toInt() //回弹的距离
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
        newRect.bottom = time.getCorrectBottomHeight(rect.bottom, mLowerLimit, position, attrs.mTimeInterval)
        mIsInRectChangeAnimator = true
        if (newRect.height() > draw.getMinHeight()) {
            val bounce = sqrt(attrs.mTimeInterval.toDouble() * 5).toInt() //回弹的距离
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
        val animator = ValueAnimator.ofInt(rect.height() / 2, 6)
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
        if (mDeletedTaskBean != null) {
            iRectViewRectManger.deleteRect(mDeletedTaskBean!!)
        }
    }
}