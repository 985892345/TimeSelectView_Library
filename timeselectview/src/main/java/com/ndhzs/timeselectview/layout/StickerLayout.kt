package com.ndhzs.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.ndhzs.timeselectview.layout.view.NowTimeLineView
import com.ndhzs.timeselectview.utils.TSViewAttrs
import com.ndhzs.timeselectview.viewinterface.IStickerLayout
import com.ndhzs.timeselectview.viewinterface.ITSViewTimeUtil

/**
 * 用来放置 RectImgView 和 NowTimeLineView，放这里的原因是为了把当前时间线显示在最顶层
 *
 * [ScrollLayout]之下，
 * [com.ndhzs.timeselectview.layout.view.RectImgView]、
 * [com.ndhzs.timeselectview.layout.view.NowTimeLineView]
 * 之上
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/4/27
 */
@SuppressLint("ViewConstructor")
internal class StickerLayout(
        context: Context,
        private val iStickerLayout: IStickerLayout,
        private val attrs: TSViewAttrs,
        private val time: ITSViewTimeUtil
) : FrameLayout(context) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine() {
        if (mNowTimeLineViewSize != attrs.mTSViewAmount) {
            post {
                for (i in mNowTimeLineViewSize until attrs.mTSViewAmount) { // 在多个时间轴时
                    val nowTimeLineView = NowTimeLineView(context, attrs, time, i)
                    val lp = LayoutParams(iStickerLayout.getChildLayoutWidth(), LayoutParams.WRAP_CONTENT)
                    lp.leftMargin = iStickerLayout.getChildLayoutToStickerLayoutDistance(i)
                    addView(nowTimeLineView, lp) // 由于这个只会在一个 TimeSelectView 中添加，所以就不写在 TSViewObjectsManger 中
                }
            }
        }
    }

    private var mNowTimeLineViewSize = 0

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        attachViewToParent(iStickerLayout.getRectImgView(), -1, lp)
    }
}