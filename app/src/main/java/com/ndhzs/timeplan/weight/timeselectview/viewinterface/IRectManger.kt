package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Rect
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
interface IRectManger {
    fun getInitialSideY(): Int
    fun getDeletedRect(): Rect
    fun getDeletedBean(): TSViewBean
    fun getRectWithBeanMap(): HashMap<Rect, TSViewBean>
    fun getUpperLimit(): Int
    fun getLowerLimit(): Int
    fun getUpperLimit(insideY: Int): Int
    fun getLowerLimit(insideY: Int): Int
    fun longClickConditionJudge(insideX: Int, insideY: Int)
    fun isInRect(insideX: Int, insideY: Int): TSViewBean?
}