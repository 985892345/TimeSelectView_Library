package com.ndhzs.timeselectview.layout.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import com.ndhzs.timeselectview.bean.TSViewTaskBean
import com.ndhzs.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeselectview.utils.TSViewLongClick
import com.ndhzs.timeselectview.viewinterface.IRectDraw
import com.ndhzs.timeselectview.viewinterface.IRectImgView
import com.ndhzs.timeselectview.viewinterface.ITSViewTimeUtil
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author 985892345
 * @date 2021/3/20
 * @description [com.ndhzs.timeselectview.layout.ScrollLayout]之下
 */
@SuppressLint("ViewConstructor")
internal class RectImgView(context: Context, iRectImgView: IRectImgView, data: TSViewInternalData, time: ITSViewTimeUtil, draw: IRectDraw) : View(context) {

    /**
     * 整体移动开始时调用
     * @param rect RectView内部坐标值的Rect
     */
    fun start(rect: Rect, taskBean: TSViewTaskBean, position: Int) {
        mPosition = position
        mRectViewInterval = mIRectImgView.getRectViewInterval()
        val distance = mRectViewToRectImgViewDistances[position]
        mRect.left = rect.left + distance
        mRect.top = rect.top
        mRect.right = rect.right + distance
        mRect.bottom = rect.bottom
        mInitialRect.set(mRect)
        mTaskBean = taskBean
        invalidate()
    }

    /**
     * 整体移动结束时调用
     */
    fun over(inWindowFinalLeft: Int, insideFinalTop: Int, onEndListener : () -> Unit?) {
        val location = IntArray(2)
        getLocationInWindow(location)
        val insideFinalLeft = inWindowFinalLeft - location[0]
        val dTop = mRect.top - insideFinalTop
        val dLeft = mRect.left - insideFinalLeft
        val rectWidth = mRect.width()
        val rectHeight = mRect.height()
        val totalDistance = sqrt((dTop * dTop + dLeft * dLeft).toFloat())
        val animator = ValueAnimator.ofFloat(totalDistance, 0F)
        animator.addUpdateListener {
            val nowDistance = it.animatedValue as Float
            mRect.left = (nowDistance / totalDistance * dLeft).toInt() + insideFinalLeft
            mRect.top = (nowDistance / totalDistance * dTop).toInt() + insideFinalTop
            mRect.right = mRect.left + rectWidth
            mRect.bottom = mRect.top + rectHeight
            invalidate()
        }
        animator.addListener(onEnd = {
            onEndListener.invoke()
            mRect.setEmpty()
            invalidate()
        })
        animator.duration = (totalDistance.toDouble().pow(0.3) * 66 + 80).toLong()
        animator.interpolator = OvershootInterpolator(0.9F)
        animator.start()
    }

    /**
     * 用于特殊情况直接取消[RectImgView]的矩形
     */
    fun forcedEnd() {
        mRect.setEmpty()
        invalidate()
    }

    /**
     * 整体移动到删除区域时调用，会有一个删除动画
     */
    fun delete(onEndListener : () -> Unit?) {
        val rectWidth = mRect.width()
        val rectHeight = mRect.height()
        val rectCenterX = mRect.centerX()
        val rectCenterY = mRect.centerY()
        val animator = ValueAnimator.ofInt(rectWidth, 0)
        animator.addUpdateListener {
            val width = it.animatedValue as Int
            val height = (width / rectWidth.toFloat() * rectHeight).toInt()
            mRect.left = rectCenterX - width/2
            mRect.top = rectCenterY - height/2
            mRect.right = rectCenterX + width/2
            mRect.bottom = rectCenterY + height/2
            mTimeSize = width / rectWidth.toFloat() * mData.mTimeTextSize
            mTaskNameSize = width / rectWidth.toFloat() * mData.mTaskTextSize
            invalidate()
        }
        animator.addListener(onEnd = {
            onEndListener.invoke()
            mRect.setEmpty()
            mTimeSize = mData.mTimeTextSize
            mTaskNameSize = mData.mTaskTextSize
            invalidate()
        })
        animator.duration = 350
        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }

    /**
     * 返回整体移动矩形的顶部高度值，为内部坐标值
     * @return 返回内部坐标值
     */
    fun getInsideTop(): Int {
        return mRect.top
    }

    /**
     * 返回整体移动矩形的底部高度值，为内部坐标值
     * @return 返回内部坐标值
     */
    fun getInsideBottom(): Int {
        return mRect.bottom
    }

