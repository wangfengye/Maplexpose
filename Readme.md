## xpose之路

#### 目前实现了一个自定义微信地区的功能,支持微信6.1.8.升级后失效 
#### hook定位数据,目的:获取百度,高德sdk提供的wifi数据
> 实现原理,hook gps,基站数据的获取接口,将返回值置空.修改`getScanResults()`返回的wifi列表,这样sdk返回的定位数据就是依据我们伪造的wifi数据产生的

### Version

* WifiLoc_1.1: 新增调试模式,上传任务相关数据


##### 存在问题
1. 返回的列表至少包含3个wifi,否则不使用wifi定位
2. 伪造的wifi列表包含实际地址距离过大的wifi,或不存在的wifi,会出现位置漂移(例子: 随机给金华的wifi,mac:偶尔出现海南新疆的位置)
3. 定位过于频繁,导致定位sdk崩溃,定位数据不变等问题(1s 10+次定位)
#####.最终方案
> 尽量使用同一点位采集到的wifi;模拟真实定位间隔(2s/次);

##### android上的通用技术问题
1. Hook函数是执行在被hook的应用的进程中,如何动态修改hook函数的变量?
    1. SharedPreference共享,xposed自己封装了获取其他应用SharedPreference的XSharedPreference.缺点:android7.0后,不在支持`MODE_WORLD_READABLE`,及只能在6.0及之前使用
    2. 使用ContentProvider提供跨应用的数据共享,需要hook`getApplicationContext`获取宿主的context才能使用
2. 进程间通信,本应用需要知道定位状态,及同步的进程通信
    1. broadcast,不支持同步
    2. Messenger 本质binder 通过handler 实现, 只支持异步,不支持并发
    3. aidl 本质binder ,支持小规模并发,同步, 最终采用该方式
    4. Android匿名共享内存（Ashmem）[文档](https://www.jianshu.com/p/d9bc9c668ba6)
    5. Android组件间通信通过bundle传递数据,支持跨进程
3. AIDL中Parcelable 实现时必须保证完全一致
   1. 我在一端加了两个字段,但未将该字段加入序列化中,导致解析错位


###### 2.0 Mqtt引入
1.```找到服务端配置的tcp端口```,开始使用ws接口浪费很长时间(异常:使用其他协议端口连接会导致,阻塞在连接中,且超时也不释放,很坑)

2. mqtt的回调接口,使用了Broacast广播进行回调,因此回调中的代码运行在主线程;
###### TODO
1. 定位数据返回设计不够优雅, 百度,高德定位是异步定位, aidl接口也是单独的线程,目前通过for循环自旋,阻塞,等待,高德百度都完成,修改标志位,结束for循环,返回结果

##### crash记录
1. `Servicehas leaked ServiceConnection c1 that was originally bound here` 原因service开启后未销毁
2. 7.0+ 上使用SharedPreference的 `MODE_WORLD_READABLE`闪退.
## 使用工具
* 方式一

    1. root手机
    2. 安装xpose框架
    3. 安装本APP
    4. 重启后生效

* 方式二

    1. 安装[VirtualXposed](https://github.com/android-hacker/VirtualXposed/releases)
    2. [VirtualXposed使用方法](https://www.jianshu.com/p/8cb84bad1e7f)按照该方法及微信及本App添加到VirtualXposed中,启用本App模块并重启


* [参考内容](https://juejin.im/post/5bfed63ce51d457ce0451ff6)
