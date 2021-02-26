package com.imooc.mall.enums;

import lombok.Getter;

/**
 * 角色0-管理员,1-普通用户
 * 因为常量很难维护 所以需要enum枚举
 * Created by 廖师兄
 */
@Getter
public enum RoleEnum {
	ADMIN(0),

	CUSTOMER(1),

	;

	Integer code;

	RoleEnum(Integer code) {
		this.code = code;
	}
}
