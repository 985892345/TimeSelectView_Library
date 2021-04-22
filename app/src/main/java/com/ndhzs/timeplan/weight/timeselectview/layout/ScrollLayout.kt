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
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 * @description 处理整体移动
 * [TimeScrollView]之下，
 * [ParentLayout]、[com.ndhzs.timeplan.weight.timeselectview.layout.view.RectImgView]之上
 */
@SuppressLint("ViewConstructor")
class ScrollLayout(context: Context, iScrollLayout: IScrollLayout, data: TSViewInternalData, time: ITSViewTime, rectManger: IRectManger) : FrameLayout(context) {

    init {
        val lp1 = LayoutParams(data.mAllTimelineWidth, LayoutParams.MATCH_PARENT)
        lp1.gravity = Gravity.CENTER
        iScrollLayout.addParentLayout(lp1, this)
        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        iScrollLayout.addRectImgView(lp2, this)
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
                val rawRect = mIScrollLayout.getRectImgViewRawRect()
                mNowPosition = mIScrollLayout.getRectViewPosition(rawRect.centerX())
                val isOverLeft = rawRect.right < mIScrollLayout.getChildLayoutLocation(0).left
                val isOverRight = rawRect.left > mIScrollLayout.getChildLayoutLocation(mData.mTSViewAmount - 1).right
                if (isOverLeft || isOverRight || mNowPosition == null) {
                    mIScrollLayout.setIsCanLongClick(true)
                    mIScrollLayout.deleteRectImgView()
                    mData.mDataChangeListener?.onDataDelete(mIScrollLayout.getDeleteBean())
                }else {
                    val prePosition = mIScrollLayout.getPreRectViewPosition()
                    val rawLeftAndInsideTop = getRawLeftAndInsideTop(rawRect, prePosition, mNowPosition!!, y)
                    val rawFinalLeft = rawLeftAndInsideTop[0]
                    val insideFinalTop = rawLeftAndInsideTop[1]
                    val position = rawLeftAndInsideTop[2]
                    mIScrollLayout.slideEndRectImgView(rawFinalLeft, insideFinalTop) {
                        val topBottom = if (y <= mInitialY) { //说明矩形向上移动
                            mTime.getCorrectTopHeight(insideFinalTop,
                                    insideFinalTop,
                                    mRectManger.getLowerLimit(insideFinalTop, position),
                                    position,
                                    mRectManger.getDeletedBean().diffTime)
                        }else { //说明矩形向下移动
                            //因为是向下滑动的，所以之前计算的是正确的bottom值
                            val correctBottom = insideFinalTop + rawRect.height()
                            mTime.getCorrectBottomHeight(correctBottom,
                                    mRectManger.getUpperLimit(correctBottom, position),
                                    correctBottom,
                                    position,
                                    mRectManger.getDeletedBean().diffTime)
                        }
                        val times = mIScrollLayout.getStartEndDTime(topBottom[0], topBottom[1], position)
                        val bean = mIScrollLayout.getDeleteBean()
                        bean.startTime = times[0]
                        bean.endTime = times[1]
                        bean.diffTime = times[2]
                        mData.mDataChangeListener?.onDataAlter(bean)
                        val rect2 = Rect(0, topBottom[0], rawRect.width(), topBottom[1])
                        mIScrollLayout.notifyRectViewAddRectFromDeleted(rect2, position)
                        mIScrollLayout.setIsCanLongClick(true) //设置为true后只要仍满足长按条件，则可以重启长按。注意：位置必须在通知RectView后调用
                    }
                }
                mData.mCondition = NULL
            }
        }
        return true
    }

    private fun getRawLeftAndInsideTop(rawRect: Rect, prePosition: Int, nowPosition: Int, insideUpY: Int): IntArray {
        if (prePosition == nowPosition) {
            val rawLeft = getRawLeft(prePosition)
            val insideTop = getInsideTop(rawRect, prePosition, insideUpY)
            return intArrayOf(rawLeft, insideTop, prePosition)
        }else {
            //以下是整体移动到另一个RectView
            val top = rawRect.top
            val bottom = rawRect.bottom
            val rectHeight = rawRect.height()
            val initialRect = mIScrollLayout.getRectImgViewInitialRect()
            val preUpperLimit = mRectManger.getClickUpperLimit()
            val preLowerLimit = mRectManger.getClickLowerLimit()
            val nowUpperLimit = mRectManger.getUpperLimit(bottom, nowPosition)
            val nowLowerLimit = mRectManger.getLowerLimit(top, nowPosition)
            val prePositionLeft = getRawLeft(prePosition)
            val nowPositionLeft = getRawLeft(nowPosition)
            return if (rectHeight <= nowLowerLimit - nowUpperLimit) {
                if (nowLowerLimit in top..bottom) { //1
                    intArrayOf(nowPositionLeft, nowLowerLimit - rectHeight, nowPosition)
                }else if (nowUpperLimit in top..bottom) { //2
                    intArrayOf(nowPositionLeft, nowUpperLimit, nowPosition)
                }else { //包括了3、4、5、6、7
                    if (bottom < mData.mRectViewTop || top > mData.mRectViewBottom) { //在RectViewTop以上或mRectViewBottom以下
                        val correctTop = getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, prePosition, insideUpY)
                        intArrayOf(prePositionLeft, correctTop, prePosition)
                    }else {
                        val lowerLimit = mRectManger.getLowerLimit(nowUpperLimit, nowPosition)
                        if (lowerLimit == nowLowerLimit) { //3、5-1、7-1
                            val correctTop = getCorrectTopHeight(rawRect, nowUpperLimit, nowLowerLimit, nowPosition, insideUpY)
                            intArrayOf(nowPositionLeft, correctTop, nowPosition)
                        } else { //4、5-2、6、7-2
                            val correctTop = getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, prePosition, insideUpY)
                            intArrayOf(prePositionLeft, correctTop, prePosition)
                        }
                    }
                }
            }else { //包括 a 的所有情况
                val correctTop = getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, prePosition, insideUpY)
                intArrayOf(prePositionLeft, correctTop, prePosition)
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
                nowLowerLimit - rectHeight
            }else if (nowUpperLimit in top..bottom) { //2
                nowUpperLimit
            }else { //包括了3、4、5、6、7
                if (bottom < mData.mRectViewTop || top > mData.mRectViewBottom) { //在RectViewTop以上或mRectViewBottom以下
                    getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, position, insideUpY)
                }else {
                    val lowerLimit = mRectManger.getLowerLimit(nowUpperLimit, position)
                    if (lowerLimit == nowLowerLimit) { //3、5-1、7-1
                        getCorrectTopHeight(rawRect, nowUpperLimit, nowLowerLimit, position, insideUpY)
                    } else { //4、5-2、6、7-2
                        getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, position, insideUpY)
                    }
                }
            }
        }else { //包括 a 的所有情况
            getCorrectTopHeight(initialRect, preUpperLimit, preLowerLimit, position, insideUpY)
        }
    }

    private fun getCorrectTopHeight(rect: Rect, upperLimit: Int, lowerLimit: Int, position: Int, insideUpY: Int): Int {
        //以下用来判断是否上下移动后而用时间间隔数计算得出正确的top值
        return if (insideUpY < mInitialY) { //说明矩形向上移动了
            mTime.getCorrectTopHeight(rect.top, upperLimit, position)
        }else { //说明矩形向下移动了
            mTime.getCorrectBottomHeight(rect.bottom, lowerLimit, position) - rect.height()
        }
    }
}