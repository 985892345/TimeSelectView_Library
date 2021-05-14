package com.ndhzs.timeselectview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ndhzs.timeselectview.weight.timeselectview.TimeSelectView
import com.ndhzs.timeselectview.weight.timeselectview.bean.TSViewDayBean
import java.util.*
import kotlin.collections.ArrayList

class MainActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        val beans = ArrayList<TSViewDayBean>()
        val calendar = Calendar.getInstance()
        repeat(20) {
            beans.add(TSViewDayBean(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        val timeView = findViewById<TimeSelectView>(R.id.time_view_1)
        timeView.initializeBean(beans, 0)

        findViewById<Button>(R.id.button_back_1).setOnClickListener {
            timeView.notifyItemRefresh(isBackToCurrentTime = true)
        }

        findViewById<Button>(R.id.button_1_1).setOnClickListener {
            timeView.setTimeInterval(1)
        }

        findViewById<Button>(R.id.button_1_5).setOnClickListener {
            timeView.setTimeInterval(5)
        }

        findViewById<Button>(R.id.button_1_10).setOnClickListener {
            timeView.setTimeInterval(10)
        }

        findViewById<Button>(R.id.button_1_15).setOnClickListener {
            timeView.setTimeInterval(15)
        }

        findViewById<Button>(R.id.button_1_20).setOnClickListener {
            timeView.setTimeInterval(20)
        }
    }
}