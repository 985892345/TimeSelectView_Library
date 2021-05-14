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

## 概要
### XML attributes
|       attr name         |                usage               |
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

### Public methods
| return  | method name |
| :-----: | :--------------------------
|  Unit   | [backCurrentTime](#backCurrentTime) ()  
|         | 当前页面回到xml中设置的CurrentTime
|  Unit   | [cancelAutoBackCurrent](#cancelAutoBackCurrent) ()  
|         | 取消当前页面自动回到xml中设置的CurrentTime的延时
|   Int   | [getCurrentItem](#getCurrentItem) ()  
|         | 得到内部ViewPager2的当前item索引
| boolean | [getIsLongClick](#getIsLongClick) ()  
|         | 得到当前页面的TimeSelectView是否处于长按状态
|   Int   | [getTimeLineScrollY](#getTimeLineScrollY) ()  
|         | 得到当前页面的时间轴的ScrollY
|  Unit   | [initializeBean](#initializeBean) ( dayBeans: ArrayList<[TSViewDayBean](#TSViewDayBean)>, showNowTimeLinePosition: Int = -1, currentItem: Int = 0, smoothScroll: Boolean = false )  
|         | 初始化数据
|  Unit   | [notifyAllItemRefresh](#notifyAllItemRefresh) ()  
|         | 通知所有item刷新
|  Unit   | [notifyItemDataChanged](#notifyItemDataChanged) ( position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false ) 
|         | 该方法用于任务在外面被增加或删除时提醒控件重新读取数据
|  Unit   | [notifyItemRefresh](#notifyItemRefresh) ( position: Int = mViewPager2.currentItem, isBackToCurrentTime: Boolean = false )  
|         | 默认通知当前页面所有的任务刷新，可输入索引值定向刷新
|  Unit   | [registerOnPageChangeCallback](#registerOnPageChangeCallback) ( callback: OnPageChangeCallback )  
|         | 设置内部ViewPager2的OnPageChangeCallback
|  Unit   | [setCurrentItem](#setCurrentItem) ( item: Int, smoothScroll: Boolean = true )  
|         | 设置内部ViewPager2显示的页面位置
|  Unit   | [setDragResistance](#setDragResistance) ( resistance: Int = DEFAULT_DRAG_RESISTANCE )  
|         | 设置相邻时间轴中拖动任务的阻力值
|  Unit   | [setIsShowDiffTime](#setIsShowDiffTime) ( boolean: Boolean )  
|         | 最终的任务区域是否显示时间差
|  Unit   | [setIsShowTopBottomTime](#setIsShowTopBottomTime) ( boolean: Boolean )  
|         | 最终的任务区域是否显示上下边界时间
|  Unit   | [setOnDataListener](#setOnDataListener) ( l: OnDataChangeListener )  
|         | 对数据改变进行监听
|  Unit   | [setOnTSVClickListener](#setOnTSVClickListener) ( onClick: ( taskBean: [TSViewTaskBean](#TSViewTaskBean) ) -> Unit )  
|         | 点击当前任务的监听
|  Unit   | [setOnTSVLongClickListener](#setOnTSVLongClickListener) ( onStart: (( condition: [TSViewLongClick](#TSViewLongClick) ) -> Unit ), onEnd: (( condition: [TSViewLongClick](#TSViewLongClick) ) -> Unit ))  
|         | 设置长按监听接口
|  Unit   | [setOnScrollListener](#setOnScrollListener) ( l: ( scrollY: Int, itemPosition: Int ) -> Unit )  
|         | 当前页面时间轴的滑动回调
|  Unit   | [setTimeInterval](#setTimeInterval) ( timeInterval: Int )  
|         | 设置时间间隔数
|  Unit   | [timeLineScrollBy](#timeLineScrollBy) ( dy: Int )  
|         | 与ScrollBy相同
|  Unit   | [timeLineScrollTo](#timeLineScrollTo) ( scrollY: Int )  
|         | 使时间轴瞬移，与ScrollTo相同
|  Unit   | [timeLineSlowlyScrollTo](#timeLineSlowlyScrollTo) ( scrollY: Int )  
|         | 使时间轴较缓慢地滑动，并有回弹动画

#### backCurrentTime
````kotlin
fun backCurrentTime()
````
当前页面回到xml中设置的 app:centerTime 时间，若为-1以中心值为中心线，-2为以目前时间值为中心线
>

#### cancelAutoBackCurrent
````kotlin
fun cancelAutoBackCurrent()
````
取消当前页面自动回到xml中设置的 CurrentTime 的延时。延时是在每次手指离开时间轴就会开启
>

#### getCurrentItem
````kotlin
fun getCurrentItem(): Int
````
得到内部 ViewPager2 的当前item索引
>

#### getIsLongClick
````kotlin
fun getIsLongClick(): Boolean
````
得到当前页面的 TimeSelectView 是否处于长按状态。  
若你想得到软件中所有的 TimeSelectView 是否存在处于长按状态的，可以使用 [TSViewLongClick](#TSViewLongClick)#sHasLongClick
>

#### getTimeLineScrollY
````kotlin
fun getTimeLineScrollY(): Int
````
得到当前页面的时间轴的ScrollY

#### initializeBean
````kotlin
fun initializeBean(dayBeans: ArrayList<TSViewDayBean>, 
                   showNowTimeLinePosition: Int = -1, 
                   currentItem: Int = 0, 
                   smoothScroll: Boolean = false)
````
初始化数据

|        Parameters        | |
| :----------------------- | ------------
| dayBeans                 | ArrayList<TSViewDayBean>: 以 [TSViewDayBean](#TSViewDayBean) 为数据的数组
| showNowTimeLinePosition  | Int = -1: 显示时间线的位置，从0开始，传入负数将不会显示
| currentItem              | Int = 0: 内部 ViewPager2 的 item 位置，默认值为0
| smoothScroll             | Boolean = false: 设置上方的 currentItem 后，在初始化时是否显示移动动画，默认值为false

#### notifyAllItemRefresh
````kotlin
fun notifyAllItemRefresh()
````
通知ViewPager2的所有item刷新

#### notifyItemDataChanged
````kotlin
fun notifyItemDataChanged(position: Int = mViewPager2.currentItem, 
                          isBackToCurrentTime: Boolean = false)
````
该方法用于任务在外面被增加或删除时提醒控件重新读取数据，***控件内部数据的增添删改也会引起外面传进来的数组中数据的改变***

|        Parameters        | |
| :----------------------- | ------------
| position                 | Int = mViewPager2.currentItem: 通知内部 ViewPager2 的页面位置刷新，默认为当前显示界面
| isBackToCurrentTime      | Boolean = false: 刷新是否回到 xml 中设置的 app:centerTime 时间

#### notifyItemRefresh
````kotlin
fun notifyItemRefresh(position: Int = mViewPager2.currentItem, 
                      isBackToCurrentTime: Boolean = false)
````
默认通知当前页面所有的任务刷新，可输入索引值定向刷新

|        Parameters        | |
| :----------------------- | ------------
| position                 | Int = mViewPager2.currentItem: 通知内部 ViewPager2 的页面位置刷新，默认为当前显示界面
| isBackToCurrentTime      | Boolean = false: 刷新是否回到 xml 中设置的 app:centerTime 时间

#### registerOnPageChangeCallback
````kotlin
fun registerOnPageChangeCallback(callback: OnPageChangeCallback)
````
设置内部ViewPager2的OnPageChangeCallback

#### setCurrentItem
````kotlin
fun setCurrentItem(item: Int, 
                   smoothScroll: Boolean = true)
````
设置内部ViewPager2显示的页面位置

#### setDragResistance
````kotlin
fun setDragResistance(resistance: Int = DEFAULT_DRAG_RESISTANCE)
````
设置相邻时间轴中拖动任务的阻力值

|        Parameters        | |
| :----------------------- | ------------
| resistance               | Int = DEFAULT_DRAG_RESISTANCE: 设置相邻


#### setIsShowDiffTime
````kotlin
fun setIsShowDiffTime(boolean: Boolean)
````
最终的任务区域是否显示时间差

#### setIsShowTopBottomTime
````kotlin
fun setIsShowTopBottomTime(boolean: Boolean)
````
最终的任务区域是否显示上下边界时间

#### setOnDataListener
````kotlin
fun setOnDataListener(l: OnDataChangeListener)
````
对数据改变进行监听  
***注意：*** 移至删除区域被删除或长按添加新任务时会引起外面传进来的数组中数据的改变
所以在数据改变后的回调中不需删掉或增加数据

#### setOnTSVClickListener
````kotlin
fun setOnTSVClickListener(onClick: (taskBean: TSViewTaskBean) -> Unit)
````
点击当前任务的监听，会返回当前点击任务的数据类  
***注意：*** 对 [TSViewTaskBean](#TSViewTaskBean) 修改数据后并不会自己刷新，请手动调用notifyAllTaskRefresh()进行刷新

|        Parameters        | |
| :----------------------- | ------------
| onClick                  | (taskBean: TSViewTaskBean) -> Unit: 点击任务后的回调，返回点击任务的 [TSViewTaskBean](#TSViewTaskBean) 

#### setOnTSVLongClickListener
````kotlin
fun setOnTSVLongClickListener(onStart: ((condition: TSViewLongClick) -> Unit), 
                              onEnd: ((condition: TSViewLongClick) -> Unit))
````
设置长按监听接口

|        Parameters        | |
| :----------------------- | ------------
| onStart                  | (condition: TSViewLongClick) -> Unit: 长按开始的回调，返回长按的情况，详细请看 [TSViewLongClick](#TSViewLongClick)
| onEnd                    | (condition: TSViewLongClick) -> Unit: 长按结束的回调，返回长按的情况，详细请看 [TSViewLongClick](#TSViewLongClick)


#### setOnScrollListener
````kotlin
fun setOnScrollListener(l: (scrollY: Int, itemPosition: Int) -> Unit)
````
当前页面时间轴的滑动回调  
不是 ViewPager2 的滑动回调,若你想监听 ViewPager2 的滑动，请使用 [registerOnPageChangeCallback](#registeronpagechangecallback)

|        Parameters        | |
| :----------------------- | ------------
| l                        | scrollY: Int: 返回控件的 scrollY
|                          | itemPosition: Int: 内部 ViewPager2 的当前 item 位置



#### setTimeInterval
````kotlin
fun setTimeInterval(timeInterval: Int)
````
设置时间间隔数  
***注意：*** 必须为60的因数，若不是，将以15为间隔数

#### timeLineScrollBy
````kotlin
fun timeLineScrollBy(dy: Int)
````
与 ScrollBy 相同  

|        Parameters        | |
| :----------------------- | ------------
| dy                       | Int: dy > 0，向上瞬移；dy < 0，向下瞬移

#### timeLineScrollTo
````kotlin
fun timeLineScrollTo(scrollY: Int)
````
使时间轴瞬移，与 ScrollTo 相同

#### timeLineSlowlyScrollTo
````kotlin
fun timeLineSlowlyScrollTo(scrollY: Int)
````
使时间轴较缓慢地滑动，并有回弹动画

## 滑动冲突
如果在本控件处于 ViewPager2 或其他滑动控件以内，请在这些控件的 onInterceptTouchEvent() 中这样设置
* 因为 ViewPager2 是 final 无法被重写，所以推荐下面写法
````kotlin
// 重写ViewPager2的父View或者无父View时的Activity     的dispatchTouchEvent()方法
private var mInitialRawX = 0
private var mInitialRawY = 0
private var timeViewLocation = Rect() // 使用mTimeSelectView.getLocationOnScreen(timeViewLocation)得到，记得用post()
override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    val x = ev.rawX.toInt()
    val y = ev.rawY.toInt()
    when (ev.action) {
        MotionEvent.ACTION_DOWN -> {
            mInitialRawX = x
            mInitialRawY = y
            mFgViewPager.isUserInputEnabled = true
        }
        MotionEvent.ACTION_MOVE -> {
            when (mViewPager2.currentItem) {
                // 把0替换为TimeSelectView在ViewPager2中的页面
                0 -> {
                    if (timeViewLocation.contains(mInitialRawX, mInitialRawY)) {
                        if (abs(x - mInitialRawX) <= TimeSelectView.MOVE_THRESHOLD + 3 || abs(y - mInitialRawY) <= TimeSelectView.MOVE_THRESHOLD + 3) {
                            mFgViewPager.isUserInputEnabled = false
                        }else {
                            mFgViewPager.isUserInputEnabled = !TSViewLongClick.sHasLongClick
                        }
                    }
                }
            }
        }
    }
    return super.dispatchTouchEvent(ev)
}
````
* 处于其他滑动控件中
````kotlin
// 重写这个滑动控件的onInterceptTouchEvent()方法
private var mInitialRawX = 0
private var mInitialRawY = 0
private var timeViewLocation = Rect() // 使用mTimeSelectView.getLocationOnScreen(timeViewLocation)得到，记得用post()
override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    val x = ev.rawX.toInt()
    val y = ev.rawY.toInt()
    when (ev.action) {
        MotionEvent.ACTION_DOWN -> {
            mInitialRawX = x
            mInitialRawY = y
        }
        MotionEvent.ACTION_MOVE -> {
            when (mViewPager2.currentItem) {
                // 把0替换为TimeSelectView在ViewPager2中的页面
                0 -> {
                    if (timeViewLocation.contains(mInitialRawX, mInitialRawY)) {
                        if (abs(x - mInitialRawX) <= TimeSelectView.MOVE_THRESHOLD + 3 || abs(y - mInitialRawY) <= TimeSelectView.MOVE_THRESHOLD + 3) {
                            return false
                        }else {
                            return !TSViewLongClick.sHasLongClick
                        }
                    }
                }
            }
        }
    }
    return false
}
````

# TSViewDayBean
# TSViewTaskBean
# TSViewLongClick
