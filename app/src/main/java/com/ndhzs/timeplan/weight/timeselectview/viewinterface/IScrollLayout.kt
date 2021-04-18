package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Rect
import android.view.ViewGroup
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/6
 * @description [com.ndhzs.timeplan.weight.timeselectview.layout.ScrollLayout]
 */
interface IScrollLayout {
    fun addParentLayout(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addRectImgView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun getRectViewPosition(rowX: Int): Int?
    fun getPreRectViewPosition(): Int
    fun getRectViewRawLocation(position: Int): Rect
    fun getChildLayoutLocation(position: Int): Rect

    /**
     * @return 返回左右值为相对于屏幕坐标，上下值为内部坐标的Rect
     */
    fun getRectImgViewRawRect(): Rect
    fun entireMoveStart(rect: Rect, bean: TSViewBean, position: Int)
    fun slideRectImgView(dx: Int, dy: Int)
    fun slideEndRectImgView(rawFinalLeft: Int, insideFinalTop: Int, onEndListener: () -> Unit? = {})
    fun deleteRectImgView(onEndListener: () -> Unit? = {})
    fun setIsCanLongClick(boolean: Boolean)
    fun notifyRectViewRedraw()
    fun notifyRectViewAddRectFromDeleted(rect: Rect, position: Int)
}