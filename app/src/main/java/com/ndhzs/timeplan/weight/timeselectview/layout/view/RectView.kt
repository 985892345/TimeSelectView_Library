package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewUtil

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
@SuppressLint("ViewConstructor")
class RectView(context: Context, util: TSViewUtil) : View(context) {

    private val mUtil = util
    private val mTimeUtil = util.mTimeUtil
    private val mDrawUtil = util.mDrawUtil
    private val mRectUtil = util.mRectUtil
    private val mLongPress = util.mLongPress
    private val mInitialRect = Rect()
    private val mRectWithBean = mRectUtil.mRectWithBean

    override fun onDraw(canvas: Canvas) {
        if (!mInitialRect.isEmpty) {
            mDrawUtil.drawRect(canvas, mInitialRect, mRectUtil.mDeletedBean)
            mDrawUtil.drawArrows(canvas, mInitialRect, mTimeUtil.getDTime(mInitialRect.top, mInitialRect.bottom))
            mDrawUtil.drawStartEndTime(canvas, mInitialRect, mTimeUtil.getTime(mInitialRect.top), mTimeUtil.getTime(mInitialRect.bottom))
        }
        mRectWithBean.forEach{
            mDrawUtil.drawRect(canvas, it.key, it.value)
            if (mUtil.mIsShowDiffTime) {
                mDrawUtil.drawArrows(canvas, it.key, it.value.diffTime)
            }
            if (mUtil.mIsShowStartEndTime) {
                mDrawUtil.drawStartEndTime(canvas, it.key, it.value.startTime, it.value.endTime)
            }
        }
    }
}