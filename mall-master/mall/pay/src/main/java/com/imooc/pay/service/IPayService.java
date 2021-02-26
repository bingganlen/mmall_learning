package com.imooc.pay.service;

import com.imooc.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

/**
 * Created by 廖师兄
 */
public interface IPayService {

	/**
	 * 创建/发起支付
	 */
	PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

	/**
	 * 异步通知处理
	 * @param notifyData
	 */
	String asyncNotify(String notifyData);

	/**
	 * 查询支付记录(通过订单号)
	 * @param orderId
	 * @return
	 */
	PayInfo queryByOrderId(String orderId);


	/**\
	 * 微信pay  支付 创建发起支付
	 * 参考  https://github.com/Pay-Group/best-pay-sdk/blob/develop/doc/use.md
	 */
	public void createWxPay(String orderId, BigDecimal amount);
	public void creatAliPay(String orderId, BigDecimal amount);

}
