package com.ndhzs.demo.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/5/23
 *@description
 */
class VpAdapter(fragmentActivity: FragmentActivity, fragments: List<Fragment>) : FragmentStateAdapter(fragmentActivity) {

    private val mFragments = fragments

    override fun getItemCount(): Int {
        return mFragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragments[position]
    }
}