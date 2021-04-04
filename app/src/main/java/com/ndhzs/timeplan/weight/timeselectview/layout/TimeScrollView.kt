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
 * @description
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

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        iTimeScrollView.addChildLayout(lp, this)
        data.setOnConditionEndListener {
            onLongClickEnd(it)
        }
        moveToCenterTime()
    }

    private fun moveToCenterTime() {
        if (mData.mCenterTime == -1F) { //以当前时间线为中线
            post(object : Runnable {
                override fun run() {
                    scrollY = mTime.getNowTimeHeight() - width / 2
                    postDelayed(this, TSViewTimeUtil.DELAY_BACK_CURRENT_TIME)
                }
            })
        }else { //以mCenterTime为中线，不随时间移动
            post {
                scrollY = mTime.getTimeHeight(mData.mCenterTime) - width / 2
            }
        }
    }

    private val mBackCurrentTimeRun = Runnable {
        scrollY = if (mData.mCenterTime == -1F) {
            mTime.getNowTimeHeight() - width / 2
        }else {
            mTime.getTimeHeight(mData.mCenterTime) - width / 2
        }
    }

    override fun dispatchTouchEventDown() {
        removeCallbacks(mBackCurrentTimeRun)
    }

    override fun dispatchTouchEventUp() {
        postDelayed(mBackCurrentTimeRun, TSViewTimeUtil.DELAY_BACK_CURRENT_TIME)
    }

    override fun onInterceptTouchEventDown(x: Int, y: Int): Boolean {
        //点击的是左部区域，直接拦截
        if (x < mData.mIntervalLeft + 3) {
            return true
        }
        //对ScrollView的外部大小的上下mExtraHeight距离进行拦截
        if (y < mData.mExtraHeight || y > height - mData.mExtraHeight) {
            return true
        }
        return false
    }

    override fun onClick(insideX: Int, insideY: Int) {
        val bean = mRectManger.isInRect(insideX, insideY)
        if (bean != null) {
            mOnClickListener?.invoke(bean)
        }
    }

    override fun onLongClickStart(insideX: Int, insideY: Int) {
        mData.mIsLongClick = true
        mRectManger.longClickConditionJudge(insideX, insideY)
        upperLimit = mRectManger.getUpperLimit()
        lowerLimit = mRectManger.getLowerLimit()
        forbidSlideCenter = insideY - scrollY
        mOnLongClickStartListener?.invoke(mData.mCondition)
    }

    private fun onLongClickEnd(condition: TSViewLongClick) {
        mData.mIsLongClick = false
        mOnLongClickEndListener?.invoke(condition)
    }

    override fun setLinkedViewPager2(): ViewPager2? = mLinkedViewPager2

    private var preY = 0
    private var velocity = 0
    private var startRun = false
    private var forbidSlideCenter = 0
    private var upperLimit = Int.MIN_VALUE
    private var lowerLimit = Int.MAX_VALUE
    private val slideRunnable = object: Runnable {
        override fun run() {
            scrollBy(0, velocity)
            when (mData.mCondition) {
                TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN,
                BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN,
                EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                    //要是不手动调用，在手指不移动却又在自动滑动的情况下RctView不会自动更新
                    iTimeScrollView.slideDrawRect(scrollY + preY + velocity)
                }
                else -> {}
            }
            postDelayed(this, 20)
        }
    }
    override fun automaticSlide(x: Int, y: Int, insideX: Int, insideY: Int) {
        when (mData.mCondition) {
            TOP, TOP_SLIDE_UP, TOP_SLIDE_DOWN,
            BOTTOM, BOTTOM_SLIDE_UP, BOTTOM_SLIDE_DOWN,
            EMPTY_AREA, EMPTY_SLIDE_UP, EMPTY_SLIDE_DOWN -> {
                val isNotTopSlide = y > AUTO_MOVE_THRESHOLD
                val isNotBottomSlide = y < height - AUTO_MOVE_THRESHOLD
                val isInCanSlideLimit = insideY in upperLimit..lowerLimit
                if (isNotTopSlide && isNotBottomSlide || !isInCanSlideLimit) {
                    removeCallbacks(slideRunnable)
                    startRun = false
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
                    if (!startRun) {
                        startRun = true
                        mData.mStartAutoSlide = true
                        post(slideRunnable)
                    }
                    if (!isNotTopSlide) { //往上滑
                        velocity = -((AUTO_MOVE_THRESHOLD - y) * MULTIPLE).toInt()
                        if (y > preY + 5 || scrollY == 0) {
                            velocity = 0
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
                        velocity = ((y - (height - AUTO_MOVE_THRESHOLD)) * MULTIPLE).toInt()
                        if (y < preY - 5 || scrollY + height == mData.mTotalHeight) {
                            velocity = 0
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
                    preY = y
                }
            }
            INSIDE, INSIDE_SLIDE_UP, INSIDE_SLIDE_DOWN -> {
                mITimeScrollView.slideRectImgView(x, y)
                val top = mITimeScrollView.getOuterTop()
                val bottom = mITimeScrollView.getOuterBottom()
                val isNotTopSlide = top < AUTO_MOVE_THRESHOLD * 0.4F
                val isNotBottomSlide = bottom - scrollY > height - AUTO_MOVE_THRESHOLD * 0.4F
                val isInForbidSlideLimit = insideY in (forbidSlideCenter - 50)..(forbidSlideCenter + 50)
                if (isNotTopSlide && isNotBottomSlide || isInForbidSlideLimit) {
                    removeCallbacks(slideRunnable)
                    startRun = false
                    mData.mStartAutoSlide = false
                    mData.mCondition = INSIDE
                }else {
                    if (!startRun) {
                        startRun = true
                        mData.mStartAutoSlide = true
                        post(slideRunnable)
                    }
                    if (!isNotTopSlide) { //往上滑
                        velocity = -((AUTO_MOVE_THRESHOLD - top) * MULTIPLE).toInt()
                        if (y > preY + 5 || scrollY == 0) {
                            velocity = 0
                        }
                        mData.mCondition = INSIDE_SLIDE_UP
                    }else { //往下滑
                        velocity = ((bottom - (height - AUTO_MOVE_THRESHOLD)) * MULTIPLE).toInt()
                        if (y < preY - 5 || scrollY + height == mData.mTotalHeight) {
                            velocity = 0
                        }
                        mData.mCondition = INSIDE_SLIDE_DOWN
                    }
                    preY = y
                }
            }
            else -> {}
        }
    }
}
