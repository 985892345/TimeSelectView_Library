package com.ndhzs.timeselectview.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.ndhzs.timeselectview.R
import com.ndhzs.timeselectview.TimeSelectView
import com.ndhzs.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.layout.view.RectImgView
import com.ndhzs.timeselectview.layout.view.SeparatorLineView
import java.io.PrintWriter

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/2
 * @description
 */
internal class TSViewInternalData(context: Context, attrs: AttributeSet? = null) {

    val mCenterTime: Float //当前时间线，支持小数

    val mTimeTextSize: Float //时间字体大小
    val mTaskTextSize: Float //任务字体大小

    val mDefaultBorderColor:Int //默认矩形边框颜色
    val mDefaultInsideColor:Int //默认矩形内部颜色
    val mDefaultTaskName: String //默认任务名称

    var mTimeInterval: Int //时间间隔数
    private var mTimelineRange: Int //单个时间轴的时长，不允许其他类得到这个值，为防止出现高度问题，请用mTimeRangeArray计算得到
    var mTimeRangeArray = Array(2) { intArrayOf(0) } //时间轴的时间范围值
        private set

    val mCardCornerRadius: Float //时间轴背景的圆角度数

    val mTSViewAmount: Int //时间轴个数
    val mTimelineWidth: Int //时间轴宽度
    val mTimelineInterval: Int //相邻时间轴间隔
    val mAllTimelineWidth: Int //全部时间轴总宽度

    val mExtraHeight:Int //上方和下方多的高度
    val mIntervalLeft: Int //左边时间刻度文字间隔宽度
    val mIntervalRight: Int //右侧水平线空出来的宽度
    val mIntervalHeight: Int //一个小时的间隔高度
    val mInsideTotalHeight: Int //总高度
    val mRectViewWidth: Int //矩形绘制宽度

    val mRectViewTop: Int //RectView实际绘制区域的顶部值
    val mRectViewBottom: Int //RectView实际绘制区域的底部值

    var mDragResistance = RectImgView.DEFAULT_DRAG_RESISTANCE //拖动阻力值

    var mIsShowDiffTime: Boolean //最终的任务区域是否显示时间差
    var mIsShowStartEndTime: Boolean //最终的任务区域是否显示上下边界时间

    var mDataChangeListener: TimeSelectView.OnDataChangeListener? = null //数据改变监听

    var mIsLongClick = false //是否处于长按状态
        set(value) {
            if (value) {
                TSViewLongClick.sIsLongClickCount++
            }else {
                TSViewLongClick.sIsLongClickCount--
            }
            field = value
        }
    var mStartAutoSlide = false //是否开启自动滑动

    var mCondition = TSViewLongClick.NULL //长按状态
        set(value) {
            if (value == TSViewLongClick.NULL) {
                onConditionEndListener?.invoke(mCondition)
            }
            field = value
        }
    private var onConditionEndListener: ((condition: TSViewLongClick) -> Unit)? = null //长按结束的回调

    var mOnClickListener: ((taskBean: TSViewTaskBean) -> Unit)? = null
    var mOnLongClickStartListener: ((condition: TSViewLongClick) -> Unit)? = null
    var mOnLongClickEndListener: ((condition: TSViewLongClick) -> Unit)? = null

    var mOnScrollListener: ((scrollY: Int, vpPosition: Int) -> Unit)? = null

    /**
     * 设置长按结束的监听
     */
    fun setOnConditionEndListener(l: ((condition: TSViewLongClick) -> Unit)) {
        onConditionEndListener = l
    }

