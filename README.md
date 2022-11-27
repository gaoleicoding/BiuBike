# BiuBike
  
 详情可参考博客：http://blog.csdn.net/gaolei1201/article/details/60876811  ,喜欢的话留下你的Star让我知道^_^。

 
 最近共享单车很火，动辄几亿美刀，屌丝的我只有羡慕的份。啥时候自己也能创一番事业呢？我眉头紧皱深深地思索着。自己以前没做过地图有关的项目，看到网上也没有完整有关地图的项目，就想起模仿一下摩拜单车app，我这个小项目包括附近车辆、规划路径、行驶距离、行驶轨迹记录、轨迹回放，导航等（也挺全的哈），其中的附近车辆用的是假数据，行驶轨迹记录都是保存在本地数据库。实际项目中附近车辆和行驶轨迹都是要保存到服务器端。

百度地图开放平台：http://lbsyun.baidu.com



-------------------------------------------

V2.2.0更新：

 1、兼容8.0 9.0系统

 2、解决地图不能定位的问题（因为9.0及之后非https请求需要配置network-security-config）

 3、解决Notification不能显示的问题（因为9.0及之后Notification需要加Channel）

 4、更换Translucent状态栏实现方式

 5、BroadcastReceiver替换为EveentBus 

 6、行程中增加行程轨迹 
 
 7、优化逻辑和代码



-------------------------------------------


 app扫码下载体验

 ![image](https://github.com/gaoleiandroid1201/BiuBike/raw/master/material/screenshots/download.png)

app效果图

![image](https://github.com/gaoleiandroid1201/BiuBike/raw/master/material/screenshots/1.gif)

![image](https://github.com/gaoleiandroid1201/BiuBike/raw/master/material/screenshots/2.png)

![image](https://github.com/gaoleiandroid1201/BiuBike/raw/master/material/screenshots/3.png)
