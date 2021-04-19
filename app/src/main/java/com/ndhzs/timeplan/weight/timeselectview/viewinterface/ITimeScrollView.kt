package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Rect
import android.view.ViewGroup
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView]
 */
interface ITimeScrollView {
    fun addScrollLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun slideDrawRect(insideY: Int, position: Int)
    fun slideRectImgView(dx: Int, dy: Int)
    fun getOuterTop(): Int
    fun getOuterBottom(): Int
    fun getRectViewPosition(rowX: Int): Int?
}