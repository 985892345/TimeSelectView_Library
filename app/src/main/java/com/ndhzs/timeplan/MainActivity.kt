package com.ndhzs.timeplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ndhzs.timeplan.weight.TimeSelectView

/**
 * @author 985892345
 * @date 2021/3/20
 * @description
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val a =TimeSelectView(this)
    }
}