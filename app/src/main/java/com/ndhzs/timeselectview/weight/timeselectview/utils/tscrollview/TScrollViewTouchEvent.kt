package com.ndhzs.timeselectview.weight.timeselectview.utils.tscrollview

import android.animation.ValueAnimator
import android.content.Context
import android.os.Vibrator
import android.view.MotionEvent
import android.view.animation.OvershootInterpolator
import android.widget.ScrollView
import androidx.core.animation.addListener
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.pow

/**
 * @author 985892345
 * @date 2021/3/21
 * @description 处理[com.ndhzs.timeselectview.weight.timeselectview.layout.TimeScrollView]的触摸事件
 * @param delayMillis 判定为长按所需要的时间，默认为300毫秒
 */
abstract class TScrollViewTouchEvent(context: Context, delayMillis: Long = 300) : ScrollView(context) {

    private var mAnimator: ValueAnimator? = null
    /**
     * 与[scrollTo]类似，但速度较缓慢
     */
    fun slowlyScrollTo(scrollY: Int) {
        cancelSlowlyScroll()
        mAnimator = ValueAnimator.ofInt(this.scrollY, scrollY)
        mAnimator?.let {
            it.addUpdateListener { animator ->
                val nowY = animator.animatedValue as Int
                this.scrollY = nowY
            }
            it.addListener(onEnd = { mAnimator = null })
            it.duration = (abs(this.scrollY - scrollY).toDouble().pow(0.3) * 66 + 80).toLong()
            it.interpolator = OvershootInterpolator(1F)
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
                mAnimator = null
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
    fun setLinkViewPager2(viewPager2: ViewPager2, onVpInterceptionStart: ((linkedViewPager2: ViewPager2, rawY: Float) -> Unit)? = null) {
        mLinkViewPager2 = viewPager2
        mOnVpInterceptionStart = onVpInterceptionStart
    }

    companion object {
        /**
         * 识别是长按而能移动的阈值
         */
        const val MOVE_THRESHOLD = 5
    }

    private var mLinkViewPager2: ViewPager2? = null
    private var mOnVpInterceptionStart: ((linkedViewPager2: ViewPager2, rawY: Float) -> Unit)? = null

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

    private val mDelayMillis = delayMillis
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
                postDelayed(mLongClickRun, mDelayMillis)
                dispatchTouchEventDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsLongClick) {
                    if (isInLongClickArea(x, y, rawX, rawY)) {
                        if (abs(rawX - mInitialRawX) > MOVE_THRESHOLD || abs(rawY - mInitialRawY) > MOVE_THRESHOLD) {
                            removeCallbacks(mLongClickRun)
                            mIsMatchLongClick = false
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
                    }else {
                        removeCallbacks(mLongClickRun)
                        mIsMatchLongClick = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mIsMatchLongClick = false
                dispatchTouchEventUp(x, y)
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
                mIsMove = true //只有在大于了移动阈值后才会调用
                if (mIsLongClick) {
                    automaticSlide(x, y, x + scrollX, y + scrollY)
                }else {
                    return true
                }
            }
            /*
            * 根据上面写的注释，可得到这里的UP事件只有在DOWN、MOVE都return false的情况下才会调用
            * */
            MotionEvent.ACTION_UP -> {
                if (abs(x - mOuterInitialX) < MOVE_THRESHOLD && abs(y - mOuterInitialY) < MOVE_THRESHOLD){
                    if (!mIsLongClick) {
                        removeCallbacks(mLongClickRun)
                        return onClick(x + scrollX, y + scrollY)
                    }else {
                        if (!mIsMove) { //长按却没有移动
                            return onLongClickStartButNotMove()
                        }
                    }
                }
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mLinkViewPager2?.let {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    it.isUserInputEnabled = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (scrollY == 0 && ev.y > mOuterInitialY) {
                        //一旦设置成true后，所有的事件都将会被ViewPager2拦截，再设置成false将无法被调用
                        //除非能在ViewPager2的父布局进行单独控制
                        it.isUserInputEnabled = true
                        mOnVpInterceptionStart?.invoke(it, ev.rawY)
                        return false
                    }
                    if (scrollY + height == getChildAt(0).height && ev.y < mOuterInitialY) {
                        it.isUserInputEnabled = true
                        mOnVpInterceptionStart?.invoke(it, ev.rawY)
                        return false
                    }
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    /**
     * 事件分发中的DOWN事件处理
     */
    protected open fun dispatchTouchEventDown(outerX: Int, outerY: Int) {}

    /**
     * 事件分发中的UP事件处理
     */
    protected open fun dispatchTouchEventUp(outerX: Int, outerY: Int) {}

    /**
     * 在onInterceptTouchEvent的Down事件直接拦截并移除长按延时Runnable
     */
    protected open fun onInterceptTouchEventDown(outerX: Int, outerY: Int, onScreenX: Int, onScreenY: Int): Boolean = false

    /**
     * 如果是长按区域，则在dispatchTouchEvent的Move事件中会进行判断，要么到长按的时间成为长按，要么滑动的距离超过阈值不为长按，不然Move事件将一直不向下分发
     */
    protected open fun isInLongClickArea(outerX: Int, outerY: Int, onScreenX: Int, onScreenY: Int): Boolean = false

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