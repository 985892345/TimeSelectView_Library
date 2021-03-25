package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewUtil
import kotlin.math.max
import kotlin.math.min

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
@SuppressLint("ViewConstructor")
class RectView(context: Context, util: TSViewUtil) : View(context) {

    private val mUtil = util
    private val mTimeUtil = util.mTimeUtil
    private val mDrawUtil = util.mDrawUtil
    private val mRectUtil = util.mRectUtil
    private val mInitialRect = Rect()
    private val mRectWithBean = mRectUtil.mRectWithBean

    override fun onDraw(canvas: Canvas) {
        when (mUtil.mCondition) {
            TSViewLongClick.EMPTY_AREA -> {
                val startTime = mTimeUtil.getTime(mInitialRect.top)
                val endTime = mTimeUtil.getTime(mInitialRect.bottom)
                val diffTime = mTimeUtil.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                mDrawUtil.drawRect(canvas, mInitialRect, mUtil.mDefaultTaskName, mUtil.mDefaultBorderColor, mUtil.mDefaultInsideColor)
                mDrawUtil.drawArrows(canvas, mInitialRect, diffTime)
                mDrawUtil.drawStartTime(canvas, mInitialRect, startTime)
                mDrawUtil.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
            }
            TSViewLongClick.TOP, TSViewLongClick.BOTTOM -> {
                val bean = mRectUtil.mDeletedBean
                val name = bean.name
                val borderColor = bean.borderColor
                val insideColor = bean.insideColor
                val startTime = mTimeUtil.getTime(mInitialRect.top)
                val endTime = mTimeUtil.getTime(mInitialRect.bottom)
                val diffTime = mTimeUtil.getDiffTime(mInitialRect.top, mInitialRect.bottom)
                mDrawUtil.drawRect(canvas, mInitialRect, name, borderColor, insideColor)
                mDrawUtil.drawArrows(canvas, mInitialRect, diffTime)
                mDrawUtil.drawStartEndTime(canvas, mInitialRect, startTime, endTime)
            }
            else -> {}
        }
        mRectWithBean.forEach{
            val rect = it.key
            val bean = it.value
            mDrawUtil.drawRect(canvas, rect, bean.name, bean.borderColor, bean.insideColor)
            if (mUtil.mIsShowDiffTime) {
                mDrawUtil.drawArrows(canvas, rect, bean.diffTime)
            }
            if (mUtil.mIsShowStartEndTime) {
                mDrawUtil.drawStartEndTime(canvas, rect, bean.startTime, bean.endTime)
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
                when (mUtil.mCondition) {
                    TSViewLongClick.TOP, TSViewLongClick.BOTTOM, TSViewLongClick.EMPTY_AREA -> {
                        if (y > mRectUtil.mInitialSideY) {
                            mInitialRect.top = mRectUtil.mInitialSideY
                            mInitialRect.bottom = min(y, mRectUtil.mLowerLimit)
                        }else {
                            mInitialRect.bottom = mRectUtil.mInitialSideY
                            mInitialRect.top = max(y, mRectUtil.mUpperLimit)
                        }
                    }
                    else -> {}
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (mInitialRect.height() > mDrawUtil.mRectMinHeight) {
                    val rect = Rect(mInitialRect)
                    when (mUtil.mCondition) {
                        TSViewLongClick.TOP -> {
                            val bean = mRectUtil.mDeletedBean
                            bean.startTime = mTimeUtil.getTime(rect.top)
                            bean.diffTime = mTimeUtil.getDiffTime(rect.top, rect.bottom)
                            mRectWithBean[rect] = bean
                        }
                        TSViewLongClick.BOTTOM -> {
                            val bean = mRectUtil.mDeletedBean
                            bean.endTime = mTimeUtil.getTime(rect.bottom)
                            bean.diffTime = mTimeUtil.getDiffTime(rect.top, rect.bottom)
                            mRectWithBean[rect] = bean
                        }
                        TSViewLongClick.EMPTY_AREA -> {
                            val name = mUtil.mDefaultTaskName
                            val startTime = mTimeUtil.getTime(rect.top)
                            val endTime = mTimeUtil.getTime(rect.bottom)
                            val diffTime = mTimeUtil.getDiffTime(rect.top, rect.bottom)
                            val borderColor = mUtil.mDefaultBorderColor
                            val insideColor = mUtil.mDefaultInsideColor
                            val bean = TSViewBean(name, startTime, endTime, diffTime, borderColor, insideColor)
                            mRectWithBean[rect] = bean
                        }
                        else -> {}
                    }
                }
                mUtil.mCondition = TSViewLongClick.NULL
                mInitialRect.setEmpty()
                invalidate()
            }
        }
        return true
    }
}