package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.view.NowTimeLineView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.RectView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.TimeSelectViewUtil

@SuppressLint("ViewConstructor")
class ChildLayout(context: Context, util: TimeSelectViewUtil) : FrameLayout(context) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine() {
        if (mNowTimeLineView == null) {
            mNowTimeLineView = NowTimeLineView(context, mUtil)
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            addView(mNowTimeLineView, lp)
        }
    }

    fun notifyRectViewRedraw() {
        mRectView.invalidate()
    }



    private val mUtil = util
    private val mRectView = RectView(context, util)

    private val mSeparatorLineView = SeparatorLineView(context, util)

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp.leftMargin = util.mIntervalLeft
        addView(mRectView, lp)
        lp.leftMargin = 0
        addView(mSeparatorLineView, lp)
    }

    private var mNowTimeLineView: NowTimeLineView? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mUtil.mTotalHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}