    init {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView)
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, TSViewTimeUtil.CENTER_TIME_CENTER)

        mDefaultBorderColor = ty.getColor(R.styleable.TimeSelectView_defaultBorderColor, 0xFFFF0000.toInt())
        mDefaultInsideColor = ty.getColor(R.styleable.TimeSelectView_defaultInsideColor, 0xFFDCCC48.toInt())
        mDefaultTaskName = ty.getString(R.styleable.TimeSelectView_defaultTaskName).toString()

        mCardCornerRadius = ty.getDimension(R.styleable.TimeSelectView_cardRadius, 28F)
        mIntervalRight = 10

        mTimeInterval = ty.getInt(R.styleable.TimeSelectView_timeInterval, 15)
        if (60 % mTimeInterval != 0) mTimeInterval = 15

        val amount = ty.getInt(R.styleable.TimeSelectView_amount, 1)
        mTSViewAmount = if (amount < 1) 1 else amount
        mTimelineWidth = ty.getDimension(R.styleable.TimeSelectView_timelineWidth, 300F).toInt()
        mTimelineInterval = ty.getDimension(R.styleable.TimeSelectView_timelineInterval, 20F).toInt()
        mAllTimelineWidth = mTSViewAmount * (mTimelineWidth + mTimelineInterval) - mTimelineInterval


        val timeRangeArray = ty.getString(R.styleable.TimeSelectView_timeRangeArray)
        mTimelineRange = 24 / mTSViewAmount //下一步设置mTimeRangeArray会重新赋值
        mTimeRangeArray = getTimeRangeArray(timeRangeArray)



        mIntervalLeft = ty.getDimension(R.styleable.TimeSelectView_intervalLeft, mTimelineWidth / 4.6F).toInt()
        mIntervalHeight = ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 360F).toInt()
        mExtraHeight = mIntervalHeight / 2
        mInsideTotalHeight = mTimelineRange * mIntervalHeight + 2 * mExtraHeight
        mRectViewWidth = mTimelineWidth - mIntervalLeft - mIntervalRight

        mTimeTextSize = ty.getDimension(R.styleable.TimeSelectView_timeTextSize, mIntervalLeft / 2.8F)
        mTaskTextSize = ty.getDimension(R.styleable.TimeSelectView_taskTextSize, mTimeTextSize * 1.16F)

        mRectViewTop = mExtraHeight + SeparatorLineView.HORIZONTAL_LINE_WIDTH
        mRectViewBottom = mInsideTotalHeight - mExtraHeight

        mIsShowDiffTime = ty.getBoolean(R.styleable.TimeSelectView_isShowDiffTime, false)
        mIsShowStartEndTime = ty.getBoolean(R.styleable.TimeSelectView_isShowTopBottomTime, true)
        ty.recycle()
    }

    private fun getTimeRangeArray(timeRange: String?): Array<IntArray> {
        var timeRangeArray = Array(2) { intArrayOf(0) }
        try {
            val times = timeRange?.split(",")
            timeRangeArray = Array(mTSViewAmount) {
                if (times == null) {
                    intArrayOf(it * mTimelineRange, (it + 1) * mTimelineRange)
                }else {
                    val hours = times[it].split("-")
                    val startHour = hours[0].toInt()
                    var endHour = hours[1].toInt()
                    if (startHour >= 24 || endHour > 24) {
                        Log.e("TimeSelectView", "**********************************************************************************")
                        Log.e("TimeSelectView", "****  Your TimeSelectView of timeRangeArray's startHour or endHour is wrong!  ****")
                        Log.e("TimeSelectView", "**********************************************************************************")
                        val e = Exception()
                        e.printStackTrace(PrintWriter("****  Your TimeSelectView of timeRangeArray's startHour or endHour is wrong!  ****"))
                        throw Exception()
                    }
                    if (endHour <= startHour) {
                        endHour += 24
                    }
                    mTimelineRange = endHour - startHour
                    intArrayOf(startHour, endHour)
                }
            }
        }catch (e: Exception) {
            Log.e("TimeSelectView", "************************************************************")
            Log.e("TimeSelectView", "****  Your TimeSelectView of timeRangeArray is wrong!   ****")
            Log.e("TimeSelectView", "************************************************************")
            e.printStackTrace(PrintWriter("******Your TimeSelectView of timeRangeArray is wrong! ******"))
        }
        return timeRangeArray
    }
}