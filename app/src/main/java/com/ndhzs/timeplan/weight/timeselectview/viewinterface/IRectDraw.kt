package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Canvas
import android.graphics.Rect

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeplan.weight.timeselectview.utils.rect.RectDraw]
 */
interface IRectDraw {
    fun getMinHeight(): Float
    fun drawRect(canvas: Canvas, rect: Rect, name: String, borderColor: Int, insideColor: Int)
    fun drawRect(canvas: Canvas, rect: Rect, name: String, borderColor: Int, insideColor: Int, nameSize: Float)
    fun drawArrows(canvas: Canvas, rect: Rect, dTime: String)
    fun drawArrows(canvas: Canvas, rect: Rect, dTime: String, timeSize: Float)
    fun drawStartTime(canvas: Canvas, rect: Rect, sTime: String)
    fun drawStartEndTime(canvas: Canvas, rect: Rect, sTime: String, eTime: String)
    fun drawStartEndTime(canvas: Canvas, rect: Rect, sTime: String, eTime: String, timeSize: Float)
}