package com.ndhzs.timeplan.weight.timeselectview.utils

import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import java.util.*

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来进行时间与高度转换的类
 */
class TSViewTimeUtil(util: TSViewUtil) {

    companion object {
        /**
         * 刷新当前时间高度的间隔时间
         */
        const val DELAY_NOW_TIME_REFRESH = 30000L

        /**
         * 回到当前时间高度的间隔时间
         */
        const val DELAY_BACK_CURRENT_TIME = 10000L
    }

    private val mStartHour = util.mStartHour
    private val mHLineWidth = SeparatorLineView.HORIZONTAL_LINE_WIDTH //水平线厚度
    private val mExtraHeight = util.mExtraHeight //上方或下方其中一方多余的高度
    private val mIntervalHeight = util.mIntervalHeight //一个小时的间隔高度
    val mEveryMinuteHeight = FloatArray(61)
    var mTimeInterval: Int = 15

    init {
        val everyMinuteWidth = mIntervalHeight / 60F //计算出一分钟要多少格，用小数表示
        for (i in 0..59) {
            mEveryMinuteHeight[i] = i * everyMinuteWidth
        }
        mEveryMinuteHeight[60] = mIntervalHeight.toFloat()
    }

    fun getNowTime(): Float {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val second = calendar[Calendar.SECOND]
        return hour + minute / 60F + second / 3600F
    }

    fun getTimeHeight(time: Float = getNowTime()): Int {
        var time1 = time
        if (time1 < mStartHour) {
            time1 += 24
        }
        return (mExtraHeight + (time1 - mStartHour) * mIntervalHeight).toInt()
    }

    fun getTime(y: Int): String {
        val h = getHour(y)
        val m = getMinute(y)
        return timeToString(h, m)
    }

    fun getDiffTime(top: Int, bottom: Int): String {
        val lastH = getHour(top)
        val lastM = getMinute(top)
        var h = getHour(bottom)
        var m = getMinute(bottom)
        if (m >= lastM) {
            m -= lastM
            h -= lastH
        }else {
            m += 60 - lastM
            h -= lastH + 1
        }
        return timeToString(h, m)
    }

    fun getHour(y: Int): Int {
        return if (y >= mExtraHeight) {
            (y - mExtraHeight) / mIntervalHeight + mStartHour
        }else {
            mStartHour - ((mExtraHeight - y) / mIntervalHeight + 1);
        }
    }

    fun getMinute(y: Int): Int {
        return if (y >= mExtraHeight) {
           ((y - mExtraHeight) % mIntervalHeight / mIntervalHeight.toFloat() * 60).toInt()
        }else {
            ((mIntervalHeight - (mExtraHeight - y) % mIntervalHeight) / mIntervalHeight.toFloat() * 60).toInt()
        }
    }

    private fun timeToString(hour: Int, minute: Int): String {
        val stH = when {
            hour < 10 -> {
                "0$hour"
            }hour < 24 -> {
                hour.toString()
            }else -> {
                "0${hour%24}"
            }
        }
        val stM: String = when {
            minute < 10 -> {
                "0$minute"
            }else -> {
                minute.toString()
            }
        }
        return "$stH:$stM"
    }
}