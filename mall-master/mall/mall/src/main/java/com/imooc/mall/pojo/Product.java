package com.imooc.mall.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Product {
    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private String subImages;

    private String detail;

    private BigDecimal price;

    //库存数量
    private Integer stock;

    private Integer status;

    private Date createTime;

    private Date updateTime;
}