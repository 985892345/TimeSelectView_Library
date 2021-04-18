package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewLongClick.*
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewTimeUtil
import com.ndhzs.timeplan.weight.timeselectview.utils.tscrollview.TScrollViewTouchEvent
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.IRectManger
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITSViewTime
import com.ndhzs.timeplan.weight.timeselectview.viewinterface.ITimeScrollView

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [com.ndhzs.timeplan.weight.timeselectview.TimeSelectView]之下，[ScrollLayout]之上
 */
@SuppressLint("ViewConstructor")
class TimeScrollView(context: Context, iTimeScrollView: ITimeScrollView, data: TSViewInternalData, time: ITSViewTime, rectManger: IRectManger) : TScrollViewTouchEvent(context) {

    /**
     * 点击当前任务的监听
     */
    fun setOnClickListener(onClick: ((bean: TSViewBean) -> Unit)) {
        mOnClickListener = onClick
    }

    /**
     * 设置长按监听接口
     */
    fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit)) {
        mOnLongClickStartListener = onStart
        mOnLongClickEndListener = onEnd
    }

    /**
     * 解决与ViewPager2的同向滑动冲突
     * @param viewPager2 传入ViewPager2，不是ViewPager
     */
    fun setLinkedViewPager2(viewPager2: ViewPager2) {
        mLinkedViewPager2 = viewPager2
    }

    /**
     * 设置能否长按
     */
    fun setIsCanLongClick(boolean: Boolean) {
        mIsCanLongClick = boolean
    }

    companion object {
        private const val AUTO_MOVE_THRESHOLD = 150 //自动滑动的阈值
        private const val MAX_AUTO_SLIDE_VELOCITY = 7 //最大滑动速度的平方
        private const val MULTIPLE = MAX_AUTO_SLIDE_VELOCITY / AUTO_MOVE_THRESHOLD.toFloat()
    }

    private val mData = data
    private val mTime = time
    private val mRectManger = rectManger
    private val mITimeScrollView = iTimeScrollView
    private var mLinkedViewPager2: ViewPager2? = null
    private var mOnClickListener: ((bean: TSViewBean) -> Unit)? = null
    private var mOnLongClickStartListener: ((condition: TSViewLongClick) -> Unit)? = null
    private var mOnLongClickEndListener: ((condition: TSViewLongClick) -> Unit)? = null

    private var mIsCanLongClick = true
    var mClickPosition: Int? = null

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, data.mInsideTotalHeight)
        iTimeScrollView.addScrollLayout(lp, this)
        data.setOnConditionEndListener {
            onLongClickEnd(it)
        }
        moveToCenterTime()
        isVerticalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
    }

    private fun moveToCenterTime() {
        if (mData.mCenterTime == -1F) { //以当前时间线为中线
            post {
                scrollY = mTime.getNowTimeHeight(TSViewTimeUtil.SCROLLVIEW_HEIGHT) - width / 2
                postDelayed(mBackNowTimeRun, TSViewTimeUtil.DELAY_NOW_TIME_REFRESH)
            }
        }else { //以mCenterTime为中线，不随时间移动
            post {
                scrollY = mTime.getTimeHeight(mData.mCenterTime, TSViewTimeUtil.SCROLLVIEW_HEIGHT) - width / 2
            }
        }
    }

    private val mBackNowTimeRun = Runnable {
        slowlyMoveTo(mTime.getNowTimeHeight(TSViewTimeUtil.SCROLLVIEW_HEIGHT) - width / 2)
    }

    private val mBackCurrentTimeRun = Runnable {
        slowlyMoveTo(if (mData.mCenterTime == -1F) {
            mTime.getNowTimeHeight(TSViewTimeUtil.SCROLLVIEW_HEIGHT) - width / 2
        }else {
            mTime.getTimeHeight(mData.mCenterTime, TSViewTimeUtil.SCROLLVIEW_HEIGHT) - width / 2
        })
    }



    override fun dispatchTouchEventDown() {
        removeCallbacks(mBackCurrentTimeRun)
    }

    override fun dispatchTouchEventUp() {
        postDelayed(mBackCurrentTimeRun, TSViewTimeUtil.DELAY_BACK_CURRENT_TIME)
    }

    override fun onInterceptTouchEventDown(outerX: Int, outerY: Int, rawX: Int, rawY: Int): Boolean {
        if (!mIsCanLongClick) {
            return true
        }
        mClickPosition = mITimeScrollView.getRectViewPosition(rawX)
        return if (mClickPosition == null) {
            true
        }else {
            //只对RectView的位置内的外部大小的RectView实际绘制区域不拦截
            outerY !in mData.mRectViewTop..mData.mRectViewBottom
        }
    }

    override fun isInLongClickArea(outerX: Int, outerY: Int, rawX: Int, rawY: Int): Boolean {
        return mClickPosition != null
    }

    override fun onClick(insideX: Int, insideY: Int) {
        if (mClickPosition != null) {
            val bean = mRectManger.getBean(insideY, mClickPosition!!)
            if (bean != null) {
                mOnClickListener?.invoke(bean)
            }
        }
    }

    override fun onLongClickStart(insideX: Int, insideY: Int, rawX: Int, rawY: Int) {
        mData.mIsLongClick = true
        mRectManger.longClickConditionJudge(insideY, mClickPosition!!) //对于刷新所有的RectView我放在了ScrollLayout中
        mUpperLimit = mRectManger.getClickUpperLimit()
        mLowerLimit = mRectManger.getClickLowerLimit()
        mForbidSlideCenter = insideY - scrollY
        mOnLongClickStartListener?.invoke(mData.mCondition)
    }

    private fun onLongClickEnd(condition: TSViewLongClick) {
        mData.mIsLongClick = false
        mOnLongClickEndListener?.invoke(condition)
    }

    override fun setLinkedViewPager2(): ViewPager2? = mLinkedViewPager2

    private var mDx = 0
    private var mDy = 0
    private var mPreOuterY = 0
    private var mVelocity = 0
    private var mStartRun = false
    private var mForbidSlideCenter = 0
    private var mUpperLimit = Int.MIN_VALUE
    private var mLowerLimit = Int.MAX_VALUE
    private val mSlideRunnable = object: Runnable {
        override fun run() {
            scrollBy(0, mVelocity)
            when (mData.mCondition) {
                TOP_SLIDE_UP, TOP_SLIDE_DOWN,
                BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN,
                EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                    //要是不手动调用，在手指不移动却又在自动滑动的情况下RctView不会自动更新
                    iTimeScrollView.slideDrawRect(scrollY + mPreOuterY)
                }
                INSIDE_SLIDE_UP, INSIDE_SLIDE_DOWN -> {
                    iTimeScrollView.slideRectImgView(mDx, mDy + mVelocity)
                }
                else -> {}
            }
            postDelayed(this, 20)
        }
    }
    override fun automaticSlide(outerX: Int, outerY: Int, insideX: Int, insideY: Int, dx: Int, dy: Int) {
        when (mData.mCondition) {
            TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN,
            BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN,
            EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                val isNotTopSlide = outerY > AUTO_MOVE_THRESHOLD
                val isNotBottomSlide = outerY < height - AUTO_MOVE_THRESHOLD
                val isInCanSlideLimit = insideY in mUpperLimit..mLowerLimit
                if (isNotTopSlide && isNotBottomSlide || !isInCanSlideLimit) {
                    removeCallbacks(mSlideRunnable)
                    mStartRun = false
                    mData.mStartAutoSlide = false
                    when (mData.mCondition) {
                        TOP_SLIDE_UP, TOP_SLIDE_DOWN -> {
                            mData.mCondition = TOP
                        }
                        BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN -> {
                            mData.mCondition = BOTTOM
                        }
                        EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                            mData.mCondition = EMPTY_AREA
                        }
                        else -> {}
                    }
                }else {
                    if (!mStartRun) {
                        mStartRun = true
                        mData.mStartAutoSlide = true
                        post(mSlideRunnable)
                    }
                    if (!isNotTopSlide) { //往上滑
                        mVelocity = -((AUTO_MOVE_THRESHOLD - outerY) * MULTIPLE).toInt()
                        if (outerY > mPreOuterY + 5 || scrollY == 0) {
                            mVelocity = 0
                        }
                        when (mData.mCondition) {
                            TOP -> {
                                mData.mCondition = TOP_SLIDE_UP
                            }
                            BOTTOM -> {
                                mData.mCondition = BOTTOM_SLIDE_UP
                            }
                            EMPTY_AREA -> {
                                mData.mCondition = EMPTY_SLIDE_UP
                            }
                            else -> {}
                        }
                    }else { //往下滑
                        mVelocity = ((outerY - (height - AUTO_MOVE_THRESHOLD)) * MULTIPLE).toInt()
                        if (outerY < mPreOuterY - 5 || scrollY + height == mData.mInsideTotalHeight) {
                            mVelocity = 0
                        }
                        when (mData.mCondition) {
                            TOP -> {
                                mData.mCondition = TOP_SLIDE_DOWN
                            }
                            BOTTOM -> {
                                mData.mCondition = BOTTOM_SLIDE_DOWN
                            }
                            EMPTY_AREA -> {
                                mData.mCondition = EMPTY_SLIDE_DOWN
                            }
                            else -> {}
                        }
                    }
                    mPreOuterY = outerY
                }
            }
            INSIDE, INSIDE_SLIDE_UP, INSIDE_SLIDE_DOWN -> {
                mDx = dx
                mDy = dy
                val top = mITimeScrollView.getOuterTop()
                val bottom = mITimeScrollView.getOuterBottom()
                val isNotTopSlide = top < AUTO_MOVE_THRESHOLD * 0.4F
                val isNotBottomSlide = bottom - scrollY > height - AUTO_MOVE_THRESHOLD * 0.4F
                val isInForbidSlideLimit = insideY in (mForbidSlideCenter - 50)..(mForbidSlideCenter + 50)
                if (isNotTopSlide && isNotBottomSlide || isInForbidSlideLimit) {
                    removeCallbacks(mSlideRunnable)
                    mStartRun = false
                    mData.mStartAutoSlide = false
                    mData.mCondition = INSIDE
                }else {
                    if (!mStartRun) {
                        mStartRun = true
                        mData.mStartAutoSlide = true
                        mForbidSlideCenter = height/2
                        post(mSlideRunnable)
                    }
                    if (!isNotTopSlide) { //往上滑
                        mVelocity = -((AUTO_MOVE_THRESHOLD - top) * MULTIPLE).toInt()
                        if (outerY > mPreOuterY + 5 || scrollY == 0) {
                            mVelocity = 0
                        }
                        mData.mCondition = INSIDE_SLIDE_UP
                    }else { //往下滑
                        mVelocity = ((bottom - (height - AUTO_MOVE_THRESHOLD)) * MULTIPLE).toInt()
                        if (outerY < mPreOuterY - 5 || scrollY + height == mData.mInsideTotalHeight) {
                            mVelocity = 0
                        }
                        mData.mCondition = INSIDE_SLIDE_DOWN
                    }
                    mPreOuterY = outerY
                }
            }
            else -> {}
        }
    }
}
