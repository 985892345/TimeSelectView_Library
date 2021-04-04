package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.view.NowTimeLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IChildLayout
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
@SuppressLint("ViewConstructor")
class ChildLayout(context: Context, iChildLayout: IChildLayout, data: TSViewInternalData, time: ITSViewTime) : FrameLayout(context) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine() {
        if (mNowTimeLineView == null) {
            mNowTimeLineView = NowTimeLineView(context, mData, mTime)
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            addView(mNowTimeLineView, lp) //由于这个只会在一个TimeSelectView中添加，所以就不写在TSViewUtil中
        }
    }



    private val mIChildLayout = iChildLayout
    private val mData = data
    private val mTime = time

    private var mNowTimeLineView: NowTimeLineView? = null

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp.leftMargin = mData.mIntervalLeft
        mIChildLayout.addRectView(lp, this)
        val lp2 = LayoutParams(lp)
        lp2.leftMargin = 0
        mIChildLayout.addSeparatorLineView(lp2, this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mData.mTotalHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}