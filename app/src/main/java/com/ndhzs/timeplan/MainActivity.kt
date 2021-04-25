package com.ndhzs.timeplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ndhzs.timeplan.weight.timeselectview.TimeSelectView
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewDayBean
import com.ndhzs.timeplan.weight.timeselectview.bean.TSViewTaskBean
import java.util.*
import kotlin.collections.ArrayList

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

        val beans = ArrayList<TSViewDayBean>()
        val calendar = Calendar.getInstance()
        repeat(20) {
            beans.add(TSViewDayBean(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        mTimeView = findViewById(R.id.time_view)
        mTimeView.initializeBean(beans)
        mTimeView.showNowTimeLine(0)

        findViewById<Button>(R.id.button_clear).setOnClickListener {
            mTimeView.notifyItemRefresh(isBackToCurrentTime = true)
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