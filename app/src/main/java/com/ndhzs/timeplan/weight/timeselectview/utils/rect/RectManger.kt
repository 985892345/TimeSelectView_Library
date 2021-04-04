package com.ndhzs.timeplan.weight.timeselectview.utils.rect

import android.graphics.Rect
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectManger

/**
 * @author 985892345
 * @date 2021/3/22
 * @description 用来专门管理与Rect相关的计算，比如求上下其他的Rect的边界
 */
class RectManger(data: TSViewInternalData) : IRectManger {

    companion object {
        private const val TOP_BOTTOM_WIDTH = 17 //长按响应顶部和底部的宽度
    }

    private val mData = data

    private val mRectWithBean = HashMap<Rect, TSViewBean>()
    private var mInitialSideY = 0
    private var mUpperLimit = data.mRectViewTop
    private var mLowerLimit = data.mRectViewBottom
    private lateinit var mDeletedRect: Rect
    private lateinit var mDeletedBean: TSViewBean

    override fun getInitialSideY(): Int = mInitialSideY

    override fun getDeletedRect(): Rect = mDeletedRect

    override fun getDeletedBean(): TSViewBean = mDeletedBean

    override fun getRectWithBeanMap(): HashMap<Rect, TSViewBean> = mRectWithBean

    override fun getUpperLimit(): Int = mUpperLimit

    override fun getUpperLimit(insideY: Int): Int {
        val bottoms = ArrayList<Int>()
        mRectWithBean.forEach {
            val bottom = it.key.bottom
            if (bottom <= insideY) {
                bottoms.add(bottom + SeparatorLineView.HORIZONTAL_LINE_WIDTH)
            }
        }
        return bottoms.maxOrNull() ?: mData.mRectViewTop
    }

    override fun getLowerLimit(): Int = mLowerLimit

    override fun getLowerLimit(insideY: Int): Int {
        val tops = ArrayList<Int>()
        mRectWithBean.forEach {
            val top = it.key.top
            if (top >= insideY) {
                tops.add(top - SeparatorLineView.HORIZONTAL_LINE_WIDTH)
            }
        }
        return tops.maxOrNull() ?: mData.mRectViewBottom
    }

    /**
     * 判断长按的情况，并求出此时上下边界、记录长按的起始点、在数组中删除Rect和Bean
     */
    override fun longClickConditionJudge(insideX: Int, insideY: Int) {
        mUpperLimit = getUpperLimit(insideY)
        mLowerLimit = getLowerLimit(insideY)
        mRectWithBean.forEach {
            val rect = it.key
            if (insideX >= rect.left && insideX <= rect.right && insideY >= rect.top && insideY <= rect.bottom) {
                when {
                    insideY - rect.top < TOP_BOTTOM_WIDTH -> {
                        mInitialSideY = rect.bottom
                        mData.mCondition = TSViewLongClick.TOP
                    }
                    rect.bottom - insideY < TOP_BOTTOM_WIDTH -> {
                        mInitialSideY = rect.top
                        mData.mCondition = TSViewLongClick.BOTTOM
                    }
                    else -> {
                        mData.mCondition = TSViewLongClick.INSIDE
                    }
                }
                mDeletedRect = rect
                mDeletedBean = it.value
                mRectWithBean.remove(rect)
                return@longClickConditionJudge
            }
        }
        mInitialSideY = insideY
        mData.mCondition = TSViewLongClick.EMPTY_AREA
    }

    override fun isInRect(insideX: Int, insideY: Int): TSViewBean? {
        mRectWithBean.forEach {
            val rect = it.key
            if (insideX >= rect.left && insideX <= rect.right && insideY >= rect.top && insideY <= rect.bottom) {
                return mRectWithBean[rect]
            }
        }
        return null
    }
}