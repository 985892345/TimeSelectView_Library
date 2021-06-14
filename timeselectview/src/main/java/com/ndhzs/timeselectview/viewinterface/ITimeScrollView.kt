package com.ndhzs.timeselectview.viewinterface

import com.ndhzs.timeselectview.layout.ScrollLayout

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeselectview.layout.TimeScrollView]
 */
internal interface ITimeScrollView {
    fun getScrollLayout(): ScrollLayout
    fun slideDrawRect(insideY: Int, position: Int)
    fun slideRectImgView(dx: Int, dy: Int)
    fun getOuterTop(): Int
    fun getOuterBottom(): Int
    fun getRectViewPosition(onScreenX: Int): Int?
    fun getVpPosition(): Int
    fun onLongClickStartButNotMove(position: Int)
    fun onScrollChanged(scrollY: Int)
}