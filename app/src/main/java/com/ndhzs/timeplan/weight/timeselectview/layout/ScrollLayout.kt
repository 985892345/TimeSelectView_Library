package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick.*
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IScrollLayout

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 * @description 处理整体移动
 * [TimeScrollView]之下，
 * [ParentLayout]、[com.ndhzs.timeplan.weight.timeselectview.layout.view.RectImgView]之上
 */
@SuppressLint("ViewConstructor")
class ScrollLayout(context: Context, iScrollLayout: IScrollLayout, data: TSViewInternalData, rectManger: IRectManger) : FrameLayout(context) {

    init {
        val lp1 = LayoutParams(data.mAllTimelineWidth, LayoutParams.MATCH_PARENT)
        lp1.gravity = Gravity.CENTER
        iScrollLayout.addParentLayout(lp1, this)
        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        iScrollLayout.addRectImgView(lp2, this)
    }

    private val mData = data
    private val mRectManger = rectManger
    private val mIScrollLayout = iScrollLayout

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE) {
            if (mData.mCondition == INSIDE) {
                val rect = mRectManger.getDeletedRect()
                val bean = mRectManger.getDeletedBean()
                val position = mIScrollLayout.getRectViewPosition(ev.rawX.toInt())
                mIScrollLayout.entireMoveStart(rect, bean, position!!)
                mIScrollLayout.notifyRectViewRedraw()
                return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private var mInitialX = 0
    private var mInitialY = 0
    private var mNowPosition: Int? = null

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
                if (mData.mCondition == INSIDE) {
                    mIScrollLayout.slideRectImgView(x - mInitialX, y - mInitialY)
                }
            }
            MotionEvent.ACTION_UP -> {
                mIScrollLayout.setIsCanLongClick(false)
                val rawRect = mIScrollLayout.getRectImgViewRawRect()
                mNowPosition = mIScrollLayout.getRectViewPosition(rawRect.centerX())
                val isOverLeft = rawRect.right < mIScrollLayout.getChildLayoutLocation(0).left
                val isOverRight = rawRect.left > mIScrollLayout.getChildLayoutLocation(mData.mTSViewAmount - 1).right
                if (isOverLeft || isOverRight || mNowPosition == null) {
                    mIScrollLayout.deleteRectImgView {
                        mIScrollLayout.setIsCanLongClick(true)
                    }
                }else {
                    val prePosition = mIScrollLayout.getPreRectViewPosition()
                    val rawLeftAndInsideTop = getRawLeftAndInsideTop(rawRect, prePosition, mNowPosition!!)
                    val rawFinalLeft = rawLeftAndInsideTop[0]
                    val insideFinalTop = rawLeftAndInsideTop[1]
                    mIScrollLayout.slideEndRectImgView(rawFinalLeft, insideFinalTop) {
                        mIScrollLayout.setIsCanLongClick(true)
                        val rect2 = Rect(0, insideFinalTop, rawRect.width(), insideFinalTop + rawRect.height())
                        mIScrollLayout.notifyRectViewAddRectFromDeleted(rect2, mNowPosition!!)
                    }
                }
            }
        }
        return true
    }

    private fun getRawLeftAndInsideTop(rawRect: Rect, prePosition: Int, nowPosition: Int): IntArray {
        if (prePosition == nowPosition) {
            val rawLeft = getRawLeft(prePosition)
            val insideTop = getInsideTop(rawRect, prePosition)
            return intArrayOf(rawLeft, insideTop)
        }else {
            val top = rawRect.top
            val bottom = rawRect.bottom
            val rectHeight = rawRect.height()
            val nowUpperLimit = mRectManger.getUpperLimit(bottom, nowPosition)
            val nowLowerLimit = mRectManger.getLowerLimit(top, nowPosition)
            val prePositionLeft = getRawLeft(prePosition)
            val nowPositionLeft = getRawLeft(nowPosition)
            return if (rectHeight <= nowLowerLimit - nowUpperLimit) {
                if (nowLowerLimit in top..bottom) { //1
                    intArrayOf(nowPositionLeft, nowLowerLimit - rectHeight)
                }else if (nowUpperLimit in top..bottom) { //2
                    intArrayOf(nowPositionLeft, nowUpperLimit)
                }else { //包括了3、4、5、6、7
                    val lowerLimit = mRectManger.getLowerLimit(nowUpperLimit, nowPosition)
                    if (lowerLimit == nowLowerLimit) { //3、5-1、7-1
                        intArrayOf(nowPositionLeft, top)
                    }else { //4、5-2、6、7-2
                        intArrayOf(prePositionLeft, top)
                    }
                }
            }else { //包括 a 的所有情况
                intArrayOf(prePositionLeft, top)
            }
        }
    }

    /**
     * 返回整体移动放手后矩形该放置的位置的左边值
     */
    private fun getRawLeft(position: Int): Int {
        return mIScrollLayout.getRectViewRawLocation(position).left
    }

    /**
     * 注意：只用于在一个RectView中上下滑动
     *
     * 返回整体移动放手后矩形该放置的位置的内部高度值
     * @param rawRect 传入左右值为相对于屏幕坐标，上下值为内部坐标的Rect
     * @return 内部高度值
     */
    private fun getInsideTop(rawRect: Rect, position: Int): Int {
        val top = rawRect.top
        val bottom = rawRect.bottom
        val rectHeight = rawRect.height()
        val preUpperLimit = mRectManger.getClickUpperLimit()
        val preLowerLimit = mRectManger.getClickLowerLimit()
        val nowUpperLimit = mRectManger.getUpperLimit(bottom, position)
        val nowLowerLimit = mRectManger.getLowerLimit(top, position)
        //每个if对应了一种情况，具体请以序号看纸上的草图
        return if (rectHeight <= nowLowerLimit - nowUpperLimit) {
            if (nowLowerLimit in top..bottom) { //1
                nowLowerLimit - rectHeight
            }else if (nowUpperLimit in top..bottom) { //2
                nowUpperLimit
            }else if (nowUpperLimit == preUpperLimit && nowLowerLimit == preLowerLimit) { //3
                if (bottom <= mData.mRectViewTop) { //整体移动到mRectViewTop以上
                    preUpperLimit
                }else if (top >= mData.mRectViewBottom) { //整体移动到mRectViewBottom以下
                    preLowerLimit - rectHeight
                }else top
            }else if (nowUpperLimit == preUpperLimit) { //4
                preLowerLimit - rectHeight
            }else if (nowUpperLimit > preUpperLimit) { //5
                val lowerLimit = mRectManger.getLowerLimit(nowUpperLimit, position)
                if (lowerLimit == nowLowerLimit) { //5-1
                    top
                }else { //5-2
                    if (rectHeight <= lowerLimit - nowUpperLimit) {
                        lowerLimit - rectHeight
                    }else preLowerLimit - rectHeight
                }
            }else if (nowLowerLimit == preLowerLimit) { //6
                preUpperLimit
            }else if (nowLowerLimit < preLowerLimit) { //7
                val upperLimit = mRectManger.getUpperLimit(nowLowerLimit, position)
                if (upperLimit == nowUpperLimit) { //7-1
                    top
                }else { //7-2
                    if (rectHeight <= nowLowerLimit - upperLimit) {
                        upperLimit
                    }else preUpperLimit
                }
            }else top //按理是不会走这一步
        }else {
            if (nowLowerLimit == preLowerLimit) { //a-1
                preLowerLimit - rectHeight
            }else if (nowUpperLimit == preUpperLimit) { //a-2
                preUpperLimit
            }else if (nowLowerLimit > preLowerLimit) { //a-3
                preLowerLimit - rectHeight
            }else if (nowUpperLimit < preUpperLimit) { //a-4
                preUpperLimit
            }else top //按理是不会走这一步
        }
    }
}