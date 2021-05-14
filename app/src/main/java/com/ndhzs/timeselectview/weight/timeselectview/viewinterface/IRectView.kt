package com.ndhzs.timeselectview.weight.timeselectview.viewinterface

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/19
 * @description
 */
interface IRectView {
    fun notifyAllRectViewRefresh()
    fun notifyTimeScrollViewScrollToSuitableHeight()
    fun setIsCanLongClick(boolean: Boolean)
    fun getDay(): String
}