package com.ndhzs.timeselectview.viewinterface

import java.util.*

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/19
 * @description
 */
internal interface IRectView {
    fun notifyAllRectViewRefresh()
    fun notifyTimeScrollViewScrollToSuitableHeight()
    fun setIsCanLongClick(boolean: Boolean)
    fun getDay(): Calendar
}