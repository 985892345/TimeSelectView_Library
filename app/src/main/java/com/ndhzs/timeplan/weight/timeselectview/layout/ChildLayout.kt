package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.view.NowTimeLineView
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.rect.RectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IChildLayout
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [ParentLayout]之下，[com.ndhzs.timeplan.weight.timeselectview.layout.view.RectView]之上
 */
@SuppressLint("ViewConstructor")
class ChildLayout(context: Context, iChildLayout: IChildLayout, data: TSViewInternalData, time: ITSViewTime, position: Int) : FrameLayout(context) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine() {
        if (mNowTimeLineView == null) {
            mNowTimeLineView = NowTimeLineView(context, mData, mTime, mPosition)
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            addView(mNowTimeLineView, lp) //由于这个只会在一个TimeSelectView中添加，所以就不写在TSViewUtil中
        }
    }



    private val mData = data
    private val mTime = time
    private val mIChildLayout = iChildLayout
    private val mPosition = position

    private var mNowTimeLineView: NowTimeLineView? = null

    init {
        val lp = LayoutParams(data.mRectViewWidth, LayoutParams.MATCH_PARENT)
        lp.leftMargin = mData.mIntervalLeft
        mIChildLayout.addRectView(lp, this, position)
        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mIChildLayout.addSeparatorLineView(lp2, this, position)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMS = MeasureSpec.makeMeasureSpec(mData.mInsideTotalHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMS)
    }
}