package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.view.ViewGroup
import android.widget.FrameLayout
import com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
interface IChildLayout {
    fun addRectView(lp: FrameLayout.LayoutParams, v: ChildLayout)
    fun addSeparatorLineView(lp: FrameLayout.LayoutParams, v: ChildLayout)
}