package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.view.ViewGroup

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 * @description
 */
interface IScrollLayout {
    fun addParentLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addRectImgView(lp: ViewGroup.LayoutParams, v: ViewGroup)
}