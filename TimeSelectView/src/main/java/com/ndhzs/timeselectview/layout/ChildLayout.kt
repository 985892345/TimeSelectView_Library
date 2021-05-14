package com.ndhzs.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.ndhzs.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeselectview.viewinterface.IChildLayout

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [ParentLayout]之下，[com.ndhzs.timeselectview.layout.view.RectView]之上
 */
@SuppressLint("ViewConstructor")
internal class ChildLayout(context: Context, iChildLayout: IChildLayout, data: TSViewInternalData, position: Int) : FrameLayout(context) {

    private val mData = data
    private val mIChildLayout = iChildLayout
    private val mPosition = position

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