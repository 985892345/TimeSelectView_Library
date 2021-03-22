package com.ndhzs.timeplan.weight.timeselectview.utils

import android.graphics.Rect
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView

/**
 * @author 985892345
 * @date 2021/3/22
 * @description 用来专门管理与Rect相关的计算，比如求上下其他的Rect的边界
 */
class RectViewRectUtil(util: TSViewUtil) {

    private val mUtil = util
    private val mLongPress = util.mLongPress
    val mRectWithBean = HashMap<Rect, TSViewBean>()
    val mDeletedRect = Rect()
    val mDeletedBean: TSViewBean? = TSViewBean("", "", "", "", 0, 0)
        get() {
            return if (mLongPress.condition == LongPress.EMPTY_AREA) {
                null
            }else {
                field
            }
        }

    fun deletedRect(rect: Rect) {
        mDeletedRect.set(rect)
        val bean = mRectWithBean[rect]
        mDeletedBean?.let { i ->
            bean?.let { j ->
                i.name = j.name
                i.startTime = j.startTime
                i.endTime = j.endTime
                i.diffTime = j.diffTime
                i.borderColor = j.borderColor
                i.insideColor = j.insideColor
            }
        }
        mRectWithBean.remove(rect)
    }

    fun getUpperLimit(y: Int): Int {
        val bottoms = ArrayList<Int>()
        mRectWithBean.forEach {
            val bottom = it.key.bottom
            if (bottom < y) {
                bottoms.add(bottom + SeparatorLineView.HORIZONTAL_LINE_WIDTH)
            }
        }
        return bottoms.maxOrNull() ?: mUtil.getTop()
    }

    fun getLowerLimit(y: Int): Int {
        val tops = ArrayList<Int>()
        mRectWithBean.forEach {
            val top = it.key.top
            if (top > y) {
                tops.add(top - SeparatorLineView.HORIZONTAL_LINE_WIDTH)
            }
        }
        return tops.maxOrNull() ?: mUtil.getBottom()
    }
}