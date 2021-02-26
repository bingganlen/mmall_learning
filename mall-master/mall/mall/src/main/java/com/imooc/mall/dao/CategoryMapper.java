package com.imooc.mall.dao;

import com.imooc.mall.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    @Select("select * from mall_category where id = #{id}")
    Category findById(@Param("id") Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    List<Category> selectAll();
}

//测试数据库接口   右键- GOTO - TEST
//Ctrl+ shift + T
