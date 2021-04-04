package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Canvas
import android.graphics.Rect

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
interface IRectDraw {
    fun getMinHeight(): Float

    fun drawRect(canvas: Canvas, rect: Rect, name: String, borderColor: Int, insideColor: Int)
    fun drawArrows(canvas: Canvas, rect: Rect, dTime: String)
    fun drawStartTime(canvas: Canvas, rect: Rect, sTime: String)
    fun drawStartEndTime(canvas: Canvas, rect: Rect, sTime: String, eTime: String)
}