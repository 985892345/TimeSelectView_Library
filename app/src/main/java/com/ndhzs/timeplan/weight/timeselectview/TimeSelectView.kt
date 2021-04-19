package com.ndhzs.timeplan.weight.timeselectview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.BackCardView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewObjectsManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSView

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/2
 * @description 顶层View，依次包含[BackCardView]、
 * [com.ndhzs.timeplan.weight.timeselectview.layout.TimeScrollView]
 */
class TimeSelectView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine() {
        mITSView.showNowTimeLine()
    }

    /**
     * 时间间隔数
     * @param timeInterval 必须为60的因数，若不是，将以15为间隔数
     */
    fun setTimeInterval(timeInterval: Int) {
        if (60 % timeInterval == 0) {
            mData.mTimeInterval = timeInterval
        }
    }

    /**
     * 最终的任务区域是否显示时间差
     */
    fun setIsShowDiffTime(boolean: Boolean) {
        if (mData.mIsShowDiffTime != boolean) {
            mData.mIsShowDiffTime = boolean
            mITSView.notifyAllRectRedraw()
        }
    }

    /**
     * 最终的任务区域是否显示上下边界时间
     */
    fun setIsShowTopBottomTime(boolean: Boolean) {
        if (mData.mIsShowStartEndTime != boolean) {
            mData.mIsShowStartEndTime = boolean
            mITSView.notifyAllRectRedraw()
        }
    }

    /**
     * 通知所有的任务刷新
     * 由于invalidate(Rect)已被官方废弃，官方推荐invalidate()刷新全部，所以就没有实现单独刷新某一个的方法
     */
    fun notifyAllTaskRefresh() {
        mITSView.notifyAllRectRedraw()
    }

    /**
     * 点击当前任务的监听，会返回当前点击任务的数据类
     * 注意：修改数据后并不会自己刷新，请手动调用notifyAllTaskRefresh()进行刷新
     */
    fun setOnTSVClickListener(onClick: (bean: TSViewBean) -> Unit) {
        mITSView.setOnClickListener(onClick)
    }

    /**
     * 设置长按监听接口
     */
    fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit)) {
        mITSView.setOnTSVLongClickListener(onStart, onEnd)
    }

    /**
     * 解决与ViewPager2的同向滑动冲突
     * @param viewPager2 传入ViewPager2，不是ViewPager
     */
    fun setLinkedViewPager2(viewPager2: ViewPager2) {
        mITSView.setLinkedViewPager2(viewPager2)
    }

    /**
     * 得到当前的TimeSelectView是否处于长按状态，
     * 若你想得到软件中所有的TimeSelectView是否存在处于长按状态的，可以使用TSViewLongClick.sIsLongClick
     */
    fun getIsLongClick(): Boolean = mData.mIsLongClick

    private val mData = TSViewInternalData(context, attrs)
    private val mUtil = TSViewObjectsManger(context, mData)
    private val mITSView: ITSView = mUtil.My1ITSView()

    init {
        val lp1 = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        lp1.gravity = Gravity.CENTER
        mITSView.addBackCardView(lp1, this)

        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp2.topMargin = BackCardView.topBottomMargin
        lp2.bottomMargin = BackCardView.topBottomMargin
        mITSView.addTimeScrollView(lp2, this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = mITSView.getOuterMinWidth()
        var newWidthMS = widthMeasureSpec
        var newHeightMS = heightMeasureSpec
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                newWidthMS = MeasureSpec.makeMeasureSpec(minWidth, MeasureSpec.EXACTLY)
            }
            MeasureSpec.EXACTLY -> {
                if (MeasureSpec.getSize(widthMeasureSpec) < minWidth) {
                    Log.e("123", "Your layout_width of TimeSelectView is too small to include timeline!!!!!")
                    Log.e("123", "Please enlarge the layout_width or shrink the timelineWidth of attrs.")
                }
                newWidthMS = widthMeasureSpec
            }
        }

        when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                newHeightMS = MeasureSpec.makeMeasureSpec(mITSView.getOuterMinHeight(), MeasureSpec.EXACTLY)
            }
            MeasureSpec.EXACTLY -> {}
        }

        super.onMeasure(newWidthMS, newHeightMS)
    }
}