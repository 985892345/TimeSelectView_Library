package com.ndhzs.timeplan.weight.timeselectview.utils

import android.content.Context
import android.graphics.Rect
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.TimeSelectView
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.*
import com.ndhzs.timeplan.weight.timeselectview.layout.view.RectImgView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.RectView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.rect.RectDraw
import com.ndhzs.timeplan.weight.timeselectview.utils.rect.RectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.*

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
class TSViewUtil(context: Context, data: TSViewInternalData, timeSelectView: TimeSelectView) {

    private val mContext = context
    private val mData = data
    private val mTime = TSViewTimeUtil(data)
    private val mRectDraw = RectDraw(data)
    private val mRectManger = RectManger(data)

    private val mTimeSelectView = timeSelectView

    private val mBackCardView = BackCardView(context, data)
    private val mTimeScrollView = TimeScrollView(context, My2ITimeScrollView(), data, mTime, mRectManger)

    private val mScrollLayout = ScrollLayout(context, My3IScrollLayout(), data)

    private val mParentLayout = ParentLayout(context, My4IParentLayout(), data)
    private val mRectImgView = RectImgView(context, My4IRectImgView(), data, mTime, mRectDraw)

    private val mChildLayouts = ArrayList<ChildLayout>()
    private val mRectViews = ArrayList<RectView>()
    private val mSeparatorLineViews = ArrayList<SeparatorLineView>()


    inner class My1ITSView : ITSView {
        override fun addBackCardView(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mBackCardView, lp)
        }

        override fun addTimeScrollView(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mTimeScrollView, lp)
        }

        override fun showNowTimeLine() {
            mChildLayouts.forEach {
                it.showNowTimeLine()
            }
        }

        override fun notifyAllRectRedraw() {
            mRectViews.forEach {
                it.notifyAllRectRedraw()
            }
        }

        override fun setOnClickListener(onClick: (bean: TSViewBean) -> Unit) {
            mTimeScrollView.setOnClickListener(onClick)
        }

        override fun setOnTSVLongClickListener(onStart: (condition: TSViewLongClick) -> Unit, onEnd: (condition: TSViewLongClick) -> Unit) {
            mTimeScrollView.setOnTSVLongClickListener(onStart, onEnd)
        }

        override fun setLinkedViewPager2(viewPager2: ViewPager2) {
            mTimeScrollView.setLinkedViewPager2(viewPager2)
        }

        override fun getOuterMinWidth(): Int {
            return mBackCardView.getMinWidth()
        }

        override fun getOuterMinHeight(): Int {
            return mBackCardView.getMinOuterHeight()
        }
    }

    inner class My2ITimeScrollView : ITimeScrollView {
        override fun addScrollLayout(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mScrollLayout, lp)
        }

        override fun slideDrawRect(insideY: Int) {
            mRectViews.forEach {
                it.slideDrawRect(insideY)
            }
        }

        override fun slideRectImgView(x: Int, y: Int) {
            mRectImgView.slideRectImgView(x, y)
        }

        override fun getOuterTop(): Int {
            return mRectImgView.getInsideTop() - mTimeScrollView.scrollY
        }

        override fun getOuterBottom(): Int {
            return mRectImgView.getInsideBottom() - mTimeScrollView.scrollY
        }

        override fun entireMoveStart(rect: Rect, bean: TSViewBean) {
            mRectImgView.start(rect, bean)
        }
    }

    inner class My3IScrollLayout : IScrollLayout {
        override fun addParentLayout(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mParentLayout, lp)
        }

        override fun addRectImgView(lp: ViewGroup.LayoutParams, v: ViewGroup) {
            v.addView(mRectImgView, lp)
        }
    }

    inner class My4IParentLayout : IParentLayout {
        override fun addChildLayout(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int) {
            val childLayout = ChildLayout(mContext, My5IChildLayout(), mData, mTime, position)
            mChildLayouts.add(childLayout)
            v.addView(childLayout, lp)
        }
    }

    inner class My4IRectImgView : IRectImgView {
        override fun getDividerLines(): IntArray {
            val array = mBackCardView.getDividerLines()
            repeat(array.size) {
                array[it] += mBackCardView.left
            }
            return array
        }

        override fun getScrollY(): Int {
            return mTimeScrollView.scrollY
        }
        override fun getRectViewToRectImgViewDistance(position: Int): Int {
            val mRectViewLocation = IntArray(2)
            val mRectImgViewLocation = IntArray(2)
            mRectViews[position].getLocationInWindow(mRectViewLocation)
            mRectImgView.getLocationInWindow(mRectImgViewLocation)
            return mRectViewLocation[0] - mRectImgViewLocation[0]
        }
    }

    inner class My5IChildLayout : IChildLayout {
        override fun addRectView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int) {
            val rectView = RectView(mContext, mData, mTime, mRectDraw, mRectManger, position)
            mRectViews.add(rectView)
            v.addView(rectView, lp)
        }

        override fun addSeparatorLineView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int) {
            val separatorLineView = SeparatorLineView(mContext, mData, position)
            mSeparatorLineViews.add(separatorLineView)
            v.addView(separatorLineView, lp)
        }
    }
}