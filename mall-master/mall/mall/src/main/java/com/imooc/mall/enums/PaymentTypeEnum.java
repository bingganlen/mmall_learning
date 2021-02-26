package com.imooc.mall.enums;

/**
 * 支付类型  1 ： 在线支付
 */

import lombok.Getter;

@Getter
public enum PaymentTypeEnum {

	PAY_ONLINE(1),
	;

	Integer code;

	PaymentTypeEnum(Integer code) {
		this.code = code;
	}
}
