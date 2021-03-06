package com.imooc.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by 廖师兄
 */
@Data
public class CartProductVo {

	private Integer productId;

	/**
	 * 购买的数量
	 */
	private Integer quantity;

	private String productName;

	//子标题
	private String productSubtitle;

	private String productMainImage;

	private BigDecimal productPrice;

	private Integer productStatus;

	/**
	 * 等于 quantity * productPrice   购物车所有的总价
	 */
	private BigDecimal productTotalPrice;

	//库存
	private Integer productStock;

	/**
	 * 商品是否选中
	 */
	private Boolean productSelected;

	public CartProductVo(Integer productId, Integer quantity, String productName, String productSubtitle, String productMainImage, BigDecimal productPrice, Integer productStatus, BigDecimal productTotalPrice, Integer productStock, Boolean productSelected) {
		this.productId = productId;
		this.quantity = quantity;
		this.productName = productName;
		this.productSubtitle = productSubtitle;
		this.productMainImage = productMainImage;
		this.productPrice = productPrice;
		this.productStatus = productStatus;
		this.productTotalPrice = productTotalPrice;
		this.productStock = productStock;
		this.productSelected = productSelected;
	}
}
