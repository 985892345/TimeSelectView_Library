package com.ndhzs.timeplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ndhzs.timeplan.weight.timeselectview.TimeSelectView
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewBean

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mDebugButton = findViewById<Button>(R.id.button_debug)
        val mTimeView = findViewById<TimeSelectView>(R.id.time_view)
        mDebugButton.setOnClickListener {
            val beans = ArrayList<TSViewBean>()
            mTimeView.initializeBean(beans)
        }
    }
}