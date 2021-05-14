package com.ndhzs.timeselectview.weight.timeselectview.utils

import android.content.Context
import android.graphics.Rect
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeselectview.weight.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.weight.timeselectview.layout.*
import com.ndhzs.timeselectview.weight.timeselectview.layout.view.RectImgView
import com.ndhzs.timeselectview.weight.timeselectview.layout.view.RectView
import com.ndhzs.timeselectview.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeselectview.weight.timeselectview.utils.rect.RectDraw
import com.ndhzs.timeselectview.weight.timeselectview.utils.rect.RectManger
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 所有View的管理工具
 */
class TSViewObjectsManger(context: Context, data: TSViewInternalData, firstDay: String) {

    var mVpPosition = -1
    private val mFirstDay = firstDay

    private val mContext = context
    private val mData = data
    private val mTime = TSViewTimeUtil(data)
    private val mRectDraw = RectDraw(data)
    private val mRectManger = RectManger(data, mTime, { initialSideY, upperLimit, lowerLimit, position ->
        mRectViews[position].clickEmptyStart(initialSideY, upperLimit, lowerLimit)

    }, { rect, bean, position ->
        mRectImgView.start(rect, bean, position)
        notifyAllRectViewRedraw()

    }, { rect, bean, initialSideY, upperLimit, lowerLimit, position ->
        mRectViews[position].clickTopAndBottomStart(rect, bean, initialSideY, upperLimit, lowerLimit)
    })

    private val mChildLayouts = ArrayList<ChildLayout>()
    private val mRectViews = ArrayList<RectView>()
    private val mSeparatorLineViews = ArrayList<SeparatorLineView>()

    private val mParentLayout = ParentLayout(context, My4IParentLayout(), data)
    private val mRectImgView = RectImgView(context, My5IRectImgView(), data, mTime, mRectDraw)

    private val mStickerLayout = StickerLayout(context, My4IStickerLayout(), data, mTime)

    private val mScrollLayout = ScrollLayout(context, My3IScrollLayout(), data, mTime, mRectManger)
    private val mBackCardView = BackCardView(context, data)

    private val mTimeScrollView = TimeScrollView(context, My2ITimeScrollView(), data, mTime, mRectManger)

    private fun getChildLayoutInWindowLeftRight(position: Int): IntArray {
        val location = IntArray(2)
        mChildLayouts[position].getLocationInWindow(location)
        location[1] = location[0] + mChildLayouts[position].width
        return location
    }

    private fun getChildLayoutOnScreenLeftRight(position: Int): IntArray {
        val location = IntArray(2)
        mChildLayouts[position].getLocationOnScreen(location)
        location[1] = location[0] + mChildLayouts[position].width
        return location
    }

    private fun getRectViewPosition(inWindowX: Int): Int? {
        for (i in 0 until mData.mTSViewAmount) {
            val leftRight = getRectViewInWindowLeftRight(i)
            if (inWindowX < leftRight[0]) {
                return null
            }else if (inWindowX in leftRight[0]..leftRight[1]) {
                return i
            }
        }
        return null
    }

    private fun getRectViewInWindowLeftRight(position: Int): IntArray {
        val location = IntArray(2)
        mRectViews[position].getLocationInWindow(location)
        location[1] = location[0] + mRectViews[position].width
        return location
    }

    private fun getRectViewOnScreenLeftRight(position: Int): IntArray {
        val location = IntArray(2)
        mRectViews[position].getLocationOnScreen(location)
        location[1] = location[0] + mRectViews[position].width
        return location
    }

    private fun getDiffBetweenInWindowAndOnScreen(): Int {
        return getRectViewOnScreenLeftRight(0)[0] - getRectViewInWindowLeftRight(0)[0]
    }

    private fun notifyAllRectViewRedraw() {
        mRectViews.forEach {
            it.notifyRectRedraw()
        }
    }


    inner class My1IVpLayout : IVpLayout {
        override fun addBackCardView(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mBackCardView, lp)
        }

