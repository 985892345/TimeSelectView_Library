package com.ndhzs.timeplan.weight.timeselectview.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean
import com.ndhzs.timeplan.weight.timeselectview.layout.VpLayout
import com.ndhzs.timeplan.weight.timeselectview.utils.TSViewInternalData

/**
 * @author 985892345
 * @email 2767465918@qq.com
 * @date 2021/4/24
 * @description
 */
class TSViewVpAdapter(beans: ArrayList<ArrayList<TSViewBean>>, data: TSViewInternalData, viewPager2: ViewPager2) : RecyclerView.Adapter<TSViewVpAdapter.ViewHolder>() {

    fun showNowTimeLine(position: Int) {
        if (mLastShowNowTimePosition != -1) {
            notifyItemChanged(mLastShowNowTimePosition)
        }
        if (mLastShowNowTimePosition != position) {
            mLastShowNowTimePosition = position
            notifyItemChanged(position)
        }
    }

    private val mBeans = beans
    private val mData = data
    private val mViewPager2 = viewPager2
    private var mLastShowNowTimePosition = -1
    private val mHashSet = HashSet<View>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = VpLayout(parent.context, mData, mViewPager2)
        view.layoutParams = ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mVpLayout.initialize(mBeans[position], position)
        if (mLastShowNowTimePosition == position) {
            holder.mVpLayout.showNowTimeLine()
        }
    }

    override fun getItemCount(): Int {
        return mBeans.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mVpLayout = itemView as VpLayout
        init {
            mHashSet.add(itemView)
            Log.d("123", "(ViewHolder:43)-->>  ${mHashSet.size}");
        }
    }
}