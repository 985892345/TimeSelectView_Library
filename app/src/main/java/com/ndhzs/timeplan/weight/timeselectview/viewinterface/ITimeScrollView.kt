package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
interface ITimeScrollView {
    fun addChildLayout(lp: FrameLayout.LayoutParams, v: TimeScrollView)
    fun slideDrawRect(insideY: Int)
    fun slideRectImgView(x: Int, y: Int)
    fun getOuterTop(): Int
    fun getOuterBottom(): Int
}