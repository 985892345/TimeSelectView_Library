package com.ndhzs.timeplan.weight.timeselectview.utils

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来判断长按状态的类
 */
enum class TSViewLongClick {
    NULL, TOP, BOTTOM, INSIDE, EMPTY_AREA;
    companion object {
        /**
         * 这个可得到全部的TimeSelectView是否是处于长按状态
         */
        val sIsLongClick
            get() = sIsNotLongClickCount == 0

        /**
         * 这个可得到全部的TimeSelectView中不是处于长按状态的数量
         */
        var sIsNotLongClickCount = 0
    }
}