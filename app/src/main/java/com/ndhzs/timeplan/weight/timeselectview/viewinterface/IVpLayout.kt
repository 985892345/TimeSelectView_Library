package com.ndhzs.timeplan.weight.timeselectview.viewinterface

import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewTaskBean

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeplan.weight.timeselectview.TimeSelectView]
 */
interface IVpLayout {
    fun addBackCardView(lp: ViewGroup.LayoutParams, v: ViewGroup)
    fun addTimeScrollView(lp: ViewGroup.LayoutParams, v: ViewGroup, viewPager2: ViewPager2?)
    fun showNowTimeLine()
    fun cancelShowNowTimeLine()
    fun notifyAllRectRefresh()
    fun initializeBean(taskBeans: MutableList<TSViewTaskBean>)
    fun backCurrentTime()
    fun moveTo(scrollY: Int)
    fun setOnScrollListener(l: ((scrollY: Int, vpPosition: Int) -> Unit))
}