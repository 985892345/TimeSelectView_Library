package com.ndhzs.timeplan.weight.timeselectview.utils.rect

import android.graphics.Rect
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime

/**
 * @author 985892345
 * @date 2021/3/22
 * @description 用来专门管理与Rect相关的计算，比如求上下其他的Rect的边界
 */
class RectManger(data: TSViewInternalData, time: ITSViewTime) : IRectManger {

    /**
     * 初始化Bean
     */
    fun initializeBean(beans: List<TSViewBean>) {
        beans.forEach {
            val rect = Rect(0,
                    mTime.getCorrectTopHeight(it.startTime),
                    mData.mRectViewWidth,
                    mTime.getCorrectBottomHeight(it.endTime))
            mAllRectWithBean[rect] = it
        }
    }

    companion object {
        private const val TOP_BOTTOM_WIDTH = 17 //长按响应顶部和底部的宽度
    }

    private val mData = data
    private val mTime = time
    private var mClickUpperLimit = mData.mRectViewTop
    private var mClickLowerLimit = mData.mRectViewBottom
    private val mAllRectWithBean = HashMap<Rect, TSViewBean>()
    private lateinit var mDeletedRect: Rect
    private lateinit var mDeletedBean: TSViewBean

    private val mMyIRectViews = ArrayList<MyIRectView>()

    override fun getDeletedRect(): Rect {
        return mDeletedRect
    }

    override fun getDeletedBean(): TSViewBean {
        return mDeletedBean
    }

    override fun getClickUpperLimit(): Int {
        return mClickUpperLimit
    }

    override fun getClickLowerLimit(): Int {
        return mClickLowerLimit
    }

    override fun getUpperLimit(insideY: Int, position: Int): Int {
        return mMyIRectViews[position].getUpperLimit(insideY)
    }

    override fun getLowerLimit(insideY: Int, position: Int): Int {
       return mMyIRectViews[position].getLowerLimit(insideY)
    }

    override fun getBean(insideY: Int, position: Int): TSViewBean? {
        return mMyIRectViews[position].getBean(insideY)
    }

    /**
     * 判断长按的情况，并求出此时上下边界、记录长按的起始点、在数组中删除Rect和Bean
     */
    override fun longClickConditionJudge(insideY: Int, position: Int) {
        mClickUpperLimit = getUpperLimit(insideY, position)
        mClickLowerLimit = getLowerLimit(insideY, position)
        mMyIRectViews[position].deleteRect(insideY)
    }

    inner class MyIRectView : IRectView {

        private val position: Int

        init {
            mMyIRectViews.add(this)
            position = mMyIRectViews.size - 1
        }

        private var mInitialSideY= 0

        override fun addNewRect(rect: Rect, bean: TSViewBean) {
            val newRect = Rect(rect.left,
                    rect.top + position * mData.mTimelineRange * mData.mIntervalHeight,
                    rect.right,
                    rect.bottom + position * mData.mTimelineRange * mData.mIntervalHeight)
            mAllRectWithBean[newRect] = bean
        }

        override fun addRectFromDeleted(rect: Rect) {
            val newRect = Rect(rect.left,
                    rect.top + position * mData.mTimelineRange * mData.mIntervalHeight,
                    rect.right,
                    rect.bottom + position * mData.mTimelineRange * mData.mIntervalHeight)
            mAllRectWithBean[newRect] = mDeletedBean
        }

        override fun getInitialSideY(): Int {
            return mInitialSideY
        }

        override fun getDeletedRect(): Rect {
            return mDeletedRect
        }

        override fun getDeletedBean(): TSViewBean {
            return mDeletedBean
        }

        override fun getRectWithBeanMap(): HashMap<Rect, TSViewBean> {
            if (mData.mTSViewAmount == 1) {
                return mAllRectWithBean
            }
            val rectWithBean = HashMap<Rect, TSViewBean>()
            mAllRectWithBean.forEach {
                val rect = it.key
                val newRect = Rect(rect.left,
                        rect.top - position * mData.mTimelineRange * mData.mIntervalHeight,
                        rect.right,
                        rect.bottom - position * mData.mTimelineRange * mData.mIntervalHeight)
                rectWithBean[newRect] = it.value
            }
            return rectWithBean
        }

        override fun getClickUpperLimit(): Int {
            return mClickUpperLimit
        }

        override fun getClickLowerLimit(): Int {
            return mClickLowerLimit
        }

        fun getUpperLimit(insideY: Int): Int {
            val bottoms = ArrayList<Int>()
            getRectWithBeanMap().forEach {
                val bottom = it.key.bottom
                if (bottom <= insideY) {
                    bottoms.add(bottom + SeparatorLineView.HORIZONTAL_LINE_WIDTH)
                }
            }
            return bottoms.maxOrNull() ?: mData.mRectViewTop
        }

        fun getLowerLimit(insideY: Int): Int {
            val tops = ArrayList<Int>()
            getRectWithBeanMap().forEach {
                val top = it.key.top
                if (top >= insideY) {
                    tops.add(top - SeparatorLineView.HORIZONTAL_LINE_WIDTH)
                }
            }
            return tops.maxOrNull() ?: mData.mRectViewBottom
        }

        fun getBean(insideY: Int): TSViewBean? {
            getRectWithBeanMap().forEach {
                val rect = it.key
                if (insideY in rect.top..rect.bottom) {
                    return it.value
                }
            }
            return null
        }

        fun deleteRect(insideY: Int) {
            getRectWithBeanMap().forEach {
                val rect = it.key
                if (insideY >= rect.top && insideY <= rect.bottom) {
                    if (insideY - rect.top < TOP_BOTTOM_WIDTH) {
                        mInitialSideY = rect.bottom
                        mData.mCondition = TSViewLongClick.TOP
                    }else if (rect.bottom - insideY < TOP_BOTTOM_WIDTH) {
                        mInitialSideY = rect.top
                        mData.mCondition = TSViewLongClick.BOTTOM
                    }else {
                        mData.mCondition = TSViewLongClick.INSIDE
                    }
                    mDeletedRect = rect
                    mDeletedBean = it.value
                    if (mData.mTSViewAmount == 1) {
                        mAllRectWithBean.remove(rect)
                    }else {
                        for (i in mAllRectWithBean) {
                            if (i.value == it.value) {
                                mAllRectWithBean.remove(i.key)
                                break
                            }
                        }
                    }
                    return@deleteRect
                }
            }
            mInitialSideY = insideY
            mData.mCondition = TSViewLongClick.EMPTY_AREA
        }
    }
}