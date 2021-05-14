package com.ndhzs.timeselectview.bean

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/4/25
 *@description 用于存储每天的所有任务
 */
class TSViewDayBean {

    val date: String
    val taskBeans = ArrayList<TSViewTaskBean>()

    constructor(day: String) {
        this.date = day
    }

    constructor(date: Date) {
        val sdf = SimpleDateFormat("yyyy-M-d")
        this.date = sdf.format(date)
    }

    fun size(): Int {
        return taskBeans.size
    }
}