package com.ndhzs.timeselectview.weight.timeselectview.utils.rect

import android.graphics.Rect
import com.ndhzs.timeselectview.weight.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.weight.timeselectview.layout.view.SeparatorLineView
import com.ndhzs.timeselectview.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeselectview.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.IRectManger
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.IRectViewRectManger
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.ITSViewTimeUtil

/**
 * @author 985892345
 * @date 2021/3/22
 * @description 用来专门管理与Rect相关的计算，比如求上下其他的Rect的边界
 */
class RectManger(data: TSViewInternalData, time: ITSViewTimeUtil,
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
                 clickInsideCallbacks: (deletedRect: Rect, deletedTaskBean: TSViewTaskBean, position: Int) -> Unit,
                 /**
                  * 第一个Int为开始点击的Rect的高度值，第二个Int为上限值，第三个Int为下限值
                  * @param deletedRect 被删掉的矩形
                  * @param deletedBean 被删掉的bean
                  * @param initialSideY 开始点击的Rect的高度值
                  * @param upperLimit 上限值
                  * @param lowerLimit 下限值
                  */
                 clickTopBottomCallbacks: (deletedRect: Rect, deletedTaskBean: TSViewTaskBean,
                                           initialSideY: Int, upperLimit: Int, lowerLimit: Int, position: Int) -> Unit) : IRectManger {

    /**
     * 初始化Bean
     */
    fun initializeBean(taskBeans: MutableList<TSViewTaskBean>) {
        mTaskBeans = taskBeans
        mAllRectWithBean.clear()
        taskBeans.forEach {
            val rect = Rect(0,
                    mTime.getCorrectTopHeight(it.startTime, 0, 0, 1),
                    mData.mRectViewWidth,
                    mTime.getCorrectBottomHeight(it.endTime, Int.MAX_VALUE, 0, 1))
            mAllRectWithBean[rect] = it
        }
    }

    /**
     * 用于数据在外面改变，然后刷新
     */
    fun refreshData() {
        initializeBean(mTaskBeans)
    }

    companion object {
        private const val TOP_BOTTOM_WIDTH = 27 //长按响应顶部和底部的宽度
    }

    private val mData = data
    private val mTime = time
    private val mClickEmptyCallBacks = clickEmptyCallBacks
    private val mClickInsideCallBacks = clickInsideCallbacks
    private val mClickTopBottomCallbacks = clickTopBottomCallbacks
    private var mClickUpperLimit = mData.mRectViewTop
    private var mClickLowerLimit = mData.mRectViewBottom
    private lateinit var mTaskBeans: MutableList<TSViewTaskBean>
    private val mAllRectWithBean = HashMap<Rect, TSViewTaskBean>()
    private var mDeletedRect = Rect()
    private lateinit var mDeletedTaskBean: TSViewTaskBean

    private val mMyIRectViews = ArrayList<MyIRectViewRectManger>()

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

    override fun getBean(insideY: Int, position: Int): TSViewTaskBean? {
        return mMyIRectViews[position].getBean(insideY)
    }

    override fun getDeletedBean(): TSViewTaskBean {
        return mDeletedTaskBean
    }

    override fun addBean(taskBean: TSViewTaskBean) {
        mTaskBeans.add(taskBean)
        mData.mDataChangeListener?.onDataAdd(taskBean)
    }


    override fun deleteBean(taskBean: TSViewTaskBean) {
        mTaskBeans.remove(taskBean)
        mData.mDataChangeListener?.onDataDelete(mDeletedTaskBean)
    }


    /**
     * 判断长按的情况，并求出此时上下边界、记录长按的起始点、在数组中删除Rect和Bean
     */
    override fun longClickConditionJudge(insideY: Int, position: Int) {
        mMyIRectViews[position].deleteRect(insideY)
    }

    inner class MyIRectViewRectManger : IRectViewRectManger {

        private val mPosition: Int

        init {
            mMyIRectViews.add(this)
            mPosition = mMyIRectViews.size - 1
        }


        override fun addNewRect(rect: Rect, taskBean: TSViewTaskBean) {
            val newRect = Rect(rect.left,
                    rect.top + (mData.mTimeRangeArray[mPosition][0] - mData.mTimeRangeArray[0][0]) * mData.mIntervalHeight,
                    rect.right,
                    rect.bottom + (mData.mTimeRangeArray[mPosition][0] - mData.mTimeRangeArray[0][0]) * mData.mIntervalHeight)
            mAllRectWithBean[newRect] = taskBean
            addBean(taskBean)
        }

        override fun addRectFromDeleted(rect: Rect) {
            val newRect = Rect(rect.left,
                    rect.top + (mData.mTimeRangeArray[mPosition][0] - mData.mTimeRangeArray[0][0]) * mData.mIntervalHeight,
                    rect.right,
                    rect.bottom +(mData.mTimeRangeArray[mPosition][0] - mData.mTimeRangeArray[0][0]) * mData.mIntervalHeight)
            mDeletedTaskBean.startTime = mTime.getTime(rect.top, mPosition)
            mDeletedTaskBean.endTime = mTime.getTime(rect.bottom, mPosition)
            mAllRectWithBean[newRect] = mDeletedTaskBean
        }

        override fun getRectWithBeanMap(): Map<Rect, TSViewTaskBean> {
            val rectWithBean = HashMap<Rect, TSViewTaskBean>()
            mAllRectWithBean.forEach {
                val rect = it.key
                val newRect = Rect(rect.left,
                        rect.top - (mData.mTimeRangeArray[mPosition][0] - mData.mTimeRangeArray[0][0]) * mData.mIntervalHeight,
                        rect.right,
                        rect.bottom - (mData.mTimeRangeArray[mPosition][0] - mData.mTimeRangeArray[0][0]) * mData.mIntervalHeight)
                rectWithBean[newRect] = it.value
            }
            return rectWithBean
        }

        override fun getDeletedRect(): Rect {
            return mDeletedRect
        }

        override fun deleteRect(taskBean: TSViewTaskBean) {
            this@RectManger.deleteBean(taskBean)
        }

        fun getUpperLimit(insideY: Int, rectWithTaskBean: Map<Rect, TSViewTaskBean> = getRectWithBeanMap()): Int {
            val bottoms = ArrayList<Int>()
            rectWithTaskBean.forEach {
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

        fun getLowerLimit(insideY: Int, rectWithTaskBean: Map<Rect, TSViewTaskBean> = getRectWithBeanMap()): Int {
            val tops = ArrayList<Int>()
            rectWithTaskBean.forEach {
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

        fun getBean(insideY: Int): TSViewTaskBean? {
            getRectWithBeanMap().forEach {
                val rect = it.key
                if (insideY in rect.top..rect.bottom) {
                    return it.value
                }
            }
            return null
        }

        fun deleteRect(insideY: Int) {
            val rectWithBean = getRectWithBeanMap()
            mClickUpperLimit = getUpperLimit(insideY, rectWithBean)
            mClickLowerLimit = getLowerLimit(insideY, rectWithBean)
            for (it in rectWithBean) {
                val rect = it.key
                if (insideY >= rect.top - TOP_BOTTOM_WIDTH/3 && insideY <= rect.bottom + TOP_BOTTOM_WIDTH/3) {
                    if (insideY < getUpperLimit(rect.top, rectWithBean)) {
                        //有两个十分相邻或就是相邻的矩形
                        //此时你点击的区域是下矩形的上方额外区域，但这里刚好属于上矩形内部，此时按用户的想法是控制上矩形，所以，跳过此次循环
                        continue
                    }
                    if (insideY > getLowerLimit(rect.bottom, rectWithBean)) {
                        //有两个十分相邻或就是相邻的矩形
                        //此时你点击的区域是上矩形的下方额外区域，但这里刚好属于下矩形内部，此时按用户的想法是控制下矩形，所以，跳过此次循环
                        continue
                    }
                    if (mClickLowerLimit + SeparatorLineView.HORIZONTAL_LINE_WIDTH == rect.top) { //此时你点击的区域在矩形上方，得到的下限值不对
                        mClickLowerLimit = getLowerLimit(rect.bottom, rectWithBean)
                    }
                    if (mClickUpperLimit - SeparatorLineView.HORIZONTAL_LINE_WIDTH == rect.bottom) { //此时你点击的区域在矩形下方，得到的上限值不对
                        mClickUpperLimit = getUpperLimit(rect.top, rectWithBean)
                    }
                    mDeletedRect.set(rect)
                    mDeletedTaskBean = it.value
                    //先在mAllRectWithBean中移去点击的矩形
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
                    if (insideY - (rect.top - TOP_BOTTOM_WIDTH/3) < TOP_BOTTOM_WIDTH) {
                        mData.mCondition = TSViewLongClick.TOP
                        mClickTopBottomCallbacks.invoke(mDeletedRect, mDeletedTaskBean,
                                rect.bottom, mClickUpperLimit, mClickLowerLimit, mPosition)
                    }else if ((rect.bottom + TOP_BOTTOM_WIDTH/3 - insideY) < TOP_BOTTOM_WIDTH) {
                        mData.mCondition = TSViewLongClick.BOTTOM
                        mClickTopBottomCallbacks.invoke(mDeletedRect, mDeletedTaskBean,
                                rect.top, mClickUpperLimit, mClickLowerLimit, mPosition)
                    }else {
                        mData.mCondition = TSViewLongClick.INSIDE
                        mClickInsideCallBacks.invoke(mDeletedRect, mDeletedTaskBean, mPosition)
                    }
                    return
                }
            }
            mData.mCondition = TSViewLongClick.EMPTY_AREA
            mClickEmptyCallBacks.invoke(insideY, mClickUpperLimit, mClickLowerLimit, mPosition)
        }
    }
}