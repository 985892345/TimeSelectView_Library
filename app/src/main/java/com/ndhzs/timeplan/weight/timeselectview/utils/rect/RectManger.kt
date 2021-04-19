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
class RectManger(data: TSViewInternalData, time: ITSViewTime,
                 /**
                  * 第一个Int为开始点击的Rect的高度值，第二个Int为RectView的position值
                  * @param initialSideY 开始点击的Rect的高度值
                  * @param upperLimit 上限值
                  * @param lowerLimit 下限值
                  * @param position RectView的position值
                  */
                 clickEmptyCallBacks: (initialSideY: Int, upperLimit: Int, lowerLimit: Int, position: Int) -> Unit,
                 /**
                  * Int为开始点击的Rect的高度值
                  * @param deletedRect 被删掉的矩形
                  * @param deletedBean 被删掉的bean
                  * @param position RectView的position值
                  */
                 clickInsideCallbacks: (deletedRect: Rect, deletedBean: TSViewBean, position: Int) -> Unit,
                 /**
                  * 第一个Int为开始点击的Rect的高度值，第二个Int为上限值，第三个Int为下限值
                  * @param deletedRect 被删掉的矩形
                  * @param deletedBean 被删掉的bean
                  * @param initialSideY 开始点击的Rect的高度值
                  * @param upperLimit 上限值
                  * @param lowerLimit 下限值
                  */
                 clickTopBottomCallbacks: (deletedRect: Rect, deletedBean: TSViewBean,
                                           initialSideY: Int, upperLimit: Int, lowerLimit: Int) -> Unit) : IRectManger {

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
        private const val TOP_BOTTOM_WIDTH = 20 //长按响应顶部和底部的宽度
    }

    private val mData = data
    private val mTime = time
    private val mClickEmptyCallBacks = clickEmptyCallBacks
    private val mClickInsideCallBacks = clickInsideCallbacks
    private val mClickTopBottomCallbacks = clickTopBottomCallbacks
    private var mClickUpperLimit = mData.mRectViewTop
    private var mClickLowerLimit = mData.mRectViewBottom
    private val mAllRectWithBean = HashMap<Rect, TSViewBean>()
    private var mDeletedRect = Rect()
    private lateinit var mDeletedBean: TSViewBean

    private val mMyIRectViews = ArrayList<MyIRectView>()

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

        private val mPosition: Int

        init {
            mMyIRectViews.add(this)
            mPosition = mMyIRectViews.size - 1
        }


        override fun addNewRect(rect: Rect, bean: TSViewBean) {
            val newRect = Rect(rect.left,
                    rect.top + mPosition * mData.mTimelineRange * mData.mIntervalHeight,
                    rect.right,
                    rect.bottom + mPosition * mData.mTimelineRange * mData.mIntervalHeight)
            mAllRectWithBean[newRect] = bean
        }

        override fun addRectFromDeleted(rect: Rect) {
            val newRect = Rect(rect.left,
                    rect.top + mPosition * mData.mTimelineRange * mData.mIntervalHeight,
                    rect.right,
                    rect.bottom + mPosition * mData.mTimelineRange * mData.mIntervalHeight)
            mAllRectWithBean[newRect] = mDeletedBean
        }

        override fun getRectWithBeanMap(): HashMap<Rect, TSViewBean> {
            if (mData.mTSViewAmount == 1) {
                return mAllRectWithBean
            }
            val rectWithBean = HashMap<Rect, TSViewBean>()
            mAllRectWithBean.forEach {
                val rect = it.key
                val newRect = Rect(rect.left,
                        rect.top - mPosition * mData.mTimelineRange * mData.mIntervalHeight,
                        rect.right,
                        rect.bottom - mPosition * mData.mTimelineRange * mData.mIntervalHeight)
                rectWithBean[newRect] = it.value
            }
            return rectWithBean
        }

        fun getUpperLimit(insideY: Int): Int {
            val bottoms = ArrayList<Int>()
            getRectWithBeanMap().forEach {
                val bottom = it.key.bottom
                if (bottom <= insideY) {
                    bottoms.add(bottom + SeparatorLineView.HORIZONTAL_LINE_WIDTH)
                }
            }
            return if (bottoms.maxOrNull() == null) {
                mData.mRectViewTop
            }else {
                if (bottoms.maxOrNull()!! < mData.mRectViewTop) {
                    mData.mRectViewTop
                }else {
                    bottoms.maxOrNull()!!
                }
            }
        }

        fun getLowerLimit(insideY: Int): Int {
            val tops = ArrayList<Int>()
            getRectWithBeanMap().forEach {
                val top = it.key.top
                if (top >= insideY) {
                    tops.add(top - SeparatorLineView.HORIZONTAL_LINE_WIDTH)
                }
            }
            return if (tops.minOrNull() == null) {
                mData.mRectViewBottom
            }else {
                if (tops.minOrNull()!! > mData.mRectViewBottom) {
                    mData.mRectViewBottom
                }else {
                    tops.minOrNull()!!
                }
            }
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
                    mDeletedRect.set(rect)
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
                    if (insideY - rect.top < TOP_BOTTOM_WIDTH) {
                        mData.mCondition = TSViewLongClick.TOP
                        mClickTopBottomCallbacks.invoke(mDeletedRect, mDeletedBean, rect.bottom, mClickUpperLimit, mClickLowerLimit)
                    }else if (rect.bottom - insideY < TOP_BOTTOM_WIDTH) {
                        mData.mCondition = TSViewLongClick.BOTTOM
                        mClickTopBottomCallbacks.invoke(mDeletedRect, mDeletedBean, rect.top, mClickUpperLimit, mClickLowerLimit)
                    }else {
                        mData.mCondition = TSViewLongClick.INSIDE
                        mClickInsideCallBacks.invoke(mDeletedRect, mDeletedBean, mPosition)
                    }
                    return@deleteRect
                }
            }
            mData.mCondition = TSViewLongClick.EMPTY_AREA
            mClickEmptyCallBacks.invoke(insideY, mClickUpperLimit, mClickLowerLimit, mPosition)
        }
    }
}