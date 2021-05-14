package com.ndhzs.timeselectview.weight.timeselectview.viewinterface

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeselectview.weight.timeselectview.utils.TSViewTimeUtil]
 */
interface ITSViewTimeUtil {
    fun getNowTimeHeight(): Int
    fun getNowTimeHeight(position: Int) : Int
    fun getTimeHeight(time: Float): Int
    fun getTime(insideY: Int, position: Int): String
    fun getDiffTime(top: Int, bottom: Int): String
    fun getHour(insideY: Int, position: Int): Int
    fun getMinute(InsideY: Int): Int
    fun getMinuteTopHeight(minute: Int): Int
    fun getMinuteBottomHeight(minute: Int): Int
    fun getCorrectTopHeight(insideTopY: Int, upperLimit: Int, position: Int, timeInterval: Int): Int
    fun getCorrectTopHeight(insideTopY: Int, upperLimit: Int, lowerLimit: Int, position: Int, dTime: String): IntArray
    fun getCorrectTopHeight(startTime: String, upperLimit: Int, position: Int, timeInterval: Int): Int
    fun getCorrectBottomHeight(insideBottomY: Int, lowerLimit: Int, position: Int, timeInterval: Int): Int
    fun getCorrectBottomHeight(insideBottomY: Int, upperLimit: Int, lowerLimit: Int, position: Int, dTime: String): IntArray
    fun getCorrectBottomHeight(endTime: String, lowerLimit: Int, position: Int, timeInterval: Int): Int
    fun getStartTime(insideBottomY: Int, dTime: String, position: Int): String
    fun getStartTime(endTime: String, dTime: String, position: Int): String
    fun getEndTime(insideTopY: Int, dTime: String, position: Int): String
    fun getEndTime(startTime: String, dTime: String): String
}