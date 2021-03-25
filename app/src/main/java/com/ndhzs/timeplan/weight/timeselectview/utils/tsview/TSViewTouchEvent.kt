package com.ndhzs.timeplan.weight.timeselectview.utils.tsview

import android.animation.ValueAnimator
import android.content.Context
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import androidx.core.animation.addListener
import androidx.viewpager2.widget.ViewPager2
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
    private var mIsAutoSlide = false
    private var mInitialX = 0
    private var mInitialY = 0
    private val mLongPressRun = Runnable {
        mIsLongPress = true
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(30)
        onLongPressStart(mInitialX + scrollX, mInitialY + scrollY)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = x
                mInitialY = y
                postDelayed(mLongPressRun, 250)
                dispatchTouchEventDown()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsLongPress) {
                    if (abs(x - mInitialX) > MOVE_THRESHOLD || abs(y - mInitialY) > MOVE_THRESHOLD) {
                        removeCallbacks(mLongPressRun)
                    }else {
                        /*
                        * 这里return true可以终止事件向下传递，意思就是MOVE事件会一直卡在这里
                        * onInterceptTouchEvent和onTouchEvent将会收不到MOVE这个事件，将不会被调用
                        * 所以这里可以用来等待长按时间结束。
                        *
                        * 如果你想在子View的onTouchEvent中判断是否是长按，就会出一个问题
                        * 一旦子View的onTouchEvent的DOWN事件return true，而你在子View的MOVE事件中又不想拦截，
                        * 想把事件给ScrollView处理，那么理所当然你想的是在子View的MOVE事件中return false，
                        * 你会以为这样ScrollView就会收到子View传来的MOVE事件，那你就大错特错了，如果在
                        * 子View的onTouchEvent的DOWN事件return true的前提下，又在子View的MOVE事件中return false，
                        * 这样的结果是MOVE事件会直接越级传递给Activity，不会再经过ScrollView
                        * */
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                removeCallbacks(mLongPressRun)
                if (mIsLongPress) {
                    mIsLongPress = false
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
                cancelSlowlyMove()
                if (onInterceptTouchEventDown(x, y)) {
                    removeCallbacks(mLongPressRun)
                    return true
                }else {
                    /*
                    * 如果不在DOWN事件手动调用onTouchEvent(), ScrollView就不会移动,
                    * 因为子View的onTouchEvent()已经把DOWN事件拦截了, ScrollView中
                    * 不执行onTouchEvent()的DOWN事件，将不会滑动
                    * */
                    onTouchEvent(ev)
                }
            }
            /*
            * onInterceptTouchEvent中一旦有一步return true，后面的所有事件都不会在接受
            * 比如，我在onInterceptTouchEvent的DOWN中return true，则onInterceptTouchEvent的MOVE、UP中就不会再接受到事件，
            * 事件会从dispatchTouchEvent的MOVE直接传递到onTouchEvent，不会再经过onInterceptTouchEvent的MOVE
            * 一句话总结就是onInterceptTouchEvent一旦return true，就不会再调用
            *
            * 所以这里的MOVE只有在完全分辨出是否是长按后才会被调用，因为前面的dispatchTouchEvent在完全判断是长按前MOVE一直
            * return true，将事件一直不向下传递
            * */
            MotionEvent.ACTION_MOVE -> {
                if (mIsLongPress) {
                    automaticSlide(x + scrollX, y + scrollY)
                }else {
                    return true
                }
            }
            /*
            * 根据上面写的注释，可得到这里的UP事件只有在DOWN、MOVE都return false的情况下才会调用
            * */
            MotionEvent.ACTION_UP -> {
                if (mIsAutoSlide) {
                    upperLimit = -1
                    lowerLimit = -1
                    automaticSlideEnd()
                }else if (abs(x - mInitialX) < MOVE_THRESHOLD && abs(y - mInitialY) < MOVE_THRESHOLD){
                    isClick(x + scrollX, y + scrollY)
                }
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val viewPager2 = setLinkedViewPager2()
        viewPager2?.let {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    it.isUserInputEnabled = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (scrollY == 0 && y > mInitialY) {
                        it.isUserInputEnabled = true
                        return false
                    }
                    if (scrollY + height == getChildAt(0).height && y < mInitialY) {
                        it.isUserInputEnabled = true
                        return false
                    }
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    private var upperLimit = -1
    private var lowerLimit = -1
    private fun automaticSlide(insideX: Int, insideY: Int) {
        if (upperLimit == -1) {
            val intArray = setUpperAndLowerLimit(insideX, insideY)
            if (intArray.size == 2) {
                upperLimit = intArray.minOrNull()!!
                lowerLimit = intArray.maxOrNull()!!
                automaticSlideStart()
                mIsAutoSlide = true
            }
        }
    }
    protected open fun automaticSlideStart() {}
    protected open fun automaticSlideEnd() {}
    protected open fun dispatchTouchEventDown() {}
    protected open fun dispatchTouchEventUp() {}
    protected open fun onInterceptTouchEventDown(x: Int, y: Int): Boolean = false
    protected open fun isClick(insideX: Int, insideY: Int) {}
    protected open fun onLongPressStart(insideX: Int, insideY: Int) {}
    protected open fun setLinkedViewPager2(): ViewPager2? = null
    protected open fun setUpperAndLowerLimit(insideX: Int, insideY: Int): IntArray = intArrayOf(-1, -1)
}