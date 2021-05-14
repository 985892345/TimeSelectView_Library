package com.ndhzs.timeselectview.bean

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
data class TSViewTaskBean(val day: String,
                          var name: String,
                          var startTime: String,
                          var endTime: String,
                          var diffTime: String,
                          var borderColor: Int,
                          var insideColor: Int)