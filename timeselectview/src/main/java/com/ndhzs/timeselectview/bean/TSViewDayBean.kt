package com.ndhzs.timeselectview.bean

import com.ndhzs.timeselectview.utils.TSViewAttrs
import java.text.SimpleDateFormat
import java.util.*

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/4/25
 *@description 用于存储每天的所有任务
 */
class TSViewDayBean {

    companion object {
        /**
         * 统一时间格式
         */
        val sdf = SimpleDateFormat("yyyy-M-d", Locale.CHINA)
    }

    var calendar = Calendar.getInstance()
        private set
    var tSViewTaskBeans: MutableList<TSViewTaskBean>
        private set

    /**
     * 请注意时间格式为 yyyy-M-d
     */
    constructor(date: String) {
        try {
            calendar.time = sdf.parse(date)!!
        }catch (e: Exception) {
            throw RuntimeException("${TSViewAttrs.Library_name}: " +
                    "Your time format is wrong!")
        }
        tSViewTaskBeans = LinkedList<TSViewTaskBean>()
    }

    constructor(date: Date) {
        calendar.time = date
        tSViewTaskBeans = LinkedList<TSViewTaskBean>()
    }

    constructor(calendar: Calendar) {
        this.calendar = calendar
        tSViewTaskBeans = LinkedList<TSViewTaskBean>()
    }

    constructor(date: String, taskBeans: MutableList<TSViewTaskBean>) {
        try {
            calendar.time = sdf.parse(date)!!
        }catch (e: Exception) {
            throw RuntimeException("${TSViewAttrs.Library_name}: " +
                    "Your time format is wrong!")
        }
        tSViewTaskBeans = taskBeans
    }

    fun taskSize(): Int {
        return tSViewTaskBeans.size
    }

    fun date(): String {
        return sdf.format(calendar.time)
    }
}