package com.ndhzs.timeplan.weight

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import androidx.core.animation.addListener
import com.ndhzs.timeplan.weight.timeselectview.layout.ChildLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TimeSelectViewUtil
import kotlin.math.abs

class TimeSelectView(context: Context, attrs: AttributeSet? = null) : ScrollView(context, attrs) {

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
        if (mUtil.mIsShowTopBottomTime != boolean) {
            mUtil.mIsShowTopBottomTime = boolean
            mChildLayout.notifyRectViewRedraw()
        }
    }

    private var mAnimator: ValueAnimator? = null
    /**
     * 与scrollTo()类似，但速度较缓慢
     */
    fun slowlyMoveTo(y: Int) {
        mAnimator = ValueAnimator.ofInt(scrollY, y)
        mAnimator?.let {
            it.addUpdateListener { animator ->
                val nowY = animator.animatedValue as Int
                scrollTo(0, nowY)
            }
            it.addListener(onEnd = {mAnimator = null})
            it.duration = abs(scrollY - y).toLong()
            it.interpolator = DecelerateInterpolator()
            it.start()
        }
    }

    /**
     * 与scrollBy()类似，但速度较缓慢，不建议短时间大量调用
     */
    fun slowlyMoveBy(dy: Int) {
        cancelSlowlyMove()
        slowlyMoveTo(scrollY + dy)
    }

    /**
     * 取消slowlyMoveTo()、slowlyMoveBy()的滑动
     */
    fun cancelSlowlyMove() {
        mAnimator?.let {
            if (it.isRunning) {
                it.cancel()
                mAnimator = null
            }
        }
    }



    private val mUtil = TimeSelectViewUtil(context, attrs)
    private val mChildLayout = ChildLayout(context, mUtil)

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(mChildLayout, lp)
    }
}