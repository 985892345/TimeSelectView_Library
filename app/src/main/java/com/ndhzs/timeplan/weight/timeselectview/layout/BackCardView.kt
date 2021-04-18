package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description [com.ndhzs.timeplan.weight.timeselectview.TimeSelectView]之下，里面是CardView
 */
@SuppressLint("ViewConstructor")
class BackCardView(context: Context, private val data: TSViewInternalData) : LinearLayout(context) {

    /**
     * 得到TimeSelectView的最小宽度，用于在wrap_content中。
     *
     * 其中先“ / 2 * 2 ”是lp.leftMargin和lp.rightMargin是Int类型，会有精度损失
     */
    fun getMinWidth(): Int = data.mAllTimelineWidth + data.mTimelineInterval / 2 * 2

    /**
     * 得到TimeSElectView的外部最小高度
     */
    fun getMinOuterHeight(): Int = 500

    companion object {
        /**
         * 为了显示CardView虚影的上下间隔值
         */
        const val topBottomMargin = 10

        const val leftRightMargin = 3
    }

    init {
        orientation = HORIZONTAL
        val cardView = CardView(context)
        val lp = LayoutParams(data.mTimelineWidth, LayoutParams.MATCH_PARENT)
        lp.topMargin = topBottomMargin
        lp.bottomMargin = topBottomMargin
        lp.leftMargin = leftRightMargin
        lp.rightMargin = leftRightMargin
        cardView.radius = data.mCardCornerRadius
        addView(cardView, lp)
        val lp2 = LayoutParams(lp)
        lp2.leftMargin += data.mTimelineInterval
        repeat(data.mTSViewAmount - 1) {
            val cardView2 = CardView(context)
            cardView2.radius = data.mCardCornerRadius
            addView(cardView2, lp2)
        }
    }


}