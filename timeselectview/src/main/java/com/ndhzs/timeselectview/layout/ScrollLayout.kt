package com.ndhzs.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.utils.TSViewListeners
import com.ndhzs.timeselectview.utils.TSViewLongClick.*
import com.ndhzs.timeselectview.viewinterface.IRectManger
import com.ndhzs.timeselectview.viewinterface.IScrollLayout
import com.ndhzs.timeselectview.viewinterface.ITSViewTimeUtil

/**
 * 处理整体移动，一旦触发整体移动，触摸事件都会在此被拦截，RectView 将收不到事件
 *
 * [TimeScrollView]之下，
 * [ParentLayout]、[StickerLayout]之上
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 */
@SuppressLint("ViewConstructor")
internal class ScrollLayout(
        context: Context,
        private val iScrollLayout: IScrollLayout,
        private val attrs: TSViewAttrs,
        private val listeners: TSViewListeners,
        private val time: ITSViewTimeUtil,
        private val rectManger: IRectManger
) : FrameLayout(context) {

    init {
        val lp1 = LayoutParams(attrs.mAllTimelineWidth, LayoutParams.MATCH_PARENT)
        lp1.gravity = Gravity.CENTER
        attachViewToParent(iScrollLayout.getParentLayout(), -1, lp1)

        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        attachViewToParent(iScrollLayout.getStickerLayout(), -1, lp2)
    }

    private var mInitialX = 0
    private var mInitialY = 0

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = ev.x.toInt()
                mInitialY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                when (attrs.mCondition) {
                    INSIDE, INSIDE_SLIDE_DOWN, INSIDE_SLIDE_UP -> {
                        return true
                    }
                    else -> {}
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }


    private var mNowPosition: Int? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                when (attrs.mCondition) {
                    INSIDE -> {
                        iScrollLayout.slideRectImgView(x - mInitialX, y - mInitialY)
                    }
                    else -> {}
                }
            }
            MotionEvent.ACTION_UP -> {
                iScrollLayout.setIsCanLongClick(false)
                val inWindowRect = iScrollLayout.getRectImgViewInWindowRect()
                mNowPosition = iScrollLayout.getRectViewPosition(inWindowRect.centerX())
                val isOverLeft = inWindowRect.right < iScrollLayout.getChildLayoutInWindowLeftRight(0)[0]
                val isOverRight = inWindowRect.left > iScrollLayout.getChildLayoutInWindowLeftRight(attrs.mTSViewAmount - 1)[1]
                if (isOverLeft || isOverRight || mNowPosition == null) {
                    iScrollLayout.setIsCanLongClick(true)
                    iScrollLayout.deleteRectImgView()
                    rectManger.deleteBean(rectManger.getDeletedBean())
                }else {
                    putDownRectJudgeAndProcess(inWindowRect, y)
                }
                attrs.mCondition = NULL
            }
        }
        return true
    }

    /**
     * 能否放下矩形的判断和处理
     */
    private fun putDownRectJudgeAndProcess(inWindowRect: Rect, y: Int) {
        val prePosition = iScrollLayout.getPreRectViewPosition()
        val inWindowLeftAndInsideTop = getRawLeftAndInsideTop(inWindowRect, prePosition, mNowPosition!!, y)
        val inWindowFinalLeft = inWindowLeftAndInsideTop[0]
        val insideFinalTop = inWindowLeftAndInsideTop[1]
        val position = inWindowLeftAndInsideTop[2]
        iScrollLayout.slideEndRectImgView(inWindowFinalLeft, insideFinalTop) {
            val topBottom = if (y <= mInitialY + iScrollLayout.getUnconstrainedDistance()) { //说明矩形向上移动
                time.getCorrectTopHeight(
                        insideFinalTop,
                        insideFinalTop,
                        rectManger.getLowerLimit(insideFinalTop, position),
                        position,
                        rectManger.getDeletedBean().diffTime)
            }else { //说明矩形向下移动
                //因为是向下滑动的，所以之前计算的是正确的bottom值
                val correctBottom = insideFinalTop + inWindowRect.height()
                time.getCorrectBottomHeight(
                        correctBottom,
                        rectManger.getUpperLimit(correctBottom, position),
                        correctBottom,
                        position,
                        rectManger.getDeletedBean().diffTime)
            }
            val times = iScrollLayout.getStartEndDTime(topBottom[0], topBottom[1], position)
            val bean = rectManger.getDeletedBean()
            bean.startTime = times[0]
            bean.endTime = times[1]
            bean.diffTime = times[2]
            listeners.mOnDataChangeListener?.onDataAlter(bean)
            val rect2 = Rect(0, topBottom[0], inWindowRect.width(), topBottom[1])
            iScrollLayout.notifyRectViewAddRectFromDeleted(rect2, position)
            iScrollLayout.setIsCanLongClick(true) //设置为true后只要仍满足长按条件，则可以重启长按。注意：这条语句位置必须在通知RectView后调用
        }

        if (mIsBack) { //抬起手后，回到原位置的时候通知TimeScrollView滑到原来矩形的中心高度
            iScrollLayout.notifyTimeScrollViewScrollToInitialHeight(iScrollLayout.getRectImgViewInitialRect().centerY())
        }else { //抬起手后，在没有回到原位置的时候通知TimeScrollView自动滑到适宜的高度
            iScrollLayout.notifyTimeScrollViewScrollToSuitableHeight()
        }
    }

    private var mIsBack = false

    /**
     * 得到不同情况的整体移动后应该放置的矩形的 RawLeft、InsideTop
     *
     * RawLeft：矩形的 left 值，该值为距离窗口的 left 值
     *
     * InsideTop：矩形的 top 值，该值为 ScrollView 内部坐标系下的值，可看 [getInsideTop]
     */
    private fun getRawLeftAndInsideTop(inWindowRect: Rect, prePosition: Int, nowPosition: Int, insideUpY: Int): IntArray {
        if (prePosition == nowPosition) {
            val inWindowLeft = getInWindowLeft(prePosition)
            val insideTop = getInsideTop(inWindowRect, prePosition, insideUpY)
            return intArrayOf(inWindowLeft, insideTop, prePosition)
        }else {
            //以下是整体移动到另一个RectView，不同的情况对应不同的图，图在两张A4纸上
            val top = inWindowRect.top
            val bottom = inWindowRect.bottom
            val rectHeight = inWindowRect.height()
            val initialRect = iScrollLayout.getRectImgViewInitialRect()
            val preUpperLimit = rectManger.getClickUpperLimit()
            val preLowerLimit = rectManger.getClickLowerLimit()
            val nowUpperLimit = rectManger.getUpperLimit(bottom, nowPosition)
            val nowLowerLimit = rectManger.getLowerLimit(top, nowPosition)
            val prePositionLeft = getInWindowLeft(prePosition)
            val nowPositionLeft = getInWindowLeft(nowPosition)
            return if (rectHeight <= nowLowerLimit - nowUpperLimit) {
                if (nowLowerLimit in top..bottom) { //1
                    mIsBack = false
                    intArrayOf(nowPositionLeft, nowLowerLimit - rectHeight, nowPosition)
                }else if (nowUpperLimit in top..bottom) { //2
                    mIsBack = false
                    intArrayOf(nowPositionLeft, nowUpperLimit, nowPosition)
                }else { //包括了3、4、5、6、7
                    if (bottom < attrs.mRectViewTop || top > attrs.mRectViewBottom) { //在RectViewTop以上或mRectViewBottom以下
                        mIsBack = true
                        val correctTop = getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, prePosition, insideUpY)
                        intArrayOf(prePositionLeft, correctTop, prePosition)
                    }else {
                        val lowerLimit = rectManger.getLowerLimit(nowUpperLimit, nowPosition)
                        if (lowerLimit == nowLowerLimit) { //3、5-1、7-1
                            mIsBack = false
                            val correctTop = getCorrectTopHeight(inWindowRect, nowUpperLimit, nowLowerLimit, nowPosition, insideUpY)
                            intArrayOf(nowPositionLeft, correctTop, nowPosition)
                        } else { //4、5-2、6、7-2
                            mIsBack = true
                            val correctTop = getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, prePosition, insideUpY)
                            intArrayOf(prePositionLeft, correctTop, prePosition)
                        }
                    }
                }
            }else { //包括 a 的所有情况
                mIsBack = true
                val correctTop = getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, prePosition, insideUpY)
                intArrayOf(prePositionLeft, correctTop, prePosition)
            }
        }
    }

    /**
     * 返回整体移动放手后矩形该放置的位置的左边值
     */
    private fun getInWindowLeft(position: Int): Int {
        return iScrollLayout.getRectViewInWindowLeftRight(position)[0]
    }

    /**
     * **WARNING：** 只用于在一个RectView中上下滑动
     *
     * 返回整体移动放手后矩形该放置的位置的 ScrollView 内部高度值
     *
     * @param rawRect 传入左右值为相对于屏幕坐标，上下值为内部坐标的Rect
     * @return 内部高度值
     */
    private fun getInsideTop(rawRect: Rect, position: Int, insideUpY: Int): Int {
        val top = rawRect.top
        val bottom = rawRect.bottom
        val rectHeight = rawRect.height()
        val initialRect = iScrollLayout.getRectImgViewInitialRect()
        val preUpperLimit = rectManger.getClickUpperLimit()
        val preLowerLimit = rectManger.getClickLowerLimit()
        val nowUpperLimit = rectManger.getUpperLimit(bottom, position)
        val nowLowerLimit = rectManger.getLowerLimit(top, position)
        //每个if对应了一种情况，具体请以序号看纸上的草图
        return if (rectHeight <= nowLowerLimit - nowUpperLimit) {
            if (nowLowerLimit in top..bottom) { //1
                mIsBack = false
                nowLowerLimit - rectHeight
            }else if (nowUpperLimit in top..bottom) { //2
                mIsBack = false
                nowUpperLimit
            }else { //包括了3、4、5、6、7
                if (bottom < attrs.mRectViewTop || top > attrs.mRectViewBottom) { //在RectViewTop以上或mRectViewBottom以下
                    mIsBack = true
                    getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, position, insideUpY)
                }else {
                    val lowerLimit = rectManger.getLowerLimit(nowUpperLimit, position)
                    if (lowerLimit == nowLowerLimit) { //3、5-1、7-1
                        mIsBack = false
                        getCorrectTopHeight(rawRect, nowUpperLimit, nowLowerLimit, position, insideUpY)
                    } else { //4、5-2、6、7-2
                        mIsBack = true
                        getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, position, insideUpY)
                    }
                }
            }
        }else { //包括 a 的所有情况
            mIsBack = true
            getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, position, insideUpY)
        }
    }

    private fun getCorrectTopHeight(rect: Rect, upperLimit: Int, lowerLimit: Int, position: Int, insideUpY: Int): Int {
        //以下用来判断是否上下移动后而用时间间隔数计算得出正确的top值
        return if (insideUpY < mInitialY - iScrollLayout.getUnconstrainedDistance()) { //说明矩形向上移动了
            time.getCorrectTopHeight(rect.top, upperLimit, position, attrs.mTimeInterval)
        }else if (insideUpY > mInitialY + iScrollLayout.getUnconstrainedDistance()){ //说明矩形向下移动了
            time.getCorrectBottomHeight(rect.bottom, lowerLimit, position, attrs.mTimeInterval) - rect.height()
        }else {
            time.getCorrectTopHeight(rect.top, upperLimit, position, 1)
        }
    }
}