    /**
     * 返回变化的矩形
     *
     * left、right为相对于Activity的值，top、bottom为insideY值的Rect
     * @return Rect的left、right值是相对于屏幕的值，top、bottom为内部值
     */
    fun getInWindowRect(): Rect {
        val location = IntArray(2)
        getLocationInWindow(location)
        return Rect(mRect.left + location[0], mRect.top, mRect.right + location[0], mRect.bottom)
    }

    /**
     * 返回移动前的矩形
     *
     * left、right为相对于屏幕的值，top、bottom为insideY值的Rect
     * @return Rect的left、right值是相对于屏幕的值，top、bottom为内部值
     */
    fun getRawInitialRect(): Rect {
        val location = IntArray(2)
        getLocationInWindow(location)
        return Rect(mInitialRect.left + location[0], mInitialRect.top, mInitialRect.right + location[0], mInitialRect.bottom)
    }

    /**
     * 整体滑动时调用
     * @param dx 与初始位置的差值，大于0向右移动
     * @param dy 与初始位置的差值，大于0向右移动
     */
    fun slideRectImgView(dx: Int, dy: Int) {
        val dx1 = getCorrectDx(dx, mPosition)
        mRect.left = dx1 + mInitialRect.left
        mRect.top = dy + mInitialRect.top
        mRect.right = dx1 + mInitialRect.right
        mRect.bottom = dy + mInitialRect.bottom
        invalidate()
    }

    /**
     * 因为CardView的圆角问题，上下滑动时，边界的显示区域会覆盖圆角，所以提供此方法在ScrollView滑动时调用刷新
     */
    fun boundaryRefresh(scrollY: Int, insideHeight: Int, outerHeight: Int) {
        if (scrollY !in mData.mExtraHeight..insideHeight - mData.mExtraHeight - outerHeight) { //只有能显示边界时才刷新
            mDrawBoundaryDiffHeight = if (scrollY in 0..mData.mExtraHeight) { //说明时间轴处于顶部
                scrollY
            }else { //说明时间轴处于底部
                insideHeight - scrollY - outerHeight
            }
            when (mData.mCondition) { //除了整体移动以外，其他情况都可以刷新，原因是整体移动已经被其他方法调用刷新
                TSViewLongClick.INSIDE, TSViewLongClick.INSIDE_SLIDE_UP, TSViewLongClick.INSIDE_SLIDE_DOWN -> {}
                else -> {
                    invalidate()
                }
            }
        }
    }

    /**
     * 因为CardView的圆角问题，上下滑动时，边界的显示区域会覆盖圆角,
     * 所以提供此值动态修改绘图的边界到底部或顶部的距离，与CardView的圆角相配合
     */
    private var mDrawBoundaryDiffHeight = 0


    private val mData = data
    private val mDraw = draw
    private val mTime = time
    private val mIRectImgView = iRectImgView
    private val mRect = Rect()
    private val mInitialRect = Rect()
    private var mPosition = 0
    private lateinit var mTaskBean: TSViewTaskBean
    private var mRectViewInterval = 0

    private var mTimeSize = mData.mTimeTextSize
    private var mTaskNameSize = mData.mTaskTextSize

    companion object {
        /**
         * 在多个时间轴中左右拖动时的默认阻力值
         */
        const val DEFAULT_DRAG_RESISTANCE = 17
    }

    private val mBoundaryPath1 = Path() //上下边界区域路1，与区域2取交集
    private val mBoundaryPath2 = Path() //上下边界区域路径2，与区域1取交集
    private val mBoundaryPaint = Paint() //上下边界区域画笔
    private val mDividerLines = IntArray(data.mTSViewAmount + 1) //分割线的距离值，用于整体移动到另一个时间轴时改变时间
    private val mRectViewToRectImgViewDistances = IntArray(data.mTSViewAmount) //每个RectView的left到自身left的距离

