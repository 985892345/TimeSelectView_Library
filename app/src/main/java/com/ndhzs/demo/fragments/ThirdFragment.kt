package com.ndhzs.demo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ndhzs.demo.R

/**
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/5/23
 *@description
 */
class ThirdFragment : Fragment() {

    private lateinit var mRootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mRootView = inflater.inflate(R.layout.fg_3, container, false)
        return mRootView
    }
}