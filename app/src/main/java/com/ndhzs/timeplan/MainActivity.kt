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

    private lateinit var mTimeView: TimeSelectView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTimeView = findViewById(R.id.time_view)

        findViewById<Button>(R.id.button_clear).setOnClickListener {
            val beans = ArrayList<TSViewBean>()
            mTimeView.initializeBean(beans)
        }

        findViewById<Button>(R.id.button_1).setOnClickListener {
            mTimeView.setTimeInterval(1)
        }

        findViewById<Button>(R.id.button_5).setOnClickListener {
            mTimeView.setTimeInterval(5)
        }

        findViewById<Button>(R.id.button_10).setOnClickListener {
            mTimeView.setTimeInterval(10)
        }

        findViewById<Button>(R.id.button_15).setOnClickListener {
            mTimeView.setTimeInterval(15)
        }

        findViewById<Button>(R.id.button_20).setOnClickListener {
            mTimeView.setTimeInterval(20)
        }
    }
}