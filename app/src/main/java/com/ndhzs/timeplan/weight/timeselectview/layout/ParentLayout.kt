package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IParentLayout

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/12
 * @description [ScrollLayout]之下，[ChildLayout]之上
 */
@SuppressLint("ViewConstructor")
class ParentLayout(context: Context, iParentLayout: IParentLayout, data: TSViewInternalData) : LinearLayout(context) {
    init {
        orientation = HORIZONTAL
        val lp = LayoutParams(data.mTimelineWidth, data.mInsideTotalHeight)
        iParentLayout.addChildLayout(lp, this, 0)
        val lp2 = LayoutParams(lp)
        lp2.leftMargin = data.mTimelineInterval
        for (i in 1 until data.mTSViewAmount) {
            iParentLayout.addChildLayout(lp2, this, i)
        }
    }
}