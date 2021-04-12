package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Rect
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
interface ITimeScrollView {
    fun addScrollLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun slideDrawRect(insideY: Int)
    fun slideRectImgView(x: Int, y: Int)
    fun getOuterTop(): Int
    fun getOuterBottom(): Int
    fun entireMoveStart(rect: Rect, bean: TSViewBean)
}