    init {
        //上下边界区域画笔
        mBoundaryPaint.color = 0x33D8D8D8
        mBoundaryPaint.style = Paint.Style.FILL

        post {
            repeat(data.mTSViewAmount) { //计算每个RectView的left到自身left的距离
                mRectViewToRectImgViewDistances[it] = iRectImgView.getRectViewToRectImgViewDistance(it)
            }
            for (i in mDividerLines.indices) { //计算除了第0个以外的每个ChildLayout的left到自身的距离，第一个值为无穷小，最后一个值为无穷大
                if (i == 0) {
                    mDividerLines[i] = Int.MIN_VALUE
                }else if (i == mDividerLines.size - 1) {
                    mDividerLines[i] = Int.MAX_VALUE
                }else {
                    mDividerLines[i] = mRectViewToRectImgViewDistances[i] - mData.mIntervalLeft
                }
            }
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!mRect.isEmpty) {
            mDraw.drawRect(canvas, mRect, mTaskBean.name, mTaskBean.borderColor, mTaskBean.insideColor, mTaskNameSize)
            val top = mRect.top
            val bottom = mRect.bottom
            for (i in 0 until mDividerLines.size - 1) {
                if (mRect.left in mDividerLines[i]..mDividerLines[i + 1]) {
                    mDraw.drawStartEndTime(canvas, mRect, mTime.getTime(top, i), mTime.getTime(bottom + 1, i), mTimeSize)
                }
            }
            if (mData.mIsShowDiffTime) {
                mDraw.drawArrows(canvas, mRect, mTaskBean.diffTime, mTimeSize)
            }
        }

        if (mRectViewToRectImgViewDistances[0] != 0) {
            //绘制顶部与底部的灰色区域
            repeat(mData.mTSViewAmount) {
                val left = mRectViewToRectImgViewDistances[it].toFloat()
                val right = mRectViewToRectImgViewDistances[it].toFloat() + mData.mRectViewWidth + mData.mIntervalRight
                val height = mData.mExtraHeight.toFloat() + SeparatorLineView.HORIZONTAL_LINE_WIDTH
                val radius = mData.mCardCornerRadius
                //绘制上方边界
                mBoundaryPath1.moveTo(right - radius, mDrawBoundaryDiffHeight.toFloat())
                mBoundaryPath1.lineTo(left, mDrawBoundaryDiffHeight.toFloat())
                mBoundaryPath1.lineTo(left, height)
                mBoundaryPath1.lineTo(right, height)
                mBoundaryPath1.lineTo(right, radius + mDrawBoundaryDiffHeight)
                mBoundaryPath1.quadTo(right, mDrawBoundaryDiffHeight.toFloat(), right - radius, mDrawBoundaryDiffHeight.toFloat())
                //取交集，如果不取交集，则会有一个点绘在了CardView圆角之外
                mBoundaryPath2.addRect(left, 0F, right, height, Path.Direction.CCW)
                mBoundaryPath1.op(mBoundaryPath2, Path.Op.INTERSECT)
                canvas.drawPath(mBoundaryPath1, mBoundaryPaint)
                mBoundaryPath1.reset()
                mBoundaryPath2.reset()

                val top = this.height - mData.mExtraHeight.toFloat()
                val bottom = top + height - SeparatorLineView.HORIZONTAL_LINE_WIDTH
                //绘制下方边界
                mBoundaryPath1.moveTo(right - radius, bottom - mDrawBoundaryDiffHeight)
                mBoundaryPath1.lineTo(left, bottom - mDrawBoundaryDiffHeight)
                mBoundaryPath1.lineTo(left, top)
                mBoundaryPath1.lineTo(right, top)
                mBoundaryPath1.lineTo(right, bottom - mDrawBoundaryDiffHeight - radius)
                mBoundaryPath1.quadTo(right, bottom - mDrawBoundaryDiffHeight, right - radius, bottom - mDrawBoundaryDiffHeight)
                //取交集
                mBoundaryPath2.addRect(left, top, right, bottom, Path.Direction.CCW)
                mBoundaryPath1.op(mBoundaryPath2, Path.Op.INTERSECT)
                canvas.drawPath(mBoundaryPath1, mBoundaryPaint)
                mBoundaryPath1.reset()
                mBoundaryPath2.reset()
            }
        }
    }

    /**
     * 计算左右移动的阻力值
     */
    private fun getCorrectDx(dx: Int, position: Int): Int {
        if (dx == 0) {
            return 0
        }
        val dragResistance = mData.mDragResistance
        if (dx > 0) {
            if (dx < dragResistance) { //先判断自身
                return 0
            }else { //再判断右边相邻的一个
                return if (position < mData.mTSViewAmount - 1) {
                    if (dx - dragResistance >= mRectViewInterval && dx - 3 * dragResistance <= mRectViewInterval) {
                        mRectViewInterval
                    }else if (dx - dragResistance < mRectViewInterval) {
                        dx - dragResistance
                    }else if (dx - 3 * dragResistance <= 2 * mRectViewInterval) {
                        dx - 3 * dragResistance
                    }else {
                        mRectViewInterval +
                                getCorrectDx(dx - 3 * dragResistance - mRectViewInterval + dragResistance, position + 1)
                    }
                }else {
                    dx - dragResistance
                }
            }
        }else {
            return -getCorrectDx(-dx, mData.mTSViewAmount - 1 - position)
        }
    }
}