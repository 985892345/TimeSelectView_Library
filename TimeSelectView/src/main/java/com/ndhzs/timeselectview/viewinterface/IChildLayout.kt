package com.ndhzs.timeselectview.viewinterface

import android.view.ViewGroup

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeselectview.layout.ChildLayout]
 */
internal interface IChildLayout {
    fun addRectView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
    fun addSeparatorLineView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
}