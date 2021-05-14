package com.ndhzs.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ndhzs.timeselectview.TimeSelectView
import com.ndhzs.timeselectview.bean.TSViewDayBean
import java.util.*
import kotlin.collections.ArrayList

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val beans = ArrayList<TSViewDayBean>()
        val calendar = Calendar.getInstance()
        repeat(20) {
            beans.add(TSViewDayBean(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        val timeView = findViewById<TimeSelectView>(R.id.time_view_2)
        timeView.initializeBean(beans, 0)

        findViewById<Button>(R.id.button_back_2).setOnClickListener {
            timeView.notifyItemRefresh(isBackToCurrentTime = true)
        }

        findViewById<Button>(R.id.button_2_1).setOnClickListener {
            timeView.setTimeInterval(1)
        }

        findViewById<Button>(R.id.button_2_5).setOnClickListener {
            timeView.setTimeInterval(5)
        }

        findViewById<Button>(R.id.button_2_10).setOnClickListener {
            timeView.setTimeInterval(10)
        }

        findViewById<Button>(R.id.button_2_15).setOnClickListener {
            timeView.setTimeInterval(15)
        }

        findViewById<Button>(R.id.button_2_20).setOnClickListener {
            timeView.setTimeInterval(20)
        }
    }
}