package com.ndhzs.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.demo.adapter.VpAdapter
import com.ndhzs.demo.fragments.FirstFragment
import com.ndhzs.demo.fragments.SecondFragment
import com.ndhzs.demo.fragments.ThirdFragment

class ViewPager2Activity4 : AppCompatActivity() {

    private lateinit var mViewPager2: ViewPager2
    private lateinit var mFirstFg: FirstFragment
    private lateinit var mSecondFg: SecondFragment
    private lateinit var mThirdFg: ThirdFragment
    private val mFragments = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        initFragments()
        initViewpager2()
    }

    private fun initFragments() {
        mFirstFg = FirstFragment()
        mSecondFg = SecondFragment()
        mThirdFg = ThirdFragment()
        mFragments.add(mFirstFg)
        mFragments.add(mSecondFg)
        mFragments.add(mThirdFg)
    }

    private fun initViewpager2() {
        mViewPager2 = findViewById(R.id.viewPager2)
        mViewPager2.adapter = VpAdapter(this, mFragments)
//        mViewPager2.offscreenPageLimit = 1
    }
}