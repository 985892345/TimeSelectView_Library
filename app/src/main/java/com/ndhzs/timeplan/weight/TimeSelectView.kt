package com.ndhzs.timeplan.weight

import android.content.Context
import android.util.AttributeSet
import com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.LongPress
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewTimeUtil
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewUtil
import com.ndhzs.timeplan.weight.timeselectview.utils.touchevent.TSViewTouchEvent

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
class TimeSelectView(context: Context, attrs: AttributeSet? = null) : TSViewTouchEvent(context, attrs) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine() {
        mChildLayout.showNowTimeLine()
    }

    /**
     * 时间间隔数
     * @param timeInterval 必须为60的因数，若不是，将以15为间隔数
     */
    fun setTimeInterval(timeInterval: Int) {
        if (60 % timeInterval == 0) {
            mUtil.mTimeUtil.mTimeInterval = timeInterval
        }
    }

    /**
     * 最终的任务区域是否显示时间差
     */
    fun setIsShowDiffTime(boolean: Boolean) {
        if (mUtil.mIsShowDiffTime != boolean) {
            mUtil.mIsShowDiffTime = boolean
            mChildLayout.notifyRectViewRedraw()
        }
    }

    /**
     * 最终的任务区域是否显示上下边界时间
     */
    fun setIsShowTopBottomTime(boolean: Boolean) {
        if (mUtil.mIsShowStartEndTime != boolean) {
            mUtil.mIsShowStartEndTime = boolean
            mChildLayout.notifyRectViewRedraw()
        }
    }

    /**
     * 设置两个TimeSelectView的相互联合
     */
    fun setLinkedTSView(linkedTimeSelectView: TimeSelectView) {
        mLinkedTsView = linkedTimeSelectView
    }

    /**
     * 设置长按监听接口
     */
    fun setOnLongPressListener(l: OnLongPressListener) {
        mLongPressListener = l
    }

    private val mUtil = TSViewUtil(context, attrs)
    private val mTimeUtil = mUtil.mTimeUtil
    private val mLongPress = mUtil.mLongPress
    private val mChildLayout = ChildLayout(context, mUtil)
    private var mLinkedTsView: TimeSelectView? = null
    private var mLongPressListener: OnLongPressListener? = null

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(mChildLayout, lp)
        moveToCenterTime()
    }

    private fun moveToCenterTime() {
        if (mUtil.mCenterTime == -1F) { //以当前时间线为中线
            post(object : Runnable {
                override fun run() {
                    scrollY = mTimeUtil.getTimeHeight() - width / 2
                    postDelayed(this, TSViewTimeUtil.DELAY_BACK_CURRENT_TIME)
                }
            })
        }else { //以mCenterTime为中线，不随时间移动
            post {
                scrollY = mTimeUtil.getTimeHeight(mUtil.mCenterTime) - width/2
            }
        }
    }

    private val mBackCurrentTimeRun = Runnable {
        scrollY = if (mUtil.mCenterTime == -1F) {
            mTimeUtil.getTimeHeight() - width/2
        }else {
            mTimeUtil.getTimeHeight(mUtil.mCenterTime) - width/2
        }
    }

    override fun dispatchTouchEventDown() {
        removeCallbacks(mBackCurrentTimeRun)
        mLinkedTsView?.let {
            it.removeCallbacks(it.mBackCurrentTimeRun)
        }
    }

    override fun dispatchTouchEventUp() {
        mLongPress.condition = LongPress.NULL
        postDelayed(mBackCurrentTimeRun, TSViewTimeUtil.DELAY_BACK_CURRENT_TIME)
    }

    override fun onInterceptTouchEventDown(x: Int, y: Int): Boolean {
        //点击的是左部区域，直接拦截
        if (x < mUtil.mIntervalLeft + 3) {
            return true
        }
        //对ScrollView的外部大小的上下mExtraHeight距离进行拦截
        if (y < mUtil.mExtraHeight || y > height - mUtil.mExtraHeight) {
            return true
        }
        return false
    }

    override fun onLongPressStart() {
        mLongPressListener?.onLongPressStart()
    }

    override fun onLongPressEnd() {
        mLongPressListener?.onLongPressEnd()
    }

    interface OnLongPressListener {
        fun onLongPressStart()
        fun onLongPressEnd()
    }
}