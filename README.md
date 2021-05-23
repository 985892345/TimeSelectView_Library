# TimeSelectView  
项目TimePlan的TimeSelectView控件  
* [参考格式](#参考格式)
* [XML属性](#xml属性)
* [Public方法](#public方法)
* [滑动冲突的解决](#滑动冲突的解决)
* [TSViewDayBean](#tsviewdaybean)
* [TSViewTaskBean](#tsviewtaskbean)
* [TSViewLongClick](#tsviewlongclick)

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
    implementation 'com.github.985892345:TimeSelectView_Library:1.0.2'
}
```

### 用前需知

1. 该控件内部整合了 ViewPager2，用于控制不同的天数。如果外置有 ViewPager2，解决滑动冲突请看 [滑动冲突的解决](#滑动冲突的解决)
2. 必须使用 initializeBean(...) 方法才会显示控件
3. 如果在横屏中使用，请在 theme 中适配全面屏，务必将刘海区域进行填充，不然点击区域可能出现偏差
4. 不建议嵌套在 ViewPager 中，建议使用 ViewPager2，暂未对 ViewPager 做滑动冲突处理

### 参考格式

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



## 概要

### XML属性
|**attr name**            | **usage**|
|:------------------------| ---------|
| app:amount              | 时间轴个数（建议不超过3个，超过后经测试暂无bug）|
| app:cardRadius          | 时间轴背景的圆角度数|
| app:centerTime          | 中心线时间（支持小数），输入-1以中心值为中心线，-2以目前时间值为中心线|
| app:timeRangeArray      | 时间范围数组，格式为"2-18,12-4"，允许出现重复时间段。  **每个时间段的差值必须相等**|
| app:timelineWidth       | 时间轴宽度|
| app:timelineInterval    | 相邻时间轴间隔|
| app:timeInterval        | 时间间隔数，必须为60的因数，若不是，将以15为间隔数|
| app:intervalLeft        | 时间轴左侧的时间文字间隔宽度|
| app:intervalHeight      | 时间轴每小时间的间隔高度|
| app:defaultTaskName     | 默认任务名称|
| app:defaultBorderColor  | 默认任务边框颜色|
| app:defaultInsideColor  | 默认任务内部颜色|
| app:timeTextSize        | 时间轴左侧时间文字大小|
| app:taskTextSize        | 任务名称文字大小|
| app:isShowDiffTime      | 最终的任务区域是否显示时间差|
| app:isShowTopBottomTime | 最终的任务区域是否显示上下边界时间|

### Public方法

| **return** | **method name**|
|:----------:|:---------------|
|    Unit    | [backCurrentTime](#backcurrenttime) ()|
|            | 当前页面回到 xml 中设置的 CurrentTime|
|    Unit    | [cancelAutoBackCurrent](#cancelautobackcurrent) ()|
|            | 取消当前页面自动回到 xml 中设置的 CurrentTime 的延时|
|    Int     | [getCurrentItem](#getcurrentitem) ()|
|            | 得到内部 ViewPager2 的当前 item 索引|
|  boolean   | [getIsLongClick](#getislongclick) ()|
|            | 得到当前页面的 TimeSelectView 是否处于长按状态|
|    Int     | [getTimeLineScrollY](#gettimelinescrolly) ()|
|            | 得到当前页面的时间轴的 ScrollY|
|    Unit    | [initializeBean](#initializebean) ( dayBeans: ArrayList<[TSViewDayBean](#tsviewdaybean)>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false )|
|            | 初始化数据|
|  boolean   | [isDealWithTouchEvent](#isdealwithtouchevent) ( ev: MotionEvent )|
|            | 解决与 HorizontalScrollView 的滑动冲突|
|  boolean   | [isDealWithTouchEvent](#isdealwithtouchevent) ( ev: MotionEvent, myItemPosition: Int )|
|            | 解决与横向的 ViewPager2 的滑动冲突|
|    Unit    | [notifyAllItemRefresh](#notifyallitemrefresh) ()|
|            | 通知所有 item 刷新|
|    Unit    | [notifyItemDataChanged](#notifyitemdatachanged) ( position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false )|
|            | 该方法用于任务在外面被增加或删除时提醒控件重新读取数据|
|    Unit    | [notifyItemRefresh](#notifyitemrefresh) ( position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false )|
|            | 默认通知当前页面所有的任务刷新，可输入索引值定向刷新。但任务在外面增多或减少，该刷新不会是想要的效果，请使用 [notifyItemDataChanged](#notifyitemdatachanged)|
|    Unit    | [registerOnPageChangeCallback](#registeronpagechangecallback) ( callback: OnPageChangeCallback )|
|            | 设置内部 ViewPager2 的 OnPageChangeCallback|
|    Unit    | [setCurrentItem](#setcurrentitem) ( item: Int, smoothScroll: Boolean = true )|
|            | 设置内部 ViewPager2 显示的页面位置|
|    Unit    | [setDragResistance](#setdragresistance) ( resistance: Int = DEFAULT_DRAG_RESISTANCE )|
|            | 设置相邻时间轴中拖动任务的阻力值|
|    Unit    | [setIsShowDiffTime](#setisshowdifftime) ( boolean: Boolean )|
|            | 最终的任务区域是否显示时间差|
|    Unit    | [setIsShowTopBottomTime](#setisshowtopbottomtime) ( boolean: Boolean )|
|            | 最终的任务区域是否显示上下边界时间|
|    Unit    | [setOnDataListener](#setondatalistener) ( l: OnDataChangeListener )|
|            | 对数据改变进行监听|
|    Unit    | [setOnTSVClickListener](#setontsvclicklistener) ( onClick: ( taskBean: [TSViewTaskBean](#tsviewtaskbean) ) -> Unit )|
|            | 点击当前任务的监听|
|    Unit    | [setOnTSVLongClickListener](#setontsvlongclicklistener) ( onStart: (( condition: [TSViewLongClick](#tsviewlongclick) ) -> Unit ), onEnd: (( condition: [TSViewLongClick](#tsviewlongclick) ) -> Unit ))|
|            | 设置长按监听接口|
|    Unit    | [setOnScrollListener](#setonscrolllistener) ( l: ( scrollY: Int, itemPosition: Int ) -> Unit )|
|            | 当前页面时间轴的滑动回调|
|    Unit    | [setTimeInterval](#settimeinterval) ( timeInterval: Int )|
|            | 设置时间间隔数|
|    Unit    | [timeLineScrollBy](#timelinescrollby) ( dy: Int )|
|            | 与ScrollBy相同|
|    Unit    | [timeLineScrollTo](#timelinescrollto) ( scrollY: Int )|
|            | 使时间轴瞬移，与ScrollTo相同|
|    Unit    | [timeLineSlowlyScrollTo](#timelineslowlyscrollto) ( scrollY: Int )|
|            | 使时间轴较缓慢地滑动，并有回弹动画|

backCurrentTime
---
````kotlin
fun backCurrentTime()
````
当前页面回到 xml 中设置的 app:centerTime 时间，  
若为-1以中心值为中心线，-2为以目前时间值为中心线

cancelAutoBackCurrent
---
````kotlin
fun cancelAutoBackCurrent()
````
取消当前页面自动回到xml中设置的 CurrentTime 的延时。  
延时是在每次手指离开时间轴就会开启

getCurrentItem
---
````kotlin
fun getCurrentItem(): Int
````
得到内部 ViewPager2 的当前item索引

getIsLongClick
---
````kotlin
fun getIsLongClick(): Boolean
````
得到当前页面的 TimeSelectView 是否处于长按状态。  
若你想得到软件中所有的 TimeSelectView 是否存在处于长按状态的，可以使用 [TSViewLongClick](#tsviewlongclick)#sHasLongClick

getTimeLineScrollY
---
````kotlin
fun getTimeLineScrollY(): Int
````
得到当前页面的时间轴的 ScrollY

initializeBean
---
````kotlin
fun initializeBean(dayBeans: List<TSViewDayBean>, 
                   showNowTimeLinePosition: Int = -1, 
                   currentItem: Int = 0, 
                   smoothScroll: Boolean = false)
````
用于初始化数据  
**注意：**必须调用，不调用将不会显示 View

|     **Parameters**     | |
| :--------------------- | :-----------|
| dayBeans               | List<TSViewDayBean>: 以 [TSViewDayBean](#tsviewdaybean) 为数据的数组 |
| showNowTimeLinePosition| Int = -1: 显示时间线的位置，从0开始，传入负数将不会显示 |
| currentItem            | Int = 0: 内部 ViewPager2 的 item 位置，默认值为0 |
| smoothScroll           | Boolean = false: 设置上方的 currentItem 后，在初始化时是否显示移动动画，默认值为 false |

isDealWithTouchEvent
---
````kotlin
fun isDealWithTouchEvent(ev: MotionEvent): Boolean
````
返回 TimeSelectView 是否要处理触摸事件，只能用于父 View 为 HorizontalScrollView 中，
解决滑动冲突问题，具体如何解决请看 [滑动冲突的解决](#滑动冲突的解决)  

````kotlin
fun isDealWithTouchEvent(ev: MotionEvent, myItemPosition: Int): Boolean
````
返回 TimeSelectView 是否要处理触摸事件，只能用于父 View 为横向 ViewPager2 中，
解决滑动冲突问题，具体如何解决请看 [滑动冲突的解决](#滑动冲突的解决)  
**注意：** 该方法用于横向 ViewPager2，不是用于 ViewPager 中

notifyAllItemRefresh
---
````kotlin
fun notifyAllItemRefresh()
````
通知 ViewPager2 的所有 item 刷新

notifyItemDataChanged
---
````kotlin
fun notifyItemDataChanged(position: Int = mViewPager2.currentItem, 
                          isBackToCurrentTime: Boolean = false)
````
该方法用于任务在外面被增加或删除时提醒控件重新读取数据  
**注意：** 控件内部数据的增加或删除也会引起外面传进来的数组中数据的改变

|      **Parameters**      | |
| :----------------------- | -----------|
| position                 | Int = mViewPager2.currentItem: 通知内部 ViewPager2 的页面位置刷新，默认为当前显示界面|
| isBackToCurrentTime      | Boolean = false: 刷新是否回到 xml 中设置的 app:centerTime 时间|

notifyItemRefresh
---
````kotlin
fun notifyItemRefresh(position: Int = mViewPager2.currentItem, 
                      isBackToCurrentTime: Boolean = false)
````
默认通知当前页面所有的任务刷新，可输入索引值定向刷新
**注意：** 在任务增加或被删掉时调用此方法并不会有刷新作用，请调用 [notifyItemDataChanged](#notifyitemdatachanged)

|      **Parameters**      | |
| :----------------------- | ------------|
| position                 | Int = mViewPager2.currentItem: 通知内部 ViewPager2 的页面位置刷新，默认为当前显示界面|
| isBackToCurrentTime      | Boolean = false: 刷新是否回到 xml 中设置的 app:centerTime 时间|

registerOnPageChangeCallback
---
````kotlin
fun registerOnPageChangeCallback(callback: OnPageChangeCallback)
````
设置内部 ViewPager2 的 OnPageChangeCallback

setCurrentItem
---
````kotlin
fun setCurrentItem(item: Int, 
                   smoothScroll: Boolean = true)
````
设置内部 ViewPager2 显示的页面位置

setDragResistance
---
````kotlin
fun setDragResistance(resistance: Int = DEFAULT_DRAG_RESISTANCE)
````
设置相邻时间轴中拖动任务的阻力值

|      **Parameters**      | |
| :----------------------- | ------------|
| resistance               | Int = DEFAULT_DRAG_RESISTANCE: 设置相邻|

setIsShowDiffTime
---
````kotlin
fun setIsShowDiffTime(boolean: Boolean)
````
最终的任务区域是否显示时间差

setIsShowTopBottomTime
---
````kotlin
fun setIsShowTopBottomTime(boolean: Boolean)
````
最终的任务区域是否显示上下边界时间

setOnDataListener
---
````kotlin
fun setOnDataListener(l: OnDataChangeListener)
````
对数据改变进行监听  
**注意：** 任务移至删除区域被删除或长按添加新任务时会引起外面传进来的数组中数据的改变，
所以在数据改变后的回调中不需删掉或增加数据

setOnTSVClickListener
---
````kotlin
fun setOnTSVClickListener(onClick: (taskBean: TSViewTaskBean) -> Unit)
````
点击当前任务的监听，会返回当前点击任务的数据类  
**注意：** 对 [TSViewTaskBean](#tsviewtaskbean) 修改数据后并不会自己刷新，请手动调用 [notifyItemRefresh](#notifyitemrefresh) 进行刷新

|      **Parameters**      | |
| :----------------------- | ------------|
| onClick                  | (taskBean: TSViewTaskBean) -> Unit: 点击任务后的回调，返回点击任务的 [TSViewTaskBean](#tsviewtaskbean)|

setOnTSVLongClickListener
---
````kotlin
fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), 
                              onEnd: ((condition: TSViewLongClick) -> Unit))
````
设置长按监听接口

|      **Parameters**      | |
| :----------------------- | ------------|
| onStart                  | (condition: TSViewLongClick) -> Unit: 长按开始的回调，返回长按的情况，详细请看 [TSViewLongClick](#tsviewlongclick)|
| onEnd                    | (condition: TSViewLongClick) -> Unit: 长按结束的回调，返回长按的情况，详细请看 [TSViewLongClick](#tsviewlongclick)|

setOnScrollListener
---
````kotlin
fun setOnScrollListener(l: (scrollY: Int, itemPosition: Int) -> Unit)
````
当前页面时间轴的滑动回调  
不是 ViewPager2 的滑动回调,若你想监听 ViewPager2 的滑动，请使用 [registerOnPageChangeCallback](#registeronpagechangecallback)

|      **Parameters**      | |
| :----------------------- | ------------|
| scrollY                  | Int: 返回控件的 scrollY |
| itemPosition             | Int: 内部 ViewPager2 的当前 item 位置 |

setTimeInterval
---
````kotlin
fun setTimeInterval(timeInterval: Int)
````
设置时间间隔数  
**注意：** 必须为60的因数，若不是，将以15为间隔数

timeLineScrollBy
---
````kotlin
fun timeLineScrollBy(dy: Int)
````
与 ScrollBy 相同  

|      **Parameters**      | |
| :----------------------- | ------------|
| dy                       | Int: dy > 0，向上瞬移；dy < 0，向下瞬移|

timeLineScrollTo
---
````kotlin
fun timeLineScrollTo(scrollY: Int)
````
使时间轴瞬移，与 ScrollTo 相同

timeLineSlowlyScrollTo
---
````kotlin
fun timeLineSlowlyScrollTo(scrollY: Int)
````
使时间轴较缓慢地滑动，并有回弹动画


## 滑动冲突的解决

如果在本控件处于 ViewPager2 或 HorizontalScrollView 以内，请使用下面设置。（其他滑动控件可能不能通用）

* 处于 HorizontalScrollView 中  
````kotlin
//重写这个滑动控件的 onInterceptTouchEvent() 方法
private lateinit var mTimeSelectView: TimeselectView
override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    if (mTimeSelectView.isDealWithTouchEvent(ev)) {
        return false
    }
    return super.onInterceptTouchEvent(ev)
}
````

* 处于 ViewPager2 中   
因为 ViewPager2 是 final 无法被重写，所以修改 ViewPager2 的父 View#onInterceptTouchEvent() 方法
或父视图为 Activity 时的 dispatchTouchEvent()。（暂不支持 ViewPager，建议使用 ViewPager2）  
具体实现也可看 demo 中的样例代码
````kotlin
// 重写 ViewPager2 的父 View#onInterceptTouchEvent()方法
// 或父视图为 Activity 时的 dispatchTouchEvent()，写法一样
private lateinit var mTimeSelectView: TimeselectView
private lateinit var mViewPager2: ViewPager2
override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // 1 表示 TimeSelectView 在 ViewPager2 中的页面 position 为 1
    mViewPager2.isUserInputEnabled = !mTimeSelectView.isDealWithTouchEvent(ev, 1)
    return super.dispatchTouchEvent(ev)
}
````


# TSViewDayBean
用于存储每天的所有任务

| **Variables** | |
| :------------ | ----------|
| val date      | String: 该 TSViewDayBean 所代表的日期，**注意：** 日期格式必须为"yyyy-M-d"，如：2021-5-14|
| val tSViewTaskBeans | MutableList<TSViewTaskBean>: 该 TSViewDayBean 所存储的当天所有任务 |

# TSViewTaskBean
用于存储当个任务

| **Variables** | |
| :-----------: | ----------|
| date          | String: 任务日期，**注意：** 日期格式必须为"yyyy-M-d"，如：2021-5-14|
| name          | String: 任务名称|
| startTime     | String: 任务开始时间|
| endTime       | String: 任务结束时间|
| diffTime      | String: 任务时间差|
| borderColor   | Int: 任务边框颜色|
| insideColor   | Int: 任务内部颜色|
| any1          | Any? = null: 用于万能转换|
| any2          | Any? = null: 用于万能转换|
| any3          | Any? = null: 用于万能转换|

# TSViewLongClick
用来判断长按状态的枚举类

|   **Variables**   | |
| :---------------- | ----------|
| sHasLongClick     | Boolean: 这个可得到软件中全部的 TimeSelectView 是否存在处于长按状态|

| **enum** | |
| :---------------- | -----------|
| NULL              | 不处于长按状态|
| TOP               | 长按的任务顶部|
| TOP_SLIDE_UP      | 长按的任务顶部且处于向上滑的状态|
| TOP_SLIDE_DOWN    | 长按的任务顶部且处于向下滑的状态|
| BOTTOM            | 长按的任务底部|
| BOTTOM_SLIDE_UP   | 按的任务底部且处于向上滑的状态|
| BOTTOM_SLIDE_DOWN | 长按的任务底部且处于向下滑的状态|
| INSIDE            | 长按的任务内部|
| INSIDE_SLIDE_UP   | 长按的任务内部且处于向上滑的状态|
| INSIDE_SLIDE_DOWN | 长按的任务内部且处于向下滑的状态|
| EMPTY_AREA        | 长按的空白区域|
| EMPTY_SLIDE_UP    | 长按的空白区域且处于向上滑的状态|
| EMPTY_SLIDE_DOWN  | 长按的空白区域且处于向下滑的状态|

---
# 更新日志
* 1.0.2  
  增加 [isDealWithTouchEvent](#isdealwithtouchevent) 方法，可快速处理滑动冲突  
  demo 增加 ViewPager2 情况下的滑动冲突解决样例代码

* 1.0.1  
  修改 [initializeBean](#initializebean) 的形参为 List  
  修改 [TSViewDayBean](#tsviewdaybean) 的 tSViewTaskBeans 为 MutableList  
  修改 [TSViewDayBean](#tsviewdaybean) 的构造方法  

* 1.0.0  
  第一个稳定版  
  2021-5-14号发布  