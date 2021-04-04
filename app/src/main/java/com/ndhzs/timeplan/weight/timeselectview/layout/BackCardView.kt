package com.ndhzs.timeplan.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewUtil

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 * @description
 */
@SuppressLint("ViewConstructor")
class BackCardView(context: Context, data: TSViewInternalData) : LinearLayout(context) {

    init {
        orientation = HORIZONTAL
        val cardView = CardView(context)
        val lp = LayoutParams(data.mTimelineWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        lp.topMargin = topBottomMargin
        lp.bottomMargin = topBottomMargin
        lp.leftMargin = data.mTimelineInterval/2
        lp.rightMargin = data.mTimelineInterval/2
        cardView.radius = data.mCardCornerRadius
        addView(cardView, lp)
        repeat(data.mTSViewAmount - 1) {
            val cardView2 = CardView(context)
            cardView2.radius = data.mCardCornerRadius
            addView(cardView2, lp)
        }
    }

    companion object {
        /**
         * 为了显示CardView虚影的上下间隔值
         */
        const val topBottomMargin = 5
    }
}