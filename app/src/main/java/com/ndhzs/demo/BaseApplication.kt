package com.ndhzs.demo

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

/**
 * **WARNING：** 要想使用记得在注册文件中修改 android:name="...... .BaseApplication"
 *
 *@author 985892345
 *@email 2767465918@qq.com
 *@data 2021/5/25
 */
class BaseApplication : Application(), Thread.UncaughtExceptionHandler {

    companion object {
        lateinit var appContext : Context

        fun isDebug(): Boolean {
            return appContext.applicationInfo != null && (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        Thread.setDefaultUncaughtExceptionHandler(this) // 在程序崩溃时打印报错信息至手机
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val sdf = SimpleDateFormat("yyyy-M-d, HH:mm:ss", Locale.CHINA)
        val time = sdf.format(Date())
        Log.d("123","(BaseApplication.kt:38)-->> $time")
        val file = appContext.getExternalFilesDir("error") // 得取该地址不用申请读写权限
        val file2 = File.createTempFile(time, ".txt", file)

        if (isDebug()) {
            Toast.makeText(appContext, "报错信息已打印在：${file2.absolutePath}", Toast.LENGTH_LONG).show()
            Toast.makeText(appContext, "文件位置已复制到手机中", Toast.LENGTH_SHORT).show()
            val copy = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            copy.setPrimaryClip(ClipData.newPlainText(null, file2.absolutePath))
        }

        val printWriter = PrintWriter(file2)
        printWriter.println(time)
        e.printStackTrace(printWriter)
        printWriter.close()
        exitProcess(1)
    }
}