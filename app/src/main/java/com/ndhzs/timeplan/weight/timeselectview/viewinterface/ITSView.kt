package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.TimeSelectView
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
interface ITSView {
    fun addBackCardView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addTimeScrollView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun showNowTimeLine()
    fun notifyAllRectRedraw()
    fun setOnClickListener(onClick: ((bean: TSViewBean) -> Unit))
    fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit))
    fun setLinkedViewPager2(viewPager2: ViewPager2)
    fun getOuterMinWidth(): Int
    fun getOuterMinHeight(): Int
}