package com.ndhzs.timeselectview.utils.tscrollview

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Vibrator
import android.view.MotionEvent
import android.view.animation.*
import android.widget.ScrollView
import androidx.core.animation.addListener
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.pow

/**
 * @author 985892345
 * @date 2021/3/21
 * @description 处理[com.ndhzs.timeselectview.layout.TimeScrollView]的触摸事件
 * @param delayMillis 判定为长按所需要的时间，默认为300毫秒
 */
internal abstract class TScrollViewTouchEvent(
        context: Context,
        delayMillis: Long = 300
) : ScrollView(context) {

    private val mInsideHeight by lazy {
        getChildAt(0).height
    }
    private var mAnimator: ValueAnimator? = null
    /**
     * 与[scrollTo]类似，但速度较缓慢，有回弹效果
     */
    fun slowlyScrollTo(scrollY: Int) {
        cancelSlowlyScroll()
        var interpolator: TimeInterpolator = OvershootInterpolator(1F)
        var duration = (abs(this.scrollY - scrollY).toDouble().pow(0.3) * 66 + 80).toLong()
        var suitableScrollY = scrollY // 优化滑到边界回弹失效的处理
        if (scrollY + 20 > mInsideHeight - height) {
            suitableScrollY = mInsideHeight - height
            interpolator = DecelerateInterpolator()
            duration = 300
        }else if (scrollY <= 20) {
            suitableScrollY = 0
            interpolator = DecelerateInterpolator()
            duration = 300
        }
        mAnimator = ValueAnimator.ofInt(this.scrollY, suitableScrollY)
        mAnimator?.let {
            it.addUpdateListener { animator ->
                val nowY = animator.animatedValue as Int
                this.scrollY = nowY
            }
            it.addListener(
                    onEnd = { mAnimator = null },
                    onCancel = { mAnimator = null }
            )
            it.duration = duration
            it.interpolator = interpolator
            it.start()
        }
    }

    /**
     * 与[scrollBy]类似，但速度较缓慢，不建议短时间大量调用
     */
    fun slowlyScrollBy(dy: Int) {
        slowlyScrollTo(scrollY + dy)
    }

    /**
     * 取消[slowlyScrollTo]、[slowlyScrollBy]的滑动
     */
    fun cancelSlowlyScroll() {
        mAnimator?.let {
            if (it.isRunning) {
                it.cancel()
            }
        }
    }

    /**
     * 用来关闭还未发生的长按，长按会在长按区域点击时开起一个延时Runnable
     */
    fun closeLongClickJudge() {
        removeCallbacks(mLongClickRun)
    }

    /**
     * 重新启动长按，只能在符合长按条件下才能实现
     *
     * @param delayMillis 默认延时0.05秒
     */
    fun restartLongClickJudge(delayMillis: Long = 50) {
        if (mIsMatchLongClick) {
            removeCallbacks(mLongClickRun)
            postDelayed(mLongClickRun, delayMillis)
        }
    }

    /**
     * 得到是否是长按
     */
    fun getIsLongClick(): Boolean {
        return mIsLongClick
    }

    /**
     * 处理与ViewPager2的同向滑动冲突
     *
     * 注意：该解决方案并不完善，在滑动到边界时使ViewPager2滑动后，滑动事件就全被ViewPager2拦截，
     * 只要不松手，之后的所有滑动都只会引起ViewPager2滑动，此问题暂无法解决
     * @param onVpInterceptionStart 当开始让ViewPager2拦截时的回调
     */
    fun setLinkViewPager2(viewPager2: ViewPager2) {
        mLinkViewPager2 = viewPager2
    }

    companion object {

        /**
         * 识别是长按而能移动的阈值
         */
        const val MOVE_THRESHOLD = 5

        /**
         * 判定为长按所需要的时间，默认为300毫秒
         */
        var LONG_CLICK_TIMEOUT = 300L
    }

    init {
        LONG_CLICK_TIMEOUT = delayMillis
    }

    private var mLinkViewPager2: ViewPager2? = null
    private var mIsLongClick = false
    private var mIsMatchLongClick = true
    private var mOuterInitialX = 0
    private var mOuterInitialY = 0
    private var mInitialRawX = 0
    private var mInitialRawY = 0
    private val mLongClickRun = Runnable {
        mIsLongClick = true
        mIsMatchLongClick = false
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(30)
        onLongClickStart(mOuterInitialX + scrollX, mOuterInitialY + scrollY, mInitialRawX, mInitialRawY)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        val rawX = ev.rawX.toInt()
        val rawY = ev.rawY.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mOuterInitialX = x
                mOuterInitialY = y
                mInitialRawX = rawX
                mInitialRawY = rawY
                mIsLongClick = false
                mIsMatchLongClick = true
                postDelayed(mLongClickRun, LONG_CLICK_TIMEOUT)
                dispatchTouchEventDown(x, y, rawX, rawY)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsMatchLongClick) {
                    if (isInLongClickArea(x, y, rawX, rawY)) {
                        if (abs(rawX - mInitialRawX) > MOVE_THRESHOLD
                                || abs(rawY - mInitialRawY) > MOVE_THRESHOLD) {
                            removeCallbacks(mLongClickRun)
                            mIsMatchLongClick = false
                        }else {
                            if (mLinkViewPager2 != null) {
                                mLinkViewPager2!!.parent.requestDisallowInterceptTouchEvent(true)
                            }else {
                                parent.requestDisallowInterceptTouchEvent(true)
                            }
                            /*
                            * 这里 return true 可以终止事件向下传递，意思就是 MOVE 事件会一直卡在这里
                            * onInterceptTouchEvent 和 onTouchEvent 将会收不到 MOVE 这个事件，将不会被调用
                            * 所以这里可以用来等待长按时间结束。
                            *
                            * 如果你想在子 View 的 onTouchEvent 中判断是否是长按，就会出一个问题
                            * 一旦子 View 的 onTouchEvent 的 DOWN 事件 return true，而你在子 View 的 MOVE事件中又不想拦截，
                            * 想把事件给 ScrollView 处理，那么理所当然你想的是在子 View的 MOVE 事件中 return false，
                            * 你会以为这样 ScrollView 就会收到子 View 传来的 MOVE 事件，那你就大错特错了，如果在
                            * 子 View 的 onTouchEvent 的 DOWN 事件 return true 的前提下，又在子 View 的 MOVE 事件中
                            * return false，这样的结果是 MOVE 事件会直接越级传递给 Activity，不会再经过 ScrollView
                            * */
                            return true
                        }
                    }else {
                        removeCallbacks(mLongClickRun)
                        mIsMatchLongClick = false
                    }
                }else {
                    if (mLinkViewPager2 != null) {
                        mLinkViewPager2!!.parent.requestDisallowInterceptTouchEvent(true)
                    }else {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mIsMatchLongClick = false
                dispatchTouchEventUp(x, y, rawX, rawY)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private var mIsMove = false
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        val rawX = ev.rawX.toInt()
        val rawY = ev.rawY.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mIsMove = false
                cancelSlowlyScroll()
                if (onInterceptTouchEventDown(x, y, rawX, rawY)) {
                    removeCallbacks(mLongClickRun)
                    return true
                }else {
                    /*
                    * 如果不在 DOWN 事件手动调用 onTouchEvent(), ScrollView 就不会移动,
                    * 因为子 View 的 onTouchEvent() 已经把 DOWN 事件拦截了, ScrollView 中
                    * 不执行 onTouchEvent() 的 DOWN 事件，将不会滑动
                    * */
                    onTouchEvent(ev)
                }
            }
            /*
            * onInterceptTouchEvent 只有一次 return true 的机会，一旦使用后，后面的所有事件在
            * onInterceptTouchEvent 都不会再被调用
            *
            * 比如，我在 onInterceptTouchEvent 的 DOWN 中 return true，则 onInterceptTouchEvent
            * 的 MOVE、UP 中就不会再接受到事件，事件会从 dispatchTouchEvent 的 MOVE 直接传递到
            * onTouchEvent，不会再经过 onInterceptTouchEvent 的 MOVE 一句话总结就是 onInterceptTouchEvent
            * 一旦 return true，就不会再被调用
            *
            * 所以这里的 MOVE 只有在完全分辨出是否是长按且没有在 onInterceptTouchEvent 的 Down 事件中被 return true
            * 才会被调用
            * */
            MotionEvent.ACTION_MOVE -> {
                mIsMove = true // 只有在大于了移动阈值后才会调用
                if (mIsLongClick) {
                    automaticSlide(x, y, x + scrollX, y + scrollY)
                }else {
                    return true // 不是长按直接拦截
                }
            }
            /*
            * 根据上面写的注释，可得到这里的 UP 事件只有在 DOWN、MOVE 都 return false 的情况下才会调用
            * */
            MotionEvent.ACTION_UP -> {
                if (abs(x - mOuterInitialX) < MOVE_THRESHOLD && abs(y - mOuterInitialY) < MOVE_THRESHOLD){
                    if (!mIsLongClick) {
                        removeCallbacks(mLongClickRun)
                        return onClick(x + scrollX, y + scrollY)
                    }else {
                        if (!mIsMove) { //长按却没有移动
                            onLongClickStartButNotMove()
                        }
                    }
                }
            }
        }
        return false
    }

    private var judgeNumber = 0
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                judgeNumber = 0
            }
            MotionEvent.ACTION_MOVE -> {
                judgeNumber++
                if (judgeNumber < 3) {
                    return true
                }else if (judgeNumber == 3){
                    if (abs(x - mOuterInitialX) > abs(y - mOuterInitialY)) {
                        if (mLinkViewPager2 != null) {
                            mLinkViewPager2!!.parent.requestDisallowInterceptTouchEvent(false)
                        }else {
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                        return false
                    }
                }
            }
        }
        if (mLinkViewPager2 != null) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mLinkViewPager2!!.isUserInputEnabled = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (scrollY == 0 && ev.y > mOuterInitialY) {
                        //一旦设置成true后，所有的事件都将会被ViewPager2拦截，再设置成false将无法被调用
                        //除非能在ViewPager2的父布局进行单独控制
                        mLinkViewPager2!!.isUserInputEnabled = true
                        return false
                    }
                    if (scrollY + height == getChildAt(0).height && ev.y < mOuterInitialY) {

                        mLinkViewPager2!!.isUserInputEnabled = true
                        return false
                    }
                }
            }
        }
        super.onTouchEvent(ev)
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelSlowlyScroll()
    }

    /**
     * 事件分发中的DOWN事件处理
     */
    protected open fun dispatchTouchEventDown(outerX: Int, outerY: Int, onScreenX: Int, onScreenY: Int) {}

    /**
     * 事件分发中的UP事件处理
     */
    protected open fun dispatchTouchEventUp(outerX: Int, outerY: Int, onScreenX: Int, onScreenY: Int) {}

    /**
     * 如果是长按区域，则在dispatchTouchEvent的Move事件中会进行判断，要么到长按的时间成为长按，要么滑动的距离超过阈值不为长按，不然Move事件将一直不向下分发
     */
    protected open fun isInLongClickArea(outerX: Int, outerY: Int, onScreenX: Int, onScreenY: Int): Boolean = false

    /**
     * 在onInterceptTouchEvent的Down事件直接拦截并移除长按延时Runnable
     */
    protected open fun onInterceptTouchEventDown(outerX: Int, outerY: Int, onScreenX: Int, onScreenY: Int): Boolean = false

    /**
     * 调用此方法说明长按已经发生，但手指却没有移动或移动距离在阈值以内，如果你在[onLongClickStart]调用了一些东西，可以在这里取消他们
     *
     * @return 此方法因为在onInterceptTouchEvent的UP事件中被调用，返回true可以防止事件向下传递
     */
    abstract fun onLongClickStartButNotMove(): Boolean

    /**
     * 没有大范围和长距离的移动时调用的方法，简单来说就是一次点击事件
     *
     * 此时已经经过了[dispatchTouchEventDown]、[dispatchTouchEventUp]、[onInterceptTouchEventDown]
     * @return 此方法因为在onInterceptTouchEvent的UP事件中被调用，返回true可以防止事件向下传递
     */
    protected open fun onClick(insideX: Int, insideY: Int): Boolean = false

    /**
     * 通知长按开始的方法
     *
     * [onLongClickStartButNotMove]该方法可能你需要重写
     */
    abstract fun onLongClickStart(insideX: Int, insideY: Int, onScreenX: Int, onScreenY: Int)

    /**
     * 自动滑动的处理，没有默认实现
     *
     * 该方法是在onInterceptTouchEvent的MOVE事件中被调用，此时事件并不是ScrollView在拦截，事件已经传递给子View了
     */
    protected open fun automaticSlide(outerX: Int, outerY: Int, insideX: Int, insideY: Int) {}
}