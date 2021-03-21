package com.ndhzs.timeplan.weight.timeselectview.utils

/**
 * @author 985892345
 * @date 2021/3/20
 * @description 用来判断长按状态的类
 */
class LongPress {
    companion object {
        /**
         * NULL表示不是长按状态
         */
        const val NULL = -1

        /**
         * EMPTY_AREA表示长按的空区域
         */
        const val EMPTY_AREA = 0

        /**
         * TOP表示长按的任务顶部
         */
        const val TOP = 1

        /**
         * INSIDE表示长按的任务内部
         */
        const val INSIDE = 2

        /**
         * BOTTOM表示长按的任务底部
         */
        const val BOTTOM = 3
    }
    var condition = NULL

}