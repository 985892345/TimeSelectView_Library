package com.ndhzs.timeplan.weight.timeselectview.bean

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/4/25
 *@description
 */
class TSViewDayBean {

    var day: String

    constructor(day: String) {
        this.day = day
    }

    constructor(date: Date) {
        val sdf = SimpleDateFormat("yyyy-M-d")
        day = sdf.format(date)
    }

    fun size(): Int {
        return taskBeans.size
    }

    val taskBeans = ArrayList<TSViewTaskBean>()
}