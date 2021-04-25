package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewDayBean
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewObjectsManger
import java.util.*

@SuppressLint("ViewConstructor")
class VpLayout(context: Context, data: TSViewInternalData, viewPager2: ViewPager2) : FrameLayout(context) {

    /**
     * 初始化
     */
    fun initialize(dayBeans: TSViewDayBean, vpPosition: Int, firstDate: String) {
        mIVpLayout.initializeBean(dayBeans.taskBeans)
        mObjectManger.mVpPosition = vpPosition
        mObjectManger.mFirstDate = firstDate
    }

    /**
     * 设置滑动监听接口
     */
    fun setOnScrollListener(l: (scrollY: Int, vpPosition: Int) -> Unit) {
        mIVpLayout.setOnScrollListener(l)
    }

    /**
     * 显示当前时间线
     */
    fun showNowTimeLine() {
        mIVpLayout.showNowTimeLine()
    }

    /**
     * 取消显示时间线
     */
    fun cancelShowNowTimeLine() {
        mIVpLayout.cancelShowNowTimeLine()
    }

    /**
     * 快速回到设置的当前时间
     */
    fun backCurrentTime() {
        mIVpLayout.backCurrentTime()
    }

    /**
     * 就是一个调用内部ScrollView的setScrollY方法
     */
    fun moveTo(scrollY: Int) {
        mIVpLayout.moveTo(scrollY)
    }

    private val mObjectManger = TSViewObjectsManger(context, data)
    private val mIVpLayout = mObjectManger.My1IVpLayout()

    init {
        val lp1 = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        lp1.gravity = Gravity.CENTER
        mIVpLayout.addBackCardView(lp1, this)

        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp2.topMargin = BackCardView.TOP_BOTTOM_MARGIN
        lp2.bottomMargin = BackCardView.TOP_BOTTOM_MARGIN
        mIVpLayout.addTimeScrollView(lp2, this, viewPager2)
    }
}