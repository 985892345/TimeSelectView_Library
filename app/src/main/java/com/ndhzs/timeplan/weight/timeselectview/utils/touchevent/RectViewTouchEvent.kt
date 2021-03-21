package com.ndhzs.timeplan.weight.timeselectview.utils.touchevent

import android.content.Context
import android.view.MotionEvent
import android.view.View

/**
 * @author 985892345
 * @date 2021/3/21
 * @description 处理RectView的触摸事件
 */
class RectViewTouchEvent(context: Context) : View(context) {
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return super.onTouchEvent(ev)
    }
}