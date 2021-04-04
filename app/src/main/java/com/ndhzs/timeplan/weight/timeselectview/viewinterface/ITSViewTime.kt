package com.ndhzs.timeplan.weight.timeselectview.viewinterface

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
interface ITSViewTime {
    fun getNowTimeHeight(): Int
    fun getTimeHeight(time: Float): Int
    fun getTime(y: Int): String
    fun getDiffTime(top: Int, bottom: Int): String
    fun getHour(y: Int): Int
    fun getMinute(y: Int): Int
    fun getMinuteTopHeight(minute: Int): Int
    fun getMinuteBottomHeight(minute: Int): Int
}