mmall_learning
==
###*linux配置
        阿里云服务器购买+域名  阿里云软件源配置说明 http://mirrors.aliyun.com/help/centos
        git
        nginx
        安装文档  http://learning.happymmall.com/
###**用户模块开发**       
        登录  用户名验证  注册  忘记密码  提交问题答案  重置密码  获取用户信息  更新用户信息  退出登录

###*分类管理模块*      
        获取节点  修改名字  增加节点  获取分类ID  递归子节点ID（传大的分类的时候 例如传一个电子产品  分类下面还有手机分类，手机又分为智能机非智能机分类）

###*商品管理模块  
        前台功能： 产品搜索  动态排序列表 分页（mybatis-PageHelper）  商品详情
                  后台功能： 商品列表  商品搜索  图片上传   富文本上传  商品详情  商品上下架  增加商品  更新商品
--                                           
###*购物车模块 
        加入商品  更新商品数  查询购物车商品数  移除商品  单选/取消  全选/取消
----
###*支付模块（难）
        支付宝对接  支付宝回调  查询支付状态    
        支付宝扫码支付的对接  沙箱调试环境  支付宝扫码支付主业务流程  支付宝扫码支付官方demo
--
###*订单模块  
        前台：  创建订单  订单列表  订单详情  取消订单
        后台：  订单列表  订单搜索  订单详情  订单发货
--                 
*线上部署与自动化发布 ：    自动化发布脚本  线上验证 
   >注意   
   >>前台 http://www.happymmall.com   \
   >>后台 http://admin.happymmall.com  \
   >>图片服务器  http://img.happymmall.com  \
   >>前端服务器  http://s.happymmall.com    
   
                        

---------
**接口扩展性最强   service使用接口   以大写I开头  表示这个类是接口**

**www.cmd5.com   可以对MD5加密后的进行解密**

**以VO结尾表示用于返回前端的数据**

**后台商品图片的springmvc上传和富文本上传ftp服务器**

**controller\backend  作为后台的控制台**

**主动轮询 与 回调的区别**
烧开水  你守着  （循环）每过1分钟看一次（主动轮询）  水烧开后有声音（回调）


##支付宝   https://open.alipay.com/platform/manageHome.htm   
 >>支付宝沙箱  https://openhome.alipay.com/platform/appDaily.htm     https://docs.open.alipay.com/200/105311/  \
 >>支付宝当面付  https://docs.open.alipay.com/194    \
 >>扫码支付接入指引   https://docs.open.alipay.com/194/106078   
 >>>支付宝官方的课程
 >>>授权回调地址选择   对于付款成功 扫码开始的时候调用的接口地址 http://www.happymmall.com/order/alipay_callback.do
 ####下载demo 现在在第10章支付模块开发\zhifubao\TradePayDemo
 > 1.配置zfbinfo.properties  
        2.webapp\WEB-INF\下新建lib   把包文件复制过来  project Setting 点击module-》dependencies 加号选第一个。。   \
        3.*打包的时候需要把这个lib也打包到服务器   \
        4.按原包com\alipay\demo\trade行式把代码添加到main文件夹下


-----------------
问题一：Maven mybatis-generator一些坑

原文：https://blog.csdn.net/qq_40307945/article/details/81351302 


问题二： 通用数据端响应对象    
在service层  UserServiceImpl实现用户登录时，   ServerResponse<User>就是数据端响应对象
      
     @Override
         public ServerResponse<User> login(String username, String password) {
     
         }
     
     （经常使用的工具泛型枚举）放在common里面   为什么要搞这个   ServerResponse<T> 返回的时候可以指定返回类型  例如  正确的时候返回一个String   失败的时候又返回什么
     构造器

问题三：  第二行与第三行
               org.springframework.beans.factory.BeanCreationException: 
               Error creating bean with name 'multipartResolver' defined in ServletContext resource [/WEB-INF/dispatcher-servlet.xml]: Instantiation of bean failed; nested exception is java.lang.NoClassDefFoundError: org/apache/commons/fileupload/FileItemFactory
           org.springframework.web.util.NestedServletException: Handler processing failed
          解决：  缺少包  文件上传的
                   <dependency>
                     <groupId>commons-fileupload</groupId>
                     <artifactId>commons-fileupload</artifactId>
                     <version>1.2.2</version>
                   </dependency>
                   <dependency>
                     <groupId>commons-io</groupId>
                     <artifactId>commons-io</artifactId>
                     <version>2.0.1</version>
                   </dependency>


问题四：   mapper.xml的参数与返回值的关系
          List<Cart> selectCheckedCartByUserId(Integer userId);
          参数是int  返回值是map
                <select id="selectCheckedCartByUserId" parameterType="int" resultMap="BaseResultMap">
                  SELECT
                  <include refid="Base_Column_List"/>
                  from mmall_cart
                  where user_id = #{userId}
                  and checked = 1
                </select>

问题五：IDEA确报错 Cannot resolve method println(java.lang.String) 
    File -> Invalidate Caches / Restart… 
    清空缓存就可以了
    
    cannot resolve method getParameter
    File>Project Structure>Modules>Dependencies，然后点击右边的绿色+号，选择Library，然后选择你的Tomcat服务器添加，然后再Apply应用即可。
    特别是添加Tomcat的驱动