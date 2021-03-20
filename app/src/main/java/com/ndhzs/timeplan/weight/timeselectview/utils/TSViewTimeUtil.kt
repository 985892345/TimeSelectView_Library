package com.ndhzs.timeplan.weight.timeselectview.utils

import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import java.util.*

class TSViewTimeUtil(util: TimeSelectViewUtil) {

    companion object {
        /**
         * 刷新当前时间高度的间隔时间
         */
        const val DELAY_NOW_TIME_REFRESH = 30000L

        /**
         * 回到当前时间的延缓时间
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

    fun getNowTimeHeight(): Int {
        var nowTime = getNowTime()
        if (nowTime < mStartHour) {
            nowTime += 24
        }
        return (mExtraHeight + (nowTime - mStartHour) * mIntervalHeight).toInt()
    }
}