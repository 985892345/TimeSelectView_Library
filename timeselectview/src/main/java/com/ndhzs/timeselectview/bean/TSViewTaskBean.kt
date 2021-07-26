package com.ndhzs.timeselectview.bean

import java.util.*

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用于存储当个任务
 */
data class TSViewTaskBean(val date: String,
                          var name: String,
                          var startTime: String,
                          var endTime: String,
                          var diffTime: String,
                          var borderColor: Int,
                          var insideColor: Int,
                          var any1: Any? = null,
                          var any2: Any? = null,
                          var any3: Any? = null) {
    constructor(
        calendar: Calendar,
        name: String,
        startTime: String,
        endTime: String,
        diffTime: String,
        borderColor: Int,
        insideColor: Int,
        any1: Any? = null,
        any2: Any? = null,
        any3: Any? = null
    ) : this(
        TSViewDayBean.sdf.format(calendar.time),
        name,
        startTime,
        endTime,
        diffTime,
        borderColor,
        insideColor,
        any1, any2, any3)
}