package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.view.ViewGroup

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/12
 * @description
 */
interface IAddView {
    fun addBackCardView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addTimeScrollView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addScrollLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addParentLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addRectImgView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addChildLayout(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
    fun addRectView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
    fun addSeparatorLineView(lp: ViewGroup.LayoutParams, v: ViewGroup, position: Int)
}