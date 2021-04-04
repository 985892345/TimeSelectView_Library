package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectDraw
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick.*
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectImgView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime
import kotlin.math.max
import kotlin.math.min

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
@SuppressLint("ViewConstructor")
class RectView(context: Context, data: TSViewInternalData, time: ITSViewTime, draw: IRectDraw, rectManger: IRectManger) : View(context) {

    /**
     * 用于在TimeScrollView滑动过程和自身MOVE事件中绘制矩形，此时长按已经确认，所以Rect的起始高度已经确定
     * @param insideY Rect的底部高度
     */
    fun slideDrawRect(insideY: Int) {
        val sideY = mRectManger.getInitialSideY()
        if (insideY > sideY) {
            mInitialRect.top = sideY
            mInitialRect.bottom = min(insideY, mRectManger.getLowerLimit())
        }else {
            mInitialRect.bottom = sideY
            mInitialRect.top = max(insideY, mRectManger.getUpperLimit())
        }
        invalidate()
    }

    /**
     * 通知RectView的所有矩形重新绘制，一般用于在设置任务显示的颜色、时间等属性时调用
     */
    fun notifyAllRectRedraw() {
        invalidate()
    }




    private val mData = data
    private val mTime = time
    private val mDraw = draw
    private val mRectManger = rectManger
    private val mInitialRect = Rect()
    private val mRectWithBean = mRectManger.getRectWithBeanMap()

    override fun onDraw(canvas: Canvas) {
        when (mData.mCondition) {
            EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                val startTime = mTime.getTime(mInitialRect.top)
                val endTime = mTime.getTime(mInitialRect.bottom)
                val diffTime = mTime.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                mDraw.drawRect(canvas, mInitialRect, mData.mDefaultTaskName, mData.mDefaultBorderColor, mData.mDefaultInsideColor)
                mDraw.drawArrows(canvas, mInitialRect, diffTime)
                mDraw.drawStartTime(canvas, mInitialRect, startTime)
                mDraw.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
            }
            TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN,
            BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                val bean = mRectManger.getDeletedBean()
                val name = bean.name
                val borderColor = bean.borderColor
                val insideColor = bean.insideColor
                val startTime = mTime.getTime(mInitialRect.top)
                val endTime = mTime.getTime(mInitialRect.bottom)
                val diffTime = mTime.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                mDraw.drawRect(canvas, mInitialRect, name, borderColor, insideColor)
                mDraw.drawArrows(canvas, mInitialRect, diffTime)
                mDraw.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
            }
            else -> {}
        }
        mRectWithBean.forEach{
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
                if (mInitialRect.height() > mDraw.getMinHeight()) {
                    val rect = Rect(mInitialRect)
                    when (mData.mCondition) {
                        TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN -> {
                            val bean = mRectManger.getDeletedBean()
                            bean.startTime = mTime.getTime(rect.top)
                            bean.diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            mRectWithBean[rect] = bean
                        }
                        BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                            val bean = mRectManger.getDeletedBean()
                            bean.endTime = mTime.getTime(rect.bottom)
                            bean.diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            mRectWithBean[rect] = bean
                        }
                        EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                            val name = mData.mDefaultTaskName
                            val startTime = mTime.getTime(rect.top)
                            val endTime = mTime.getTime(rect.bottom)
                            val diffTime = mTime.getDiffTime(rect.top, rect.bottom)
                            val borderColor = mData.mDefaultBorderColor
                            val insideColor = mData.mDefaultInsideColor
                            val bean = TSViewBean(name, startTime, endTime, diffTime, borderColor, insideColor)
                            mRectWithBean[rect] = bean
                        }
                        else -> {}
                    }
                }
                mData.mCondition = NULL
                mInitialRect.setEmpty()
                invalidate()
            }
        }
        return true
    }


}