package com.ndhzs.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.viewinterface.IParentLayout

/**
 * [ScrollLayout]之下，[ChildLayout]之上
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/12
 */
@SuppressLint("ViewConstructor")
internal class ParentLayout(
        context: Context,
        iParentLayout: IParentLayout,
        attrs: TSViewAttrs
) : LinearLayout(context) {

    init {
        orientation = HORIZONTAL
        val lp = LayoutParams(attrs.mTimelineWidth, LayoutParams.MATCH_PARENT)
        attachViewToParent(iParentLayout.getChildLayout(0), -1, lp)

        val lp2 = LayoutParams(lp)
        lp2.leftMargin = attrs.mTimelineInterval
        for (i in 1 until attrs.mTSViewAmount) {
            attachViewToParent(iParentLayout.getChildLayout(i), -1, lp2)
        }
    }
}