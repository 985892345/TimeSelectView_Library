package com.ndhzs.timeselectview.viewinterface

import com.ndhzs.timeselectview.layout.view.RectImgView

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/4/27
 *@description
 */
internal interface IStickerLayout {
    fun getRectImgView(): RectImgView
    fun getChildLayoutWidth(): Int
    fun getChildLayoutToStickerLayoutDistance(position: Int): Int
}