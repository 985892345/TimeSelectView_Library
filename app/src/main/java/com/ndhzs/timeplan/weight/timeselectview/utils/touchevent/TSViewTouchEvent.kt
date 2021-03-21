package com.ndhzs.timeplan.weight.timeselectview.utils.touchevent

import android.animation.ValueAnimator
import android.content.Context
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import androidx.core.animation.addListener
import kotlin.math.abs

/**
 * @author 985892345
 * @date 2021/3/21
 * @description 处理TimeSelectView的触摸事件
 */
abstract class TSViewTouchEvent(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {

    private var mAnimator: ValueAnimator? = null
    /**
     * 与scrollTo()类似，但速度较缓慢
     */
    fun slowlyMoveTo(y: Int) {
        mAnimator = ValueAnimator.ofInt(scrollY, y)
        mAnimator?.let {
            it.addUpdateListener { animator ->
                val nowY = animator.animatedValue as Int
                scrollY = nowY
            }
            it.addListener(onEnd = { mAnimator = null })
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

    fun getIsLongPress(): Boolean = mIsLongPress

    companion object {
        private const val MOVE_THRESHOLD = 5 //识别是长按而能移动的阈值
    }

    private var mIsLongPress = false
    private var mInitialX = 0
    private var mInitialY = 0
    private val mLongPressRun = Runnable {
        mIsLongPress = true
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(30)
        onLongPressStart()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = x
                mInitialY = y
                postDelayed(mLongPressRun, 250)
                /*
                 * 如果不在DOWN事件手动调用onTouchEvent(), ScrollView就不会移动,
                 * 因为子View的onTouchEvent()已经把DOWN事件拦截了, ScrollView中
                 * 不执行onTouchEvent()的DOWN事件，将不会滑动
                 * */
                onTouchEvent(ev)
                cancelSlowlyMove()
                dispatchTouchEventDown()
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                removeCallbacks(mLongPressRun)
                if (mIsLongPress) {
                    mIsLongPress = false
                    onLongPressEnd()
                }
                dispatchTouchEventUp()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (onInterceptTouchEventDown(x, y)) {
                    removeCallbacks(mLongPressRun)
                }
            }
            MotionEvent.ACTION_MOVE -> {
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {

            }
        }
        return super.onTouchEvent(ev)
    }

    abstract fun dispatchTouchEventDown()
    abstract fun dispatchTouchEventUp()
    abstract fun onInterceptTouchEventDown(x: Int, y: Int): Boolean
    abstract fun onLongPressStart()
    abstract fun onLongPressEnd()
}