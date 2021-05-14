package com.ndhzs.timeselectview.viewinterface

import android.view.ViewGroup

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/4/27
 *@description
 */
internal interface IStickerLayout {
    fun addRectImgView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun getChildLayoutWidth(): Int
    fun getChildLayoutToStickerLayoutDistance(position: Int): Int
}