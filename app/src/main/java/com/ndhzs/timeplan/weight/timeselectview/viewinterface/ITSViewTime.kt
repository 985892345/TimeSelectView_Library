package com.ndhzs.timeplan.weight.timeselectview.viewinterface

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeplan.weight.timeselectview.utils.TSViewTimeUtil]
 */
interface ITSViewTime {
    fun getNowTimeHeight(position: Int): Int
    fun getTimeHeight(time: Float, position: Int): Int
    fun getTime(insideY: Int, position: Int): String
    fun getDiffTime(top: Int, bottom: Int): String
    fun getHour(insideY: Int, position: Int): Int
    fun getMinute(InsideY: Int): Int
    fun getMinuteTopHeight(minute: Int): Int
    fun getMinuteBottomHeight(minute: Int): Int
    fun getCorrectTopHeight(insideY: Int, upperLimit: Int, position: Int): Int
    fun getCorrectTopHeight(time: String): Int
    fun getCorrectBottomHeight(insideY: Int, lowerLimit: Int, position: Int): Int
    fun getCorrectBottomHeight(time: String): Int
}