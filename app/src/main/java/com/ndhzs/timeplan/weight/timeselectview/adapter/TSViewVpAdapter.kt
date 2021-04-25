package com.ndhzs.timeplan.weight.timeselectview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewDayBean
import com.ndhzs.timeplan.weight.timeselectview.layout.VpLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/24
 * @description
 */
class TSViewVpAdapter(dayBeans: ArrayList<TSViewDayBean>, data: TSViewInternalData, viewPager2: ViewPager2, firstDate: String) : RecyclerView.Adapter<TSViewVpAdapter.ViewHolder>() {

    fun showNowTimeLine(position: Int) {
        mViewPager2.post {
            if (mLastShowNowTimePosition != -1) {
                notifyItemChanged(mLastShowNowTimePosition)
            }
            if (mLastShowNowTimePosition != position) {
                mLastShowNowTimePosition = position
                notifyItemChanged(position)
            }
        }
    }

    fun notifyItemRefresh(position: Int, isBackToCurrentTime: Boolean) {
        mRefreshPosition = position
        mIsBackToCurrentTime = isBackToCurrentTime
        mOnScrollListener = null
        notifyItemChanged(position)
    }

    fun setOnScrollListener(l: (scrollY: Int) -> Unit) {
        mOnScrollListener = l
        mOnScrollListenerSave = l
    }

    private val mDayBeans = dayBeans
    private val mData = data
    private val mViewPager2 = viewPager2
    private val mFirstDate = firstDate

    private var mOnScrollListener: ((scrollY: Int) -> Unit)? = null
    private var mOnScrollListenerSave: ((scrollY: Int) -> Unit)? = null

    private var mLastShowNowTimePosition = -1

    private var mRefreshPosition = -1
    private var mIsBackToCurrentTime = false

    private val mPositionWithScrollY = HashMap<Int, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = VpLayout(parent.context, mData, mViewPager2)
        view.layoutParams = ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mVpLayout.initialize(mDayBeans[position], position, mFirstDate)
        holder.mVpLayout.setOnScrollListener { scrollY, vpPosition ->
            mPositionWithScrollY[vpPosition] = scrollY
            mOnScrollListener?.invoke(scrollY)
        }
        if (position == mRefreshPosition) {
            mRefreshPosition = -1
            if (position == mViewPager2.currentItem) {
                val scrollY = mPositionWithScrollY[position]
                val run = if (mIsBackToCurrentTime) {
                    Runnable {
                        holder.mVpLayout.backCurrentTime()
                    }
                }else {
                    Runnable {
                        holder.mVpLayout.moveTo(scrollY!!)
                    }
                }
                //此时是在调用notifyItemChanged后执行的，因为调用那个有可能会使整个View重绘，就只好使用一个延时在重绘后再移动
                //50毫秒的时间应该比较够
                //后面还有一个设置滑动监听延时了100毫秒，因为ScrollTo()等一系列方法可能是异线程调用，得等重绘并到了正确的位置后再设置监听
                mViewPager2.postDelayed(run, 50)
                mViewPager2.postDelayed({
                    mOnScrollListener = mOnScrollListenerSave
                }, 100)
            }else {
                holder.mVpLayout.backCurrentTime()
            }
        }
        if (mLastShowNowTimePosition == position) {
            holder.mVpLayout.showNowTimeLine()
        }
    }

    override fun getItemCount(): Int {
        return mDayBeans.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mVpLayout = itemView as VpLayout
    }
}