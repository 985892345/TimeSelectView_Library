package com.ndhzs.timeplan.weight.timeselectview.utils

import android.util.Log
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTimeUtil
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来进行时间与高度转换的类
 */
class TSViewTimeUtil(data: TSViewInternalData) : ITSViewTimeUtil {

    companion object {
        /**
         * 刷新当前时间高度的间隔时间
         */
        const val DELAY_NOW_TIME_REFRESH = 30000L

        /**
         * 回到当前时间高度的间隔时间
         */
        const val DELAY_BACK_CURRENT_TIME = 1000000L

        /**
         * 回到中心值的mCenterTime值
         */
        const val CENTER_TIME_CENTER = -1F

        /**
         * 回到当前时间的mCenterTime值
         */
        const val CENTER_TIME_NOW_TIME = -2F

        /**
         * String时间文字的小时与分钟的分隔符
         */
        private const val TIME_STRING_SPLIT_SYMBOL = ":"

        fun getDay(firstDate: String, diff: Int): String {
            val sdf = SimpleDateFormat("yyyy-M-d")
            val date = sdf.parse(firstDate)
            val calendar = Calendar.getInstance()
            calendar.time = date!!
            calendar.add(Calendar.DATE, diff)
            return sdf.format(calendar.time)
        }
    }

    private val mData = data
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

    /**
     * 返回时间对应的合适高度
     *
     * 注意：当你设置的时间范围不存在当前时间时，会返回时间轴中心线高度，这么设计的理由是：该方法只能用于
     * [com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView]的自动回到CurrentTime时调用
     */
    override fun getNowTimeHeight(): Int {
        return getTimeHeight(getNowTime())
    }

    /**
     * 返回当前时间与以当前position对应的时间轴的startTime的距离
     */
    override fun getNowTimeHeight(position: Int): Int {
        val time = getNowTime()
        return mExtraHeight + ((time - mData.mTimeRangeArray[position][0]) * mIntervalHeight).toInt()
    }

