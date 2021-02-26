package com.imooc.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 * Created by 廖师兄
 */
@Data
public class CartVo {

	private List<CartProductVo> cartProductVoList;

	/**\
	 * 是否全选
	 */
	private Boolean selectedAll;

	/**\
	 * 总价
	 */
	private BigDecimal cartTotalPrice;

	private Integer cartTotalQuantity;


	/*	"cartProductVoList": [
	{
		"productId": 1,
			"quantity": 1,
			"productName": "iphone7",
			"productSubtitle": "双十一促销",
			"productMainImage": "mainimage.jpg",
			"productPrice": 7199.22,
			"productStatus": 1,
			"productTotalPrice": 7199.22,
			"productStock": 86,
			"productSelected": true,
	},
	{
		"productId": 2,
			"quantity": 1,
			"productName": "oppo R8",
			"productSubtitle": "oppo促销进行中",
			"productMainImage": "mainimage.jpg",
			"productPrice": 2999.11,
			"productStatus": 1,
			"productTotalPrice": 2999.11,
			"productStock": 86,
			"productSelected": false,
	}
],
		"selectedAll": false,
		"cartTotalPrice": 10198.33,
		"cartTotalQuantity": 2
}*/
}
