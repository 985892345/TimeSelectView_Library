package com.ndhzs.timeselectview.weight.timeselectview.viewinterface

import android.view.ViewGroup

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeselectview.weight.timeselectview.layout.ChildLayout]
 */
interface IChildLayout {
    fun addRectView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
    fun addSeparatorLineView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
}