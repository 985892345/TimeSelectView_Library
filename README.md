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
1. 该控件内部整合了ViewPager2，用于控制不同的天数。如果外置有ViewPager2，解决滑动冲突请看[滑动冲突](#滑动冲突)
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
| app:timeRangeArray      | 时间范围数组，格式为"2-18,12-4"，允许出现重复时间段。  **每个时间段的差值必须相等**
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

|         | Public methods |
| :------ | :--------------------------
| Unit    | [backCurrentTime](#backCurrentTime) ()  当前页面回到xml中设置的CurrentTime
| Unit    | [cancelAutoBackCurrent](#cancelAutoBackCurrent) ()  取消当前页面自动回到xml中设置的CurrentTime的延时
| Int     | [getCurrentItem](#getCurrentItem) ()  得到内部ViewPager2的当前item索引
| boolean | [getIsLongClick](#getIsLongClick) ()  得到当前页面的TimeSelectView是否处于长按状态
| Int     | [getTimeLineScrollY](#getTimeLineScrollY) ()  得到当前页面的时间轴的ScrollY
| Unit    | [initializeBean](#initializeBean) (dayBeans: ArrayList<TSViewDayBean>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false)  初始化数据
| Unit    | [notifyAllItemRefresh](#notifyAllItemRefresh) ()  通知所有item刷新
| Unit    | [notifyItemDataChanged](#notifyItemDataChanged) (position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false)  该方法用于在任务在外部被增加或删除时提醒内部重新读取数据
| Unit    | [notifyItemRefresh](#notifyItemRefresh) (position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false)  默认通知当前页面所有的任务刷新，可输入索引值定向刷新
| Unit    | [registerOnPageChangeCallback](#registerOnPageChangeCallback) (callback: OnPageChangeCallback)  设置内部ViewPager2的OnPageChangeCallback
| Unit    | [setCurrentItem](#setCurrentItem) (item: Int, smoothScroll: Boolean = true)  设置内部ViewPager2显示的页面位置
| Unit    | [setDragResistance](#setDragResistance) (resistance: Int = RectImgView.DEFAULT_DRAG_RESISTANCE)  设置在多个时间轴中拖动任务的阻力值
| Unit    | [setIsShowDiffTime](#setIsShowDiffTime) (boolean: Boolean)  最终的任务区域是否显示时间差
| Unit    | [setIsShowTopBottomTime](#setIsShowTopBottomTime) (boolean: Boolean)  最终的任务区域是否显示上下边界时间
| Unit    | [setOnDataListener](#setOnDataListener) (l: OnDataChangeListener)  对数据改变进行监听
| Unit    | [setOnTSVClickListener](#setOnTSVClickListener) (onClick: (taskBean: TSViewTaskBean) -> Unit)  点击当前任务的监听
| Unit    | [setOnTSVLongClickListener](#setOnTSVLongClickListener) (onStart: ((condition: TSViewLongClick) -> Unit), onEnd: ((condition: TSViewLongClick) -> Unit))  设置长按监听接口
| Unit    | [setOnScrollListener](#setOnScrollListener) (l: (scrollY: Int, itemPosition: Int) -> Unit)  当前页面时间轴的滑动回调
| Unit    | [setTimeInterval](#setTimeInterval) (timeInterval: Int)  时间间隔数
| Unit    | [timeLineScrollBy](#timeLineScrollBy) (dy: Int)  与ScrollBy相同
| Unit    | [timeLineScrollTo](#timeLineScrollTo) (scrollY: Int)  使时间轴瞬移，与ScrollTo相同
| Unit    | [timeLineSlowlyScrollTo](#timeLineSlowlyScrollTo) (scrollY: Int)  使时间轴较缓慢地滑动，并有回弹动画

### Public methods
#### backCurrentTime
#### cancelAutoBackCurrent
#### getCurrentItem
#### getIsLongClick
#### getTimeLineScrollY
#### initializeBean
#### notifyAllItemRefresh
#### notifyItemDataChanged
#### notifyItemRefresh
#### registerOnPageChangeCallback
#### setCurrentItem
#### setDragResistance
#### setIsShowDiffTime
#### setIsShowTopBottomTime
#### setOnDataListener
#### setOnTSVClickListener
#### setOnTSVLongClickListener
#### setOnScrollListener
#### setTimeInterval
#### timeLineScrollBy
#### timeLineScrollTo
#### timeLineSlowlyScrollTo
## 滑动冲突
