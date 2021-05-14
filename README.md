# TimeSelectView  
项目TimePlan的TimeSelectView控件  

## 使用方法  

### Project build  
```
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
```

### Module build  
```
dependencies {
    implementation 'com.github.985892345:TimeSelectView_Library:0.9'
}
```

### 用前需知
1. 该控件已内置ViewPager2，用于控制不同的天数。如果外置有ViewPager2，解决滑动冲突请看[滑动冲突](#滑动冲突)
2. 必须使用 initializeBean() 方法才会显示控件
3. 如果在横屏中使用，请在theme中适配全面屏，务必将刘海区域进行填充，不然点击区域可能出现偏差

### 普通使用  
* 单个时间轴
```
<com.ndhzs.timeselectview.TimeSelectView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:amount="1"                
        app:centerTime="-1"           
        app:cardRadius="8dp"
        app:timeRangeArray="2-2"      
        app:timelineWidth="160dp"/>
```
* 多个时间轴
```
<com.ndhzs.timeselectview.TimeSelectView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:amount="2"                
        app:centerTime="9"
        app:cardRadius="8dp"
        app:timeRangeArray="2-16,12-2"  
        app:timelineWidth="160dp"
        app:timelineInterval="10dp"/>  
```

### Summary  

| XML attributes          | |
| :---------------------- | :---------------------------------------
| app:amount              | 时间轴个数（建议不超过3个，超过后经测试暂无bug） 
| app:cardRadius          | 时间轴背景的圆角度数                         
| app:centerTime          | 中心线时间（支持小数），输入-1为以中心值为中心线，-2为以目前时间值为中心线
| app:timeRangeArray      | 时间范围数组，格式为"2-18,12-4"，允许出现重复时间段。**每个时间段的差值必须相等**
| app:timelineWidth       | 时间轴宽度
| app:timelineInterval    | 相邻时间轴间隔
| app:timeInterval        | 时间间隔数，必须为60的因数，若不是，将以15为间隔数
| app:intervalLeft        | 时间轴左侧的时间文字间隔宽度
| app:intervalHeight      | 时间轴每小时间的间隔高度
| app:defaultTaskName     | 默认任务名称
| app:defaultBorderColor  | 默认任务边框颜色
| app:defaultInsideColor  | 默认任务内部颜色
| app:timeTextSize        | 时间轴左侧时间文字大小
| app:taskTextSize        | 任务名称文字大小
| app:isShowDiffTime      | 最终的任务区域是否显示时间差
| app:isShowTopBottomTime | 最终的任务区域是否显示上下边界时间

| Public methods | |
| :--------------| :--------------------------
| Unit           | [backCurrentTime](#backCurrentTime)()
| Unit           | [cancelAutoBackCurrent](#cancelAutoBackCurrent)()
| Int            | getCurrentItem()
| boolean        | getIsLongClick()
| Int            | getTimeLineScrollY()
| Unit           | initializeBean(dayBeans: ArrayList<TSViewDayBean>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false)
| Unit           | notifyAllItemRefresh()
| Unit           | notifyItemDataChanged(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false)
| Unit           | notifyItemRefresh(position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false)
| Unit           | registerOnPageChangeCallback(callback: OnPageChangeCallback)
| Unit           | setCurrentItem(item: Int, smoothScroll: Boolean = true)
| Unit           | setDragResistance(resistance: Int = RectImgView.DEFAULT_DRAG_RESISTANCE)
| Unit           | setIsShowDiffTime(boolean: Boolean)
| Unit           | setIsShowTopBottomTime(boolean: Boolean)
| Unit           | setOnDataListener(l: OnDataChangeListener)
| Unit           | setOnTSVClickListener(onClick: (taskBean: TSViewTaskBean) -> Unit)
| Unit           | setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit))
| Unit           | setOnScrollListener(l: (scrollY: Int, itemPosition: Int) -> Unit)
| Unit           | setTimeInterval(timeInterval: Int)
| Unit           | timeLineScrollBy(dy: Int)
| Unit           | timeLineScrollTo(scrollY: Int)
| Unit           | timeLineSlowlyScrollTo(scrollY: Int)

### Public methods
#### backCurrentTime
#### cancelAutoBackCurrent
## 滑动冲突
