package com.ndhzs.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.ndhzs.timeselectview.utils.TSViewAttrs

/**
 * [com.ndhzs.timeselectview.TimeSelectView]之下，里面是CardView
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/4
 */
@SuppressLint("ViewConstructor")
internal class BackCardView(
        context: Context,
        attrs: TSViewAttrs,
) : LinearLayout(context) {

    companion object {
        /**
         * 为了显示CardView虚影的上下间隔值
         */
        const val TOP_BOTTOM_MARGIN = 20

        /**
         * 为了显示CardView虚影的左右间隔值
         */
        const val LEFT_RIGHT_MARGIN = 3
    }

    init {
        orientation = HORIZONTAL
        val cardView = CardView(context)
        val lp = LayoutParams(attrs.mTimelineWidth, LayoutParams.MATCH_PARENT)
        lp.topMargin = TOP_BOTTOM_MARGIN
        lp.bottomMargin = TOP_BOTTOM_MARGIN
        lp.leftMargin = LEFT_RIGHT_MARGIN
        lp.rightMargin = LEFT_RIGHT_MARGIN
        cardView.radius = attrs.mCardCornerRadius
        attachViewToParent(cardView, -1, lp)
        val lp2 = LayoutParams(lp)
        lp2.leftMargin += attrs.mTimelineInterval - LEFT_RIGHT_MARGIN * 2
        repeat(attrs.mTSViewAmount - 1) {
            val cardView2 = CardView(context)
            cardView2.radius = attrs.mCardCornerRadius
            attachViewToParent(cardView2, -1, lp2)
        }
    }
}