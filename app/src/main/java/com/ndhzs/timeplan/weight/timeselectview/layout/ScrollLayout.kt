package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IScrollLayout

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 * @description [TimeScrollView]之下，[ParentLayout]之上
 */
@SuppressLint("ViewConstructor")
class ScrollLayout(context: Context, iScrollLayout: IScrollLayout, data: TSViewInternalData) : FrameLayout(context) {

    init {
        val lp1 = LayoutParams(data.mAllTimelineWidth, LayoutParams.MATCH_PARENT)
        lp1.gravity = Gravity.CENTER
        iScrollLayout.addParentLayout(lp1, this)
        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        iScrollLayout.addRectImgView(lp2, this)
    }
}