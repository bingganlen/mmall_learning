package com.imooc.mall.dao;

import com.imooc.mall.MallApplication;
import com.imooc.mall.pojo.Category;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CategoryMapperTest extends MallApplication {

    //categoryMapper空指针
    //解决方法 ：  @RunWith(SpringRunner.class)  @SpringBootTest
    //          2.  继承一个空类 如MallApplicationTests
    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    public void selectByPrimaryKey() {
        Category category = categoryMapper.selectByPrimaryKey(100001);
        System.out.println(category.toString());
    }

    @Test
    public void findById() {
        Category category = categoryMapper.findById(1000001);
        System.out.println(category.toString());
    }
}