        override fun addTimeScrollView(lp: ViewGroup.LayoutParams, v: ViewGroup, viewPager2: ViewPager2?) {
            v.addView(mTimeScrollView, lp)
            if (viewPager2 != null) {
                mTimeScrollView.setLinkedViewPager2(viewPager2)
            }
        }

        override fun showNowTimeLine() {
            mStickerLayout.showNowTimeLine()
        }

        override fun onViewDetachedFromWindow() {
            mTimeScrollView.fastBackCurrentTime()
        }

        override fun onViewRecycled() {
            mVpPosition = -1 //防止onScrollListener接口回调
        }

        override fun notifyAllRectRefresh() {
            return this@TSViewObjectsManger.notifyAllRectViewRedraw()
        }

        override fun initializeBean(taskBeans: MutableList<TSViewTaskBean>) {
            mRectManger.initializeBean(taskBeans)
            notifyAllRectRefresh()
        }

        override fun backCurrentTime() {
            mTimeScrollView.backCurrentTime()
        }

        override fun cancelAutoBackCurrentTime() {
            mTimeScrollView.cancelAfterUpBackCurrentTimeRun()
        }

        override fun timeLineScrollTo(scrollY: Int) {
            mTimeScrollView.scrollY = scrollY
        }

        override fun timeLineSlowlyScrollTo(scrollY: Int) {
            mTimeScrollView.slowlyScrollTo(scrollY)
        }

