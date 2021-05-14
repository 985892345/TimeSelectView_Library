package com.ndhzs.timeselectview.viewinterface

import android.graphics.Rect
import android.view.ViewGroup

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 * @description [com.ndhzs.timeselectview.layout.ScrollLayout]
 */
internal interface IScrollLayout {
    fun addParentLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addStickerLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun getRectViewPosition(inWindowX: Int): Int?
    fun getPreRectViewPosition(): Int
    fun getRectViewInWindowLeftRight(position: Int): IntArray
    fun getChildLayoutInWindowLeftRight(position: Int): IntArray
    fun getUnconstrainedDistance(): Int

    /**
     * @return 返回左右值为相对于屏幕坐标，上下值为内部坐标的Rect
     */
    fun getRectImgViewInWindowRect(): Rect
    fun getRectImgViewInitialRect(): Rect
    fun getStartEndDTime(top: Int, bottom: Int, position: Int): Array<String>
    fun slideRectImgView(dx: Int, dy: Int)
    fun slideEndRectImgView(inWindowFinalLeft: Int, insideFinalTop: Int, onEndListener: () -> Unit? = {})
    fun deleteRectImgView(onEndListener: () -> Unit? = {})
    fun setIsCanLongClick(boolean: Boolean)
    fun notifyRectViewRedraw()
    fun notifyRectViewAddRectFromDeleted(rect: Rect, position: Int)
    fun notifyTimeScrollViewScrollToSuitableHeight()
    fun notifyTimeScrollViewScrollToInitialHeight(height: Int)
}