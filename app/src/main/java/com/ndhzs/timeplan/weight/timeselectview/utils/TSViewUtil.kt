package com.ndhzs.timeplan.weight.timeselectview.utils

import android.content.Context
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.TimeSelectView
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.BackCardView
import com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.RectImgView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.RectView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.rect.RectDraw
import com.ndhzs.timeplan.weight.timeselectview.utils.rect.RectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IChildLayout
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectImgView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITimeScrollView

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
class TSViewUtil(context: Context, data: TSViewInternalData, timeSelectView: TimeSelectView) : ITSView, ITimeScrollView, IChildLayout, IRectImgView {

    private val mTime = TSViewTimeUtil(data)
    private val mRectDraw = RectDraw(data)
    private val mRectManger = RectManger(data)

    private val mTimeSelectView = timeSelectView
    private val mBackCardView = BackCardView(context, data)
    private val mRectImgView = RectImgView(context, this, data, mTime, mRectDraw)
    private val mTimeScrollView = TimeScrollView(context, this, data, mTime, mRectManger)
    private val mChildLayout = ChildLayout(context, this, data, mTime)
    private val mRectView = RectView(context, data, mTime, mRectDraw, mRectManger)
    private val mSeparatorLineView = SeparatorLineView(context, data)


    override fun addBackCardView(lp: FrameLayout.LayoutParams, v: TimeSelectView) {
        v.addView(mBackCardView, lp)
    }



    override fun addRectImgView(lp: FrameLayout.LayoutParams, v: TimeSelectView) {
        v.addView(mRectImgView, lp)
    }
    override fun slideRectImgView(x: Int, y: Int) {
        mRectImgView.slideRectImgView(x, y)
    }
    override fun getOuterTop(): Int = mRectImgView.getOuterTop()
    override fun getOuterBottom(): Int = mRectImgView.getOuterBottom()



    override fun addTimeScrollView(lp: FrameLayout.LayoutParams, v: TimeSelectView) {
        v.addView(mTimeScrollView, lp)
    }
    override fun setOnClickListener(onClick: ((bean: TSViewBean) -> Unit)) {
        mTimeScrollView.setOnClickListener(onClick)
    }
    override fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit)) {
        mTimeScrollView.setOnTSVLongClickListener(onStart, onEnd)
    }
    override fun setLinkedViewPager2(viewPager2: ViewPager2) {
        mTimeScrollView.setLinkedViewPager2(viewPager2)
    }



    override fun addChildLayout(lp: FrameLayout.LayoutParams, v: TimeScrollView) {
        v.addView(mChildLayout, lp)
    }
    override fun showNowTimeLine() {
        mChildLayout.showNowTimeLine()
    }



    override fun addRectView(lp: FrameLayout.LayoutParams, v: ChildLayout) {
        v.addView(mRectView, lp)
    }
    override fun notifyAllRectRedraw() {
        mRectView.notifyAllRectRedraw()
    }
    override fun slideDrawRect(insideY: Int) {
        mRectView.slideDrawRect(insideY)
    }



    override fun addSeparatorLineView(lp: FrameLayout.LayoutParams, v: ChildLayout) {
        v.addView(mSeparatorLineView, lp)
    }

    override fun getScrollY(): Int = mTimeScrollView.scrollY
}