        override fun notifyRectViewDataChanged() {
            mRectManger.refreshData()
            notifyAllRectRefresh()
        }
    }

    inner class My2ITimeScrollView : ITimeScrollView {
        override fun addScrollLayout(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mScrollLayout, lp)
        }

        override fun slideDrawRect(insideY: Int, position: Int) {
            mRectViews[position].slideDrawRect(insideY)
        }

        override fun slideRectImgView(dx: Int, dy: Int) {
            mRectImgView.slideRectImgView(dx, dy)
        }

        override fun getOuterTop(): Int {
            return mRectImgView.getInsideTop() - mTimeScrollView.scrollY
        }

        override fun getOuterBottom(): Int {
            return mRectImgView.getInsideBottom() - mTimeScrollView.scrollY
        }

        override fun getRectViewPosition(onScreenX: Int): Int? {
            return this@TSViewObjectsManger.getRectViewPosition(onScreenX - getDiffBetweenInWindowAndOnScreen())
        }

        override fun getVpPosition(): Int {
            return mVpPosition
        }

        override fun onLongClickStartButNotMove(position: Int) {
            mRectViews[position].recoverRectFromDeleted()
            mRectImgView.forcedEnd()
        }

        override fun onScrollChanged(scrollY: Int) {
            mRectImgView.boundaryRefresh(scrollY, mData.mInsideTotalHeight, mTimeScrollView.height)
        }
    }

    inner class My3IScrollLayout : IScrollLayout {
        override fun addParentLayout(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mParentLayout, lp)
        }

        override fun addStickerLayout(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mStickerLayout, lp)
        }

        override fun getRectViewPosition(inWindowX: Int): Int? {
            return this@TSViewObjectsManger.getRectViewPosition(inWindowX)
        }

        override fun getPreRectViewPosition(): Int {
            return mTimeScrollView.mClickRectViewPosition!!
        }

        override fun getRectViewInWindowLeftRight(position: Int): IntArray {
            return this@TSViewObjectsManger.getRectViewInWindowLeftRight(position)
        }

        override fun getChildLayoutInWindowLeftRight(position: Int): IntArray {
            return this@TSViewObjectsManger.getChildLayoutInWindowLeftRight(position)
        }

        override fun getUnconstrainedDistance(): Int {
            return mRectViews[0].mUnconstrainedDistance
        }

        override fun getRectImgViewInWindowRect(): Rect {
            return mRectImgView.getInWindowRect()
        }

        override fun getRectImgViewInitialRect(): Rect {
            return mRectImgView.getRawInitialRect()
        }

        override fun getStartEndDTime(top: Int, bottom: Int, position: Int): Array<String> {
            val startTime = mTime.getTime(top, position)
            val endTime = mTime.getTime(bottom, position)
            val dTime = mTime.getDiffTime(top, bottom)
            return arrayOf(startTime, endTime, dTime)
        }

        override fun slideRectImgView(dx: Int, dy: Int) {
            mRectImgView.slideRectImgView(dx, dy)
        }

        override fun slideEndRectImgView(inWindowFinalLeft: Int, insideFinalTop: Int, onEndListener: () -> Unit?) {
            mRectImgView.over(inWindowFinalLeft, insideFinalTop, onEndListener)
        }

        override fun deleteRectImgView(onEndListener: () -> Unit?) {
            mRectImgView.delete(onEndListener)
        }

        override fun setIsCanLongClick(boolean: Boolean) {
            mTimeScrollView.setIsCanLongClick(boolean)
        }

        override fun notifyRectViewRedraw() {
            return this@TSViewObjectsManger.notifyAllRectViewRedraw()
        }

        override fun notifyRectViewAddRectFromDeleted(rect: Rect, position: Int) {
            mRectViews[position].addRectFromDeleted(rect)
        }

        override fun notifyTimeScrollViewScrollToSuitableHeight() {
            mTimeScrollView.scrollToSuitableHeight()
        }

        override fun notifyTimeScrollViewScrollToInitialHeight(height: Int) {
            mTimeScrollView.scrollToSuitableHeight(height)
        }
    }

    inner class My4IParentLayout : IParentLayout {
        override fun addChildLayout(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int) {
            val childLayout = ChildLayout(mContext, My5IChildLayout(), mData, position)
            mChildLayouts.add(childLayout)
            v.addView(childLayout, lp)
        }
    }
    
    inner class My4IStickerLayout : IStickerLayout {
        override fun addRectImgView(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mRectImgView, lp)
        }

        override fun getChildLayoutWidth(): Int {
            return mChildLayouts[0].width
        }

        override fun getChildLayoutToStickerLayoutDistance(position: Int): Int {
            val mChildLayoutLocation = IntArray(2)
            val mStickerLayoutLocation = IntArray(2)
            mChildLayouts[position].getLocationInWindow(mChildLayoutLocation)
            mRectImgView.getLocationInWindow(mStickerLayoutLocation)
            return mChildLayoutLocation[0] - mStickerLayoutLocation[0]
        }
    }

    inner class My5IRectImgView : IRectImgView {
        override fun getRectViewToRectImgViewDistance(position: Int): Int {
            val mRectViewLocation = IntArray(2)
            val mRectImgViewLocation = IntArray(2)
            mRectViews[position].getLocationInWindow(mRectViewLocation)
            mRectImgView.getLocationInWindow(mRectImgViewLocation)
            return mRectViewLocation[0] - mRectImgViewLocation[0]
        }

        override fun getRectViewInterval(): Int {
            return if (mData.mTSViewAmount == 1) {
                Int.MAX_VALUE
            }else {
                getRectViewInWindowLeftRight(1)[0] - getRectViewInWindowLeftRight(0)[0]
            }
        }
    }

    inner class My5IChildLayout : IChildLayout {
        override fun addRectView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int) {
            val rectView = RectView(mContext, mData, mTime, mRectDraw, mRectManger.MyIRectViewRectManger(), My6IRectView(), position)
            mRectViews.add(rectView)
            v.addView(rectView, lp)
        }

        override fun addSeparatorLineView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int) {
            val separatorLineView = SeparatorLineView(mContext, mData, position)
            mSeparatorLineViews.add(separatorLineView)
            v.addView(separatorLineView, lp)
        }
    }

    inner class My6IRectView : IRectView {

        override fun notifyAllRectViewRefresh() {
            return this@TSViewObjectsManger.notifyAllRectViewRedraw()
        }

        override fun notifyTimeScrollViewScrollToSuitableHeight() {
            mTimeScrollView.scrollToSuitableHeight()
        }

        override fun setIsCanLongClick(boolean: Boolean) {
            mTimeScrollView.setIsCanLongClick(boolean)
        }

        override fun getDay(): String {
            return TSViewTimeUtil.getDay(mFirstDay, mVpPosition)
        }
    }
}