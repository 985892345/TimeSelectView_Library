# TimeSelectView
项目TimePlan的TimeSelectView控件
##使用方法
###Project build
````
buildscript {
    repositories {
        ......
        maven { url 'https://jitpack.io' }
    }
}

allprojects {
    repositories {
        ......
        maven { url 'https://jitpack.io' }
    }
}
````
###Module build
````
dependencies {
    implementation 'com.github.985892345:TimeSelectView_Library:0.9'
}
````
###普通使用
* 单个时间轴
````
<com.ndhzs.timeselectview.TimeSelectView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:amount="1"                
        app:centerTime="-1"           
        app:cardRadius="8dp"
        app:timeRangeArray="2-2"      
        app:timelineWidth="160dp"/>
````
* 多个时间轴
````
<com.ndhzs.timeselectview.TimeSelectView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:amount="2"                
        app:centerTime="8"
        app:cardRadius="8dp"
        app:timeRangeArray="2-16,12-2"  
        app:timelineWidth="160dp"
        app:timelineInterval="10dp"/>  
````
###自定义属性
* app:amount
时间轴个数(建议不超过3个，超过后经测试暂无bug)  
**注意：** 如果在横屏中，请在theme中适配全面屏，务必将刘海区域进行填充，不然点击区域可能出现偏差
* app:cardRadius
时间轴背景的圆角度数
* app:centerTime
1. 以输入时间线的为中心线，时间只能在第一个时间轴的范围内(支持小数)
2. 输入-1为以中心值为中心线
3. 输入-2为以目前时间值为中心线
* app:timeRangeArray
时间范围数组，格式为"2-18,12-4"(英文逗号，且逗号后没有空格)
**注意：** 
1. 时间都必须大于0且小于24
2. 每个时间段的差值必须相等
3. 允许出现重复时间段
* app:timelineWidth
时间轴宽度
* app:timelineInterval
相邻时间轴间隔
* app:timeInterval
时间默认间隔数，必须为60的因数，若不是，将以15为间隔数
* app:intervalLeft
时间轴左侧的时间文字间隔宽度
* app:intervalHeight
时间轴每小时间的间隔高度
* app:defaultBorderColor
默认任务边框颜色
* app:defaultInsideColor
默认任务内部颜色
* app:defaultTaskName
默认任务名称
* app:timeTextSize
时间轴左侧时间文字大小(任务文字大小随之改变)
* app:taskTextSize
任务名称文字大小
* app:isShowDiffTime
最终的任务区域是否显示时间差
* app:isShowTopBottomTime
最终的任务区域是否显示上下边界时间
###Public fun
````kotlin
fun initializeBean(dayBeans: ArrayList<TSViewDayBean>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false)
````
**注意：** 该方法必须调用  
````kotlin
fun setOnScrollListener(l: (scrollY: Int, itemPosition: Int) -> Unit)
````
````kotlin
fun setTimeInterval(timeInterval: Int)
````
````kotlin
fun setIsShowDiffTime(boolean: Boolean)
````
````kotlin
fun setIsShowTopBottomTime(boolean: Boolean)
````
````kotlin
fun setOnTSVClickListener(onClick: (taskBean: TSViewTaskBean) -> Unit)
````
````kotlin
fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick)
````
````kotlin
fun setOnDataListener(l: OnDataChangeListener)
````
````kotlin
fun setIsShowTopBottomTime(boolean: Boolean)
````
````kotlin
fun getIsLongClick(): Boolean
````
````kotlin
fun getTimeLineScrollY(): Int
````
````kotlin
fun notifyItemRefresh(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false)
````
````kotlin
fun notifyItemDataChanged(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false)
````
````kotlin
fun notifyAllItemRefresh()
````
````kotlin
fun registerOnPageChangeCallback(callback: OnPageChangeCallback)
````
````kotlin
fun timeLineSlowlyScrollTo(scrollY: Int)
````
````kotlin
fun backCurrentTime()
````
````kotlin
fun cancelAutoBackCurrent()
````
````kotlin
fun setCurrentItem(item: Int, smoothScroll: Boolean = true)
````
````kotlin
fun setDragResistance(resistance: Int = RectImgView.DEFAULT_DRAG_RESISTANCE)
````
````kotlin
fun getCurrentItem(): Int
````