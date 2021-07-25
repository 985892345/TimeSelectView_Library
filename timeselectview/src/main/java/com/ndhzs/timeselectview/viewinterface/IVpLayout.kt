package com.ndhzs.timeselectview.viewinterface

import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.layout.BackCardView
import com.ndhzs.timeselectview.layout.TimeScrollView

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeselectview.TimeSelectView]
 */
internal interface IVpLayout {
    fun getBackCardView(): BackCardView
    fun getTimeScrollView(viewPager2: ViewPager2): TimeScrollView
    fun showNowTimeLine()
    fun onViewDetachedFromWindow()
    fun onViewRecycled()
    fun notifyAllRectRefresh()
    fun initializeBean(taskBeans: MutableList<TSViewTaskBean>)
    fun backCurrentTime()
    fun cancelAutoBackCurrentTime()
    fun timeLineScrollTo(scrollY: Int)
    fun timeLineSlowlyScrollTo(scrollY: Int)
    fun notifyRectViewDataChanged()
}