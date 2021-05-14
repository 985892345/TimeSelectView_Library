package com.ndhzs.timeselectview.weight.timeselectview.layout

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.ndhzs.timeselectview.weight.timeselectview.layout.view.NowTimeLineView
import com.ndhzs.timeselectview.weight.timeselectview.utils.TSViewInternalData
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.IStickerLayout
import com.ndhzs.timeselectview.weight.timeselectview.viewinterface.ITSViewTimeUtil

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/4/27
 *@description 用来放置RectImgView和NowTimeLineView，放这里的原因是为了把当前时间线显示在顶层
 * [ScrollLayout]之下，
 * [com.ndhzs.timeselectview.weight.timeselectview.layout.view.RectImgView]、
 * [com.ndhzs.timeselectview.weight.timeselectview.layout.view.NowTimeLineView]
 * 之上
 */
@SuppressLint("ViewConstructor")
class StickerLayout(context: Context, iStickerLayout: IStickerLayout, data: TSViewInternalData, time: ITSViewTimeUtil) : FrameLayout(context) {

    /**
     * 设置是否显示当前时间线
     */
    fun showNowTimeLine() {
        if (mNowTimeLineViews.size != mData.mTSViewAmount) {
            post {
                for (i in mNowTimeLineViews.size until mData.mTSViewAmount) {
                    val nowTimeLineView = NowTimeLineView(context, mData, mTime, i)
                    mNowTimeLineViews.add(nowTimeLineView)
                    val lp = LayoutParams(mIStickerLayout.getChildLayoutWidth(), LayoutParams.WRAP_CONTENT)
                    lp.leftMargin = mIStickerLayout.getChildLayoutToStickerLayoutDistance(i)
                    addView(nowTimeLineView, lp)//由于这个只会在一个TimeSelectView中添加，所以就不写在TSViewObjectsManger中
                }
            }
        }
    }
    private var mNowTimeLineViews = ArrayList<NowTimeLineView>()

    private val mData = data
    private val mTime = time
    private val mIStickerLayout = iStickerLayout

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        iStickerLayout.addRectImgView(lp, this)
    }
}