package com.ndhzs.timeselectview.viewinterface

import com.ndhzs.timeselectview.layout.view.RectView
import com.ndhzs.timeselectview.layout.view.SeparatorLineView

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeselectview.layout.ChildLayout]
 */
internal interface IChildLayout {
    fun getRectView(position: Int): RectView
    fun getSeparatorLineView(position: Int): SeparatorLineView
}