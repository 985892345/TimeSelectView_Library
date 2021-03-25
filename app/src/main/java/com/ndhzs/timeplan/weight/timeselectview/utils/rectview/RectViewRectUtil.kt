package com.ndhzs.timeplan.weight.timeselectview.utils.rectview

import android.graphics.Rect
import android.util.Log
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewUtil

/**
 * @author 985892345
 * @date 2021/3/22
 * @description 用来专门管理与Rect相关的计算，比如求上下其他的Rect的边界
 */
class RectViewRectUtil(util: TSViewUtil) {

    companion object {
        private const val TOP_BOTTOM_WIDTH = 17 //长按响应顶部和底部的宽度
    }

    private val mUtil = util

    val mRectWithBean = HashMap<Rect, TSViewBean>()

    var mInitialSideY = 0
    var mUpperLimit = util.getTop()
        private set
    var mLowerLimit = util.getBottom()
        private set
    lateinit var mDeletedRect: Rect
        private set
    lateinit var mDeletedBean: TSViewBean
        private set

    fun deletedRect(rect: Rect) {
        val bean = mRectWithBean[rect]
        if (bean != null) {
            mDeletedRect = rect
            mDeletedBean = bean
            mRectWithBean.remove(rect)
        }else {
            Log.e("123", "this rect is not in mRectWithBean!")
        }
    }

    fun getUpperLimit(insideY: Int): Int {
        val bottoms = ArrayList<Int>()
        mRectWithBean.forEach {
            val bottom = it.key.bottom
            if (bottom <= insideY) {
                bottoms.add(bottom + SeparatorLineView.HORIZONTAL_LINE_WIDTH)
            }
        }
        return bottoms.maxOrNull() ?: mUtil.getTop()
    }

    fun getLowerLimit(insideY: Int): Int {
        val tops = ArrayList<Int>()
        mRectWithBean.forEach {
            val top = it.key.top
            if (top >= insideY) {
                tops.add(top - SeparatorLineView.HORIZONTAL_LINE_WIDTH)
            }
        }
        return tops.maxOrNull() ?: mUtil.getBottom()
    }

    fun longClickConditionJudge(insideX: Int, insideY: Int) {
        mUpperLimit = getUpperLimit(insideY)
        mLowerLimit = getLowerLimit(insideY)
        mRectWithBean.forEach {
            val rect = it.key
            if (insideX >= rect.left && insideX <= rect.right && insideY >= rect.top && insideY <= rect.bottom) {
                when {
                    insideY - rect.top < TOP_BOTTOM_WIDTH -> {
                        mInitialSideY = rect.bottom
                        mUtil.mCondition = TSViewLongClick.TOP
                    }
                    rect.bottom - insideY < TOP_BOTTOM_WIDTH -> {
                        mInitialSideY = rect.top
                        mUtil.mCondition = TSViewLongClick.BOTTOM
                    }
                    else -> {
                        mUtil.mCondition = TSViewLongClick.INSIDE
                    }
                }
            }else {
                mInitialSideY = insideY
                mUtil.mCondition = TSViewLongClick.EMPTY_AREA
            }
        }
    }

    fun isInRect(insideX: Int, insideY: Int): TSViewBean? {
        mRectWithBean.forEach {
            val rect = it.key
            if (insideX >= rect.left && insideX <= rect.right && insideY >= rect.top && insideY <= rect.bottom) {
                return mRectWithBean[rect]
            }
        }
        return null
    }
}