    /**
     * 返回时间对应的合适高度
     *
     * 注意：当你设置的时间范围不存在当前时间时，会返回时间轴中心线高度，这么设计的理由是：该方法只能用于
     * [com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView]的自动回到CurrentTime时调用
     */
    override fun getTimeHeight(time: Float): Int {
        var t = time
        var isIn = false
        for (i in 0 until mData.mTSViewAmount) {
            if (t in mData.mTimeRangeArray[i][0].toFloat()..mData.mTimeRangeArray[i][1].toFloat()) {
                t -= mData.mTimeRangeArray[i][0].toFloat()
                isIn = true
                break
            }
        }
        if (!isIn) { //当你设置的时间范围不存在当前时间时，自动以时间轴中心为中心线
            t = (mData.mTimeRangeArray[0][1] - mData.mTimeRangeArray[0][0])/2F
        }
        return mExtraHeight + ((t - mData.mTimeRangeArray[0][0]) * mIntervalHeight).toInt()
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

    /**
     * @return 注意：返回的hour是会大于24的值
     */
    override fun getHour(insideY: Int, position: Int): Int {
        return if (insideY >= mExtraHeight) {
            (insideY - mExtraHeight) / mIntervalHeight + mData.mTimeRangeArray[position][0]
        }else {
            mData.mTimeRangeArray[position][0] - ((mExtraHeight - insideY) / mIntervalHeight + 1)
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
            return getMinuteTopHeight(0)
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
        val h = if (hour < mData.mTimeRangeArray[position][0]) hour + 24 else hour
        return getMinuteTopHeight(minute) + (h - mData.mTimeRangeArray[position][0]) * mIntervalHeight + mExtraHeight
    }

    /**
     * 根据时间间隔数来返回正确的高度
     */
    override fun getCorrectTopHeight(insideTopY: Int, upperLimit: Int, position: Int, timeInterval: Int): Int {
        if (insideTopY == upperLimit) {
            return insideTopY
        }
        val h = getHour(insideTopY, position)
        val m = getMinute(insideTopY)
        return max(if (m % timeInterval >= timeInterval - timeInterval / 3F) {
            val minute = (m / timeInterval + 1) * timeInterval
            val hour = if (minute == 60) h + 1 else h
            getCorrectHeight(hour, minute, position) + 1
        }else {
            val minute = m - m % timeInterval //间隔数为15，分钟数为16时，则取15
            getCorrectHeight(h, minute, position) + 1
        }, upperLimit)

    }

    override fun getCorrectTopHeight(insideTopY: Int, upperLimit: Int, lowerLimit: Int, position: Int, dTime: String): IntArray {
        val correctTopHeight = getCorrectTopHeight(insideTopY, upperLimit, position, mData.mTimeInterval)
        val endTime = getEndTime(correctTopHeight, dTime, position)
        val correctBottomHeight = getCorrectBottomHeight(endTime, lowerLimit, position, 1)
        return intArrayOf(correctTopHeight, correctBottomHeight)
    }

    override fun getCorrectTopHeight(startTime: String, upperLimit: Int, position: Int, timeInterval: Int): Int {
        return if (60 % timeInterval != 0) {
            getCorrectTopHeight(startTime, upperLimit, position, mData.mTimeInterval)
        }else {
            val times = startTime.split(TIME_STRING_SPLIT_SYMBOL)
            val hour = times[0].toInt()
            val minute = times[1].toInt()
            val height = mExtraHeight + (hour - mData.mTimeRangeArray[position][0]) * mIntervalHeight + mEveryMinuteHeight[minute].toInt() + 1
            getCorrectTopHeight(height, upperLimit, position, timeInterval)
        }
    }

    override fun getCorrectBottomHeight(insideBottomY: Int, lowerLimit: Int, position: Int, timeInterval: Int): Int {
        if (insideBottomY == lowerLimit) {
            return insideBottomY
        }
        val h = getHour(insideBottomY, position)
        val m = getMinute(insideBottomY)
        return min(if (m % timeInterval <= timeInterval / 3F) {
            val minute = m - m % timeInterval
            getCorrectHeight(h, minute, position)
        }else {
            val minute = (m / timeInterval + 1) * timeInterval
            val hour = if (minute == 60) h + 1 else h
            getCorrectHeight(hour, minute, position)
        }, lowerLimit)
    }

    override fun getCorrectBottomHeight(insideBottomY: Int, upperLimit: Int, lowerLimit: Int, position: Int, dTime: String): IntArray {
        val correctBottomHeight = getCorrectBottomHeight(insideBottomY, lowerLimit, position, mData.mTimeInterval)
        val startTime = getStartTime(correctBottomHeight, dTime, position)
        val correctTopHeight = getCorrectTopHeight(startTime, upperLimit, position, 1)
        return intArrayOf(correctTopHeight, correctBottomHeight)
    }

    override fun getCorrectBottomHeight(endTime: String, lowerLimit: Int, position: Int, timeInterval: Int): Int {
        return if (60 % timeInterval != 0) {
            getCorrectBottomHeight(endTime, lowerLimit, position, mData.mTimeInterval)
        }else {
            val times = endTime.split(TIME_STRING_SPLIT_SYMBOL)
            val h = times[0].toInt()
            val hour = if (h < mData.mTimeRangeArray[position][0]) h + 24 else h
            val minute = times[1].toInt()
            val height = mExtraHeight + (hour - mData.mTimeRangeArray[position][0]) * mIntervalHeight + mEveryMinuteHeight[minute].toInt() + 1
            return getCorrectBottomHeight(height, lowerLimit, position, timeInterval)
        }
    }

    override fun getStartTime(insideBottomY: Int, dTime: String, position: Int): String {
        return getStartTime(getTime(insideBottomY, position), dTime, position)
    }

    override fun getStartTime(endTime: String, dTime: String, position: Int): String {
        val endTimes = endTime.split(TIME_STRING_SPLIT_SYMBOL)
        val endH = endTimes[0].toInt()
        val endHour = if (endH < mData.mTimeRangeArray[position][0]) endH + 24 else endH
        val endMinute = endTimes[1].toInt()
        val dTimes = dTime.split(TIME_STRING_SPLIT_SYMBOL)
        val dHour = dTimes[0].toInt()
        val dMinute = dTimes[1].toInt()
        var startHour = endHour - dHour
        var startMinute = endMinute - dMinute
        if (startMinute < 0) {
            startHour--
            startMinute += 60
        }
        if (startHour < 0) {
            startHour += 24
        }
        return timeToString(startHour, startMinute)
    }

    override fun getEndTime(insideTopY: Int, dTime: String, position: Int): String {
        return getEndTime(getTime(insideTopY, position), dTime)
    }

    override fun getEndTime(startTime: String, dTime: String): String {
        val startTimes = startTime.split(TIME_STRING_SPLIT_SYMBOL)
        val startHour = startTimes[0].toInt()
        val startMinute = startTimes[1].toInt()
        val dTimes = dTime.split(TIME_STRING_SPLIT_SYMBOL)
        val dHour = dTimes[0].toInt()
        val dMinute = dTimes[1].toInt()
        var endHour = startHour + dHour
        var endMinute = startMinute + dMinute
        if (endMinute >= 60) {
            endHour++
            endMinute -= 60
        }
        if (endHour > 24) {
            endHour -= 24
        }
        return timeToString(endHour, endMinute)
    }


    private fun getNowTime(): Float {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val second = calendar[Calendar.SECOND]
        return hour + minute / 60F + second / 3600F
    }

    private fun timeToString(hour: Int, minute: Int): String {
        val stH =if (hour < 0) {
            return timeToString(hour + 24, minute)
        }else if (hour < 10) {
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
        return "$stH$TIME_STRING_SPLIT_SYMBOL$stM"
    }
}