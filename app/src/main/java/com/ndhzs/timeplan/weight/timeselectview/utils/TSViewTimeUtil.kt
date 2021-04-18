package com.ndhzs.timeplan.weight.timeselectview.utils

import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来进行时间与高度转换的类
 */
class TSViewTimeUtil(data: TSViewInternalData) : ITSViewTime {

    companion object {
        /**
         * 刷新当前时间高度的间隔时间
         */
        const val DELAY_NOW_TIME_REFRESH = 30000L

        /**
         * 回到当前时间高度的间隔时间
         */
        const val DELAY_BACK_CURRENT_TIME = 10000L

        /**
         * 用于[getTimeHeight]中，代表返回ScrollView应滑到的高度
         */
        const val SCROLLVIEW_HEIGHT = -1
    }

    private val mStartHour = data.mStartHour
    private val mTimeInterval = data.mTimeInterval
    private val mTimelineRange = data.mTimelineRange
    private val mHLineWidth = SeparatorLineView.HORIZONTAL_LINE_WIDTH //水平线厚度
    private val mExtraHeight = data.mExtraHeight //上方或下方其中一方多余的高度
    private val mIntervalHeight = data.mIntervalHeight //一个小时的间隔高度
    private val mEveryMinuteHeight = FloatArray(61)

    init {
        val everyMinuteWidth = mIntervalHeight / 60F //计算出一分钟要多少格，用小数表示
        for (i in 0..59) {
            mEveryMinuteHeight[i] = i * everyMinuteWidth
        }
        mEveryMinuteHeight[60] = mIntervalHeight.toFloat()
    }

    override fun getNowTimeHeight(position: Int): Int {
        return getTimeHeight(getNowTime(), position)
    }

    override fun getTimeHeight(time: Float, position: Int): Int {
        var t = time
        if (t < mStartHour) {
            t += 24
        }
        t -= mStartHour
        return when (position) {
            SCROLLVIEW_HEIGHT -> {
                while (t > mTimelineRange) {
                    t -= mTimelineRange
                }
                (mExtraHeight + t * mIntervalHeight).toInt()
            }
            else -> {
                (mExtraHeight + (t - mTimelineRange * position) * mIntervalHeight).toInt()
            }
        }
    }

    override fun getTime(insideY: Int, position: Int): String {
        val h = getHour(insideY, position)
        val m = getMinute(insideY)
        return timeToString(h, m)
    }

    override fun getDiffTime(top: Int, bottom: Int): String {
        val lastH = getHour(top, 0)
        val lastM = getMinute(top)
        var h = getHour(bottom, 0)
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

    override fun getHour(insideY: Int, position: Int): Int {
        return if (insideY >= mExtraHeight) {
            (insideY - mExtraHeight) / mIntervalHeight + mStartHour + position * mTimelineRange
        }else {
            mStartHour + position * mTimelineRange - ((mExtraHeight - insideY) / mIntervalHeight + 1)
        }
    }

    override fun getMinute(InsideY: Int): Int {
        return if (InsideY >= mExtraHeight) {
           ((InsideY - mExtraHeight) % mIntervalHeight / mIntervalHeight.toFloat() * 60).toInt()
        }else {
            ((mIntervalHeight - (mExtraHeight - InsideY) % mIntervalHeight) / mIntervalHeight.toFloat() * 60).toInt()
        }
    }

    override fun getMinuteTopHeight(minute: Int): Int {
        if (minute == 60) {
            return 0
        }
        //ceil为取大于等于它的最小整数，如：12.5则取13
        return ceil(mEveryMinuteHeight[minute]).toInt()
    }

    override fun getMinuteBottomHeight(minute: Int): Int {
        return if (minute < 59) {
            getMinuteTopHeight(minute + 1) - 1
        }else if (minute == 59){
            ceil(mEveryMinuteHeight[60]).toInt() - 1
        }else{
            0
        }
    }

    private fun getCorrectHeight(hour: Int, minute: Int, position: Int): Int {
        return getMinuteTopHeight(minute) + (hour - position * mTimelineRange - mStartHour) * mIntervalHeight + mExtraHeight
    }

    /**
     * 根据时间间隔数来返回正确的高度
     */
    override fun getCorrectTopHeight(insideY: Int, upperLimit: Int, position: Int): Int {
        val hour = getHour(insideY, position)
        val m = getMinute(insideY)
        val minute = m - m % mTimeInterval //间隔数为15，分钟数为16时，则取15
        return max(upperLimit, getCorrectHeight(hour, minute, position) + 1)
    }

    override fun getCorrectTopHeight(time: String): Int {
        val times = time.split(":")
        val h = times[0].toInt()
        val hour = if (h < mStartHour) h + 24 else h
        val minute = times[1].toInt()
        val height = mExtraHeight + (hour - mStartHour) * mIntervalHeight + mEveryMinuteHeight[minute].toInt() + 1
        return getCorrectTopHeight(height, 0, 0)
    }

    override fun getCorrectBottomHeight(insideY: Int, lowerLimit: Int, position: Int): Int {
        val hour = getHour(insideY, position)
        val m = getMinute(insideY)
        return min(if (m % mTimeInterval <= mTimeInterval / 3F) {
            val minute = m - m % mTimeInterval
            getCorrectHeight(hour, minute, position)
        } else {
            val minute = (m / mTimeInterval + 1) * mTimeInterval
            getCorrectHeight(hour, minute, position)
        }, lowerLimit)
    }

    override fun getCorrectBottomHeight(time: String): Int {
        val times = time.split(":")
        val h = times[0].toInt()
        val hour = if (h < mStartHour) h + 24 else h
        val minute = times[1].toInt()
        val height = mExtraHeight + (hour - mStartHour) * mIntervalHeight + mEveryMinuteHeight[minute].toInt() + 1
        return getCorrectBottomHeight(height, Int.MAX_VALUE, 0)
    }


    private fun getNowTime(): Float {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val second = calendar[Calendar.SECOND]
        return hour + minute / 60F + second / 3600F
    }

    private fun timeToString(hour: Int, minute: Int): String {
        val stH = if (hour < 10) {
            "0$hour"
        }else if (hour < 24) {
            hour.toString()
        }else {
            "0${hour%24}"
        }
        val stM: String = if (minute < 10) {
            "0$minute"
        }else {
            minute.toString()
        }
        return "$stH:$stM"
    }
}