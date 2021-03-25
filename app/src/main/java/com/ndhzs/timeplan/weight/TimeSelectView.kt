package com.ndhzs.timeplan.weight

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewTimeUtil
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewUtil
import com.ndhzs.timeplan.weight.timeselectview.utils.tsview.TSViewTouchEvent

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
     * 点击当前任务的监听
     */
    fun setOnClickListener(l: ((v: TimeSelectView, bean: TSViewBean) -> Unit)) {
        mOnClickListener = l
    }

    /**
     * 设置长按监听接口
     */
    fun setOnLongPressListener(start: ((condition: TSViewLongClick) -> Unit), end: ((condition: TSViewLongClick) -> Unit)) {
        mOnLongClickStartListener = start
        mOnLongClickEndListener = end
    }

    /**
     * 解决与ViewPager2的同向滑动冲突
     * @param viewPager2 传入ViewPager2，不是ViewPager
     */
    fun setLinkedViewPager2(viewPager2: ViewPager2) {
        mLinkedViewPager2 = viewPager2
    }

    /**
     * 得到当前的和与之联合的TimeSelectView是否是处于长按状态，
     * 若你想得到所有的TimeSelectView是否是处于长按状态，可以使用TSViewLongClick.sIsLongClick
     */
    fun getIsLongClick(): Boolean = mIsLongClick

    private val mUtil = TSViewUtil(context, attrs)
    private val mTimeUtil = mUtil.mTimeUtil
    private val mRectUtil = mUtil.mRectUtil
    private val mChildLayout = ChildLayout(context, mUtil)
    private var mIsLongClick = false
    private var mLinkedTsView: TimeSelectView? = null
    private var mLinkedViewPager2: ViewPager2? = null
    private var mOnClickListener: ((v: TimeSelectView, bean: TSViewBean) -> Unit)? = null
    private var mOnLongClickStartListener: ((condition: TSViewLongClick) -> Unit)? = null
    private var mOnLongClickEndListener: ((condition: TSViewLongClick) -> Unit)? = null

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(mChildLayout, lp)
        moveToCenterTime()
        mUtil.setOnConditionEndListener {
            onLongPressEnd(it)
        }
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
                scrollY = mTimeUtil.getTimeHeight(mUtil.mCenterTime) - width / 2
            }
        }
    }

    private val mBackCurrentTimeRun = Runnable {
        scrollY = if (mUtil.mCenterTime == -1F) {
            mTimeUtil.getTimeHeight() - width / 2
        }else {
            mTimeUtil.getTimeHeight(mUtil.mCenterTime) - width / 2
        }
    }

    override fun dispatchTouchEventDown() {
        removeCallbacks(mBackCurrentTimeRun)
        mLinkedTsView?.let {
            it.removeCallbacks(it.mBackCurrentTimeRun)
        }
    }

    override fun dispatchTouchEventUp() {
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

    override fun isClick(insideX: Int, insideY: Int) {
        val bean = mRectUtil.isInRect(insideX, insideY)
        if (bean != null) {
            mOnClickListener?.invoke(this, bean)
        }
    }

    override fun onLongPressStart(insideX: Int, insideY: Int) {
        mIsLongClick = true
        mLinkedTsView?.mIsLongClick = true
        mRectUtil.longClickConditionJudge(insideX, insideY)
        mOnLongClickStartListener?.invoke(mUtil.mCondition)
    }

    private fun onLongPressEnd(condition: TSViewLongClick) {
        mIsLongClick = false
        mLinkedTsView?.mIsLongClick = false
        mOnLongClickEndListener?.invoke(condition)
    }

    override fun setLinkedViewPager2(): ViewPager2? = mLinkedViewPager2

    override fun setUpperAndLowerLimit(insideX: Int, insideY: Int): IntArray {
        TODO("Not yet implemented")
    }
}
