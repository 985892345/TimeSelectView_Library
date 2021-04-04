package com.ndhzs.timeplan.weight.timeselectview.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectDraw
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectImgView
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
@SuppressLint("ViewConstructor")
class RectImgView(context: Context, iRectImgView: IRectImgView, data: TSViewInternalData, time: ITSViewTime, draw: IRectDraw) : View(context) {

    /**
     * 设置将要绘制的Rect和Bean，其中Rect会自动转换坐标系
     * (记得调用over()结束)
     */
    fun start(rect: Rect, bean: TSViewBean) {
        mRect.left = rect.left + mData.mRectViewToTSViewLeft
        mRect.top = rect.top - mIRectImgView.getScrollY()
        mRect.right = rect.right + mData.mRectViewToTSViewLeft
        mRect.bottom = rect.bottom - mIRectImgView.getScrollY()
        mBean = bean.copy()
    }

    fun over() {
    }

    fun getOuterTop(): Int = mRect.top

    fun getOuterBottom(): Int = mRect.bottom

    fun slideRectImgView(x: Int, y: Int) {

    }

    private val mData = data
    private val mDraw = draw
    private val mTime = time
    private val mIRectImgView = iRectImgView
    private val mRect = Rect()
    private lateinit var mBean: TSViewBean

    companion object {
        private const val X_KEEP_THRESHOLD = 30
    }

    override fun onDraw(canvas: Canvas) {
        if (!mRect.isEmpty) {
            mDraw.drawRect(canvas, mRect, mBean.name, mBean.borderColor, mBean.insideColor)
            mDraw.drawStartEndTime(canvas, mRect, mTime.getTime(mRect.top), mTime.getTime(mRect.bottom))
            if (mData.mIsShowDiffTime) {
                mDraw.drawArrows(canvas, mRect, mBean.diffTime)
            }
        }
    }
}