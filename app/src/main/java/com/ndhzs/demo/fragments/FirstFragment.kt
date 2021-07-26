package com.ndhzs.demo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ndhzs.demo.R
import com.ndhzs.timeselectview.TimeSelectView
import com.ndhzs.timeselectview.bean.TSViewDayBean
import java.util.*

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/5/23
 *@description
 */
class FirstFragment : Fragment() {

    private lateinit var mRootView: View
    private lateinit var mTimeSelectView: TimeSelectView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mRootView = inflater.inflate(R.layout.fg_1, container, false)
        initView()
        return mRootView
    }

    private fun initView() {
        mTimeSelectView = mRootView.findViewById(R.id.fg1_timeView)
        val beans = ArrayList<TSViewDayBean>()
        val calendar = Calendar.getInstance()
        repeat(20) {
            beans.add(TSViewDayBean(calendar))
            calendar.add(Calendar.DATE, 1)
        }
        mTimeSelectView.initializeBean(beans, 0)
    }
}