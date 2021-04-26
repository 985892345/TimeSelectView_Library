package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.graphics.Rect
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewTaskBean

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/18
 * @description
 */
interface IRectViewRectManger {
    fun addNewRect(rect: Rect, taskBean: TSViewTaskBean)
    fun addRectFromDeleted(rect: Rect)
    fun getRectWithBeanMap(): Map<Rect, TSViewTaskBean>
    fun getDeletedRect(): Rect
    fun deleteRect(taskBean: TSViewTaskBean)
}