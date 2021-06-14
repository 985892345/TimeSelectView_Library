package com.ndhzs.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.viewinterface.IChildLayout

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [ParentLayout]之下，[com.ndhzs.timeselectview.layout.view.RectView]之上
 */
@SuppressLint("ViewConstructor")
internal class ChildLayout(
        context: Context,
        private val iChildLayout: IChildLayout,
        private val attrs: TSViewAttrs,
        position: Int
) : FrameLayout(context) {

    init {
        val lp = LayoutParams(attrs.mRectViewWidth, LayoutParams.MATCH_PARENT)
        lp.leftMargin = this.attrs.mIntervalLeft
        attachViewToParent(iChildLayout.getRectView(position), -1, lp)
        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        attachViewToParent(iChildLayout.getSeparatorLineView(position), -1, lp2)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMS = MeasureSpec.makeMeasureSpec(attrs.mInsideTotalHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMS)
    }
}