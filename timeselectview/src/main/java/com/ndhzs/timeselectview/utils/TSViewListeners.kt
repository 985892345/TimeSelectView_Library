package com.ndhzs.timeselectview.utils

import com.ndhzs.timeselectview.TimeSelectView
import com.ndhzs.timeselectview.bean.TSViewTaskBean

/**
 * .....
 * @author 985892345
 * @email 2767465918@qq.com
 * @data 2021/6/10
 */
class TSViewListeners {
    var mOnDataChangeListener: TimeSelectView.OnDataChangeListener? = null //数据改变监听
    var mOnClickListener: ((taskBean: TSViewTaskBean) -> Unit)? = null
    var mOnLongClickStartListener: ((condition: TSViewLongClick) -> Unit)? = null
    var mOnLongClickEndListener: ((condition: TSViewLongClick) -> Unit)? = null
    var mOnScrollListener: ((scrollY: Int, vpPosition: Int) -> Unit)? = null
}