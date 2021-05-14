package com.ndhzs.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ndhzs.timeselectview.TimeSelectView
import com.ndhzs.timeselectview.bean.TSViewDayBean
import java.util.*
import kotlin.collections.ArrayList

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_main3)

        val beans = ArrayList<TSViewDayBean>()
        val calendar = Calendar.getInstance()
        repeat(20) {
            beans.add(TSViewDayBean(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        val timeView = findViewById<TimeSelectView>(R.id.time_view_3)
        timeView.initializeBean(beans, 0)
        timeView.setDragResistance(0)
    }
}