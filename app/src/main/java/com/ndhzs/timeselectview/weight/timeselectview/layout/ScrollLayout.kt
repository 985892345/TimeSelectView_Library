package com.ndhzs.timeselectview.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import com.ndhzs.timeselectview.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeselectview.weight.timeselectview.utils.TSViewLongClick.*
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.IRectManger
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.IScrollLayout
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.ITSViewTimeUtil

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 * @description 处理整体移动，一旦触发整体移动，触摸事件都会在此被拦截
 * [TimeScrollView]之下，
 * [ParentLayout]、[StickerLayout]之上
 */
@SuppressLint("ViewConstructor")
class ScrollLayout(context: Context, iScrollLayout: IScrollLayout, data: TSViewInternalData, time: ITSViewTimeUtil, rectManger: IRectManger) : FrameLayout(context) {

    init {
        val lp1 = LayoutParams(data.mAllTimelineWidth, LayoutParams.MATCH_PARENT)
        lp1.gravity = Gravity.CENTER
        iScrollLayout.addParentLayout(lp1, this)
        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        iScrollLayout.addStickerLayout(lp2, this)
    }

    private val mData = data
    private val mTime = time
    private val mRectManger = rectManger
    private val mIScrollLayout = iScrollLayout

    private var mInitialX = 0
    private var mInitialY = 0

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = ev.x.toInt()
                mInitialY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                when (mData.mCondition) {
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
                when (mData.mCondition) {
                    INSIDE -> {
                        mIScrollLayout.slideRectImgView(x - mInitialX, y - mInitialY)
                    }
                    else -> {}
                }
            }
            MotionEvent.ACTION_UP -> {
                mIScrollLayout.setIsCanLongClick(false)
                val inWindowRect = mIScrollLayout.getRectImgViewInWindowRect()
                mNowPosition = mIScrollLayout.getRectViewPosition(inWindowRect.centerX())
                val isOverLeft = inWindowRect.right < mIScrollLayout.getChildLayoutInWindowLeftRight(0)[0]
                val isOverRight = inWindowRect.left > mIScrollLayout.getChildLayoutInWindowLeftRight(mData.mTSViewAmount - 1)[1]
                if (isOverLeft || isOverRight || mNowPosition == null) {
                    mIScrollLayout.setIsCanLongClick(true)
                    mIScrollLayout.deleteRectImgView()
                    mRectManger.deleteBean(mRectManger.getDeletedBean())
                }else {
                    putDownRectJudgeAndProcess(inWindowRect, y)
                }
                mData.mCondition = NULL
            }
        }
        return true
    }

    /**
     * 能否放下矩形的判断和处理
     */
    private fun putDownRectJudgeAndProcess(inWindowRect: Rect, y: Int) {
        val prePosition = mIScrollLayout.getPreRectViewPosition()
        val inWindowLeftAndInsideTop = getRawLeftAndInsideTop(inWindowRect, prePosition, mNowPosition!!, y)
        val inWindowFinalLeft = inWindowLeftAndInsideTop[0]
        val insideFinalTop = inWindowLeftAndInsideTop[1]
        val position = inWindowLeftAndInsideTop[2]
        mIScrollLayout.slideEndRectImgView(inWindowFinalLeft, insideFinalTop) {
            val topBottom = if (y <= mInitialY + mIScrollLayout.getUnconstrainedDistance()) { //说明矩形向上移动
                mTime.getCorrectTopHeight(insideFinalTop,
                        insideFinalTop,
                        mRectManger.getLowerLimit(insideFinalTop, position),
                        position,
                        mRectManger.getDeletedBean().diffTime)
            }else { //说明矩形向下移动
                //因为是向下滑动的，所以之前计算的是正确的bottom值
                val correctBottom = insideFinalTop + inWindowRect.height()
                mTime.getCorrectBottomHeight(correctBottom,
                        mRectManger.getUpperLimit(correctBottom, position),
                        correctBottom,
                        position,
                        mRectManger.getDeletedBean().diffTime)
            }
            val times = mIScrollLayout.getStartEndDTime(topBottom[0], topBottom[1], position)
            val bean = mRectManger.getDeletedBean()
            bean.startTime = times[0]
            bean.endTime = times[1]
            bean.diffTime = times[2]
            mData.mDataChangeListener?.onDataAlter(bean)
            val rect2 = Rect(0, topBottom[0], inWindowRect.width(), topBottom[1])
            mIScrollLayout.notifyRectViewAddRectFromDeleted(rect2, position)
            mIScrollLayout.setIsCanLongClick(true) //设置为true后只要仍满足长按条件，则可以重启长按。注意：这条语句位置必须在通知RectView后调用
        }

        if (mIsBack) { //抬起手后，回到原位置的时候通知TimeScrollView滑到原来矩形的中心高度
            mIScrollLayout.notifyTimeScrollViewScrollToInitialHeight(mIScrollLayout.getRectImgViewInitialRect().centerY())
        }else { //抬起手后，在没有回到原位置的时候通知TimeScrollView自动滑到适宜的高度
            mIScrollLayout.notifyTimeScrollViewScrollToSuitableHeight()
        }
    }

    private var mIsBack = false
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
            val initialRect = mIScrollLayout.getRectImgViewInitialRect()
            val preUpperLimit = mRectManger.getClickUpperLimit()
            val preLowerLimit = mRectManger.getClickLowerLimit()
            val nowUpperLimit = mRectManger.getUpperLimit(bottom, nowPosition)
            val nowLowerLimit = mRectManger.getLowerLimit(top, nowPosition)
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
                    if (bottom < mData.mRectViewTop || top > mData.mRectViewBottom) { //在RectViewTop以上或mRectViewBottom以下
                        mIsBack = true
                        val correctTop = getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, prePosition, insideUpY)
                        intArrayOf(prePositionLeft, correctTop, prePosition)
                    }else {
                        val lowerLimit = mRectManger.getLowerLimit(nowUpperLimit, nowPosition)
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
        return mIScrollLayout.getRectViewInWindowLeftRight(position)[0]
    }

    /**
     * 注意：只用于在一个RectView中上下滑动
     *
     * 返回整体移动放手后矩形该放置的位置的内部高度值
     * @param rawRect 传入左右值为相对于屏幕坐标，上下值为内部坐标的Rect
     * @return 内部高度值
     */
    private fun getInsideTop(rawRect: Rect, position: Int, insideUpY: Int): Int {
        val top = rawRect.top
        val bottom = rawRect.bottom
        val rectHeight = rawRect.height()
        val initialRect = mIScrollLayout.getRectImgViewInitialRect()
        val preUpperLimit = mRectManger.getClickUpperLimit()
        val preLowerLimit = mRectManger.getClickLowerLimit()
        val nowUpperLimit = mRectManger.getUpperLimit(bottom, position)
        val nowLowerLimit = mRectManger.getLowerLimit(top, position)
        //每个if对应了一种情况，具体请以序号看纸上的草图
        return if (rectHeight <= nowLowerLimit - nowUpperLimit) {
            if (nowLowerLimit in top..bottom) { //1
                mIsBack = false
                nowLowerLimit - rectHeight
            }else if (nowUpperLimit in top..bottom) { //2
                mIsBack = false
                nowUpperLimit
            }else { //包括了3、4、5、6、7
                if (bottom < mData.mRectViewTop || top > mData.mRectViewBottom) { //在RectViewTop以上或mRectViewBottom以下
                    mIsBack = true
                    getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, position, insideUpY)
                }else {
                    val lowerLimit = mRectManger.getLowerLimit(nowUpperLimit, position)
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
        return if (insideUpY < mInitialY - mIScrollLayout.getUnconstrainedDistance()) { //说明矩形向上移动了
            mTime.getCorrectTopHeight(rect.top, upperLimit, position, mData.mTimeInterval)
        }else if (insideUpY > mInitialY + mIScrollLayout.getUnconstrainedDistance()){ //说明矩形向下移动了
            mTime.getCorrectBottomHeight(rect.bottom, lowerLimit, position, mData.mTimeInterval) - rect.height()
        }else {
            mTime.getCorrectTopHeight(rect.top, upperLimit, position, 1)
        }
    }
}