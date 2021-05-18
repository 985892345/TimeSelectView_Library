package com.ndhzs.timeselectview.utils

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来判断长按状态的枚举类
 */
enum class TSViewLongClick {
    /**
     * 不处于长按状态
     */
    NULL,


    /**
     * 长按的任务顶部
     */
    TOP,
    /**
     * 长按的任务顶部且处于向上滑的状态
     */
    TOP_SLIDE_UP,
    /**
     * 长按的任务顶部且处于向下滑的状态
     */
    TOP_SLIDE_DOWN,


    /**
     * 长按的任务底部
     */
    BOTTOM,
    /**
     * 长按的任务底部且处于向上滑的状态
     */
    BOTTOM_SLIDE_UP,
    /**
     * 长按的任务底部且处于向下滑的状态
     */
    BOTTOM_SLIDE_DOWN,


    /**
     * 长按的任务内部
     */
    INSIDE,
    /**
     * 长按的任务内部且处于向上滑的状态
     */
    INSIDE_SLIDE_UP,
    /**
     * 长按的任务内部且处于向下滑的状态
     */
    INSIDE_SLIDE_DOWN,


    /**
     * 长按的空白区域
     */
    EMPTY_AREA,
    /**
     * 长按的空白区域且处于向上滑的状态
     */
    EMPTY_SLIDE_UP,
    /**
     * 长按的空白区域且处于向下滑的状态
     */
    EMPTY_SLIDE_DOWN;


    companion object {
        /**
         * 这个可得到软件中全部的TimeSelectView是否存在正处于长按状态的
         */
        @JvmStatic
        val sHasLongClick
            get() = sIsLongClickCount > 0

        /**
         * 这个可得到全部的TimeSelectView中处于长按状态的数量
         */
        internal var sIsLongClickCount = 0

        @JvmStatic
        internal fun getIsLongClickCount(): Int {
            return sIsLongClickCount
        }
    }
}

