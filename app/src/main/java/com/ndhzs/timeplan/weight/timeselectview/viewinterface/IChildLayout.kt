package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.view.ViewGroup
import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout]
 */
interface IChildLayout {
    fun addRectView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
    fun addSeparatorLineView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
}