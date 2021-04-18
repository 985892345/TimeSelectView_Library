package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Rect
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/18
 * @description
 */
interface IRectView {
    fun addNewRect(rect: Rect, bean: TSViewBean)
    fun addRectFromDeleted(rect: Rect)
    fun getInitialSideY(): Int
    fun getDeletedRect(): Rect
    fun getDeletedBean(): TSViewBean
    fun getRectWithBeanMap(): HashMap<Rect, TSViewBean>
    fun getClickUpperLimit(): Int
    fun getClickLowerLimit(): Int
}