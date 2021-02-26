package com.imooc.mall.service.impl;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.rabbitmq.client.ConnectionFactoryConfigurator.PASSWORD;
import static com.rabbitmq.client.ConnectionFactoryConfigurator.USERNAME;

//要么加上这两个注解   要么继承MallApplicationTests
//@RunWith(SpringRunner.class)
//@SpringBootTest
@Transactional   // 事物  但在这个测试类里面起到  数据表回滚的作用  即添加该注解可以回滚
public class UserServiceImplTest extends MallApplicationTests {

    @Autowired
    private IUserService userService;

    @Before
    public void register() {
        User user = new User(USERNAME, PASSWORD,"xxxx@qq.com",1);
        userService.register(user);
    }

    @Test
    public void login(){
        //register();
        ResponseVo<User> responseVo = userService.login(USERNAME, PASSWORD);
        Assert.assertEquals(ResponseEnum.ERROR.getCode(),responseVo.getStatus());
    }

}