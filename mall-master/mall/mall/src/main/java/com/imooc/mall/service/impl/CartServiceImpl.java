package com.imooc.mall.service.impl;

import com.google.gson.Gson;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.enums.ProductStatusEnum;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.pojo.Cart;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartProductVo;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 购物车server
 * Created by 廖师兄
 */
@Service
public class CartServiceImpl implements ICartService {

	private final static String CART_REDIS_KEY_TEMPLATE = "cart_%d";

	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private StringRedisTemplate redisTemplate;

	private Gson gson = new Gson();

	/**\
	 * 把一件商品加入购物车（中括号）  [(商品1)、(商品2)]
	 * @param uid   uuid
	 * @param form
	 * @return
	 */
	@Override
	public ResponseVo<CartVo> add(Integer uid, CartAddForm form) {
		Integer quantity = 1;

		Product product = productMapper.selectByPrimaryKey(form.getProductId());
		//商品是否存在
		if (product == null) {
			return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
		}

		//商品是否正常在售  不等于1 就是下架或删除
		if (!product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())) {
			return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
		}

		//商品库存是否充足
		if (product.getStock() <= 0) {
			return ResponseVo.error(ResponseEnum.PROODUCT_STOCK_ERROR);
		}

		//写入到redis
		//key: cart_1
		HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
		String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);


		Cart cart;
		String value = opsForHash.get(redisKey, String.valueOf(product.getId()));
		if (StringUtils.isEmpty(value)) {
			//没有该商品, 新增
			cart = new Cart(product.getId(), quantity, form.getSelected());
		}else {
			//已经有了，数量+1
			cart = this.gson.fromJson(value, Cart.class);
			cart.setQuantity(cart.getQuantity() + quantity);
		}

		opsForHash.put(redisKey,
				String.valueOf(product.getId()),
				this.gson.toJson(cart));

		//
		return list(uid);
	}

	/**\
	 *
	 * @param uid
	 * @return
	 */
	@Override
	public ResponseVo<CartVo> list(Integer uid) {
		HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
		String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);
		Map<String, String> entries = opsForHash.entries(redisKey);

		boolean selectAll = true;
		Integer cartTotalQuantity = 0;
		BigDecimal cartTotalPrice = BigDecimal.ZERO;
		CartVo cartVo = new CartVo();
		List<CartProductVo> cartProductVoList = new ArrayList<>();

		//改进  针对循环sql  productMapper.selectByProductIdSet(productIdSet);
		//   where id  in  <foreach collection="productIdSet" item="item" index="index" open="(" separator="," close=")">

		List<Cart> cartList = new ArrayList<>();
		Set<Integer> productIds = new HashSet<>();
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			productIds.add(Integer.valueOf(entry.getKey()));
			cartList.add(gson.fromJson(entry.getValue(), Cart.class));
		}
		List<Product> productList = productMapper.selectByProductIdSet(productIds);
		int productListSize = productList.size();
		for (int i=0;i<productListSize;i++) {  //Product product:productList
			Product product = productList.get(i);
			Cart cart = cartList.get(i);
			if (product != null) {
				CartProductVo cartProductVo = new CartProductVo(product.getId(),
						cart.getQuantity(),
						product.getName(),
						product.getSubtitle(),
						product.getMainImage(),
						product.getPrice(),
						product.getStatus(),
						product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
						product.getStock(),
						cart.getProductSelected()
				);
				cartProductVoList.add(cartProductVo);

				if (!cart.getProductSelected()) {
					selectAll = false;
				}

				//计算总价(只计算选中的)
				if (cart.getProductSelected()) {
					cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
				}
			}
		}


		//原版
//		for (Map.Entry<String, String> entry : entries.entrySet()) {
//			Integer productId = Integer.valueOf(entry.getKey());
//			Cart cart = gson.fromJson(entry.getValue(), Cart.class);
//
//			//TODO 需要优化，使用mysql里的in
//			Product product = productMapper.selectByPrimaryKey(productId);
//			if (product != null) {
//				CartProductVo cartProductVo = new CartProductVo(productId,
//						cart.getQuantity(),
//						product.getName(),
//						product.getSubtitle(),
//						product.getMainImage(),
//						product.getPrice(),
//						product.getStatus(),
//						product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
//						product.getStock(),
//						cart.getProductSelected()
//				);
//				cartProductVoList.add(cartProductVo);
//
//				if (!cart.getProductSelected()) {
//					selectAll = false;
//				}
//
//				//计算总价(只计算选中的)
//				if (cart.getProductSelected()) {
//					cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
//				}
//			}
//
//			cartTotalQuantity += cart.getQuantity();
//		}

		//有一个没有选中，就不叫全选
		cartVo.setSelectedAll(selectAll);
		cartVo.setCartTotalQuantity(cartTotalQuantity);
		cartVo.setCartTotalPrice(cartTotalPrice);
		cartVo.setCartProductVoList(cartProductVoList);
		return ResponseVo.success(cartVo);
	}

	/**\
	 * 更新购物车（的其中一个） 数量 状态
	 * @param uid
	 * @param productId
	 * @param form
	 * @return
	 */
	@Override
	public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm form) {
		HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
		String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);

		String value = opsForHash.get(redisKey, String.valueOf(productId));
		if (StringUtils.isEmpty(value)) {
			//没有该商品, 报错
			return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
		}

		//已经有了，修改内容
		Cart cart = gson.fromJson(value, Cart.class);
		if (form.getQuantity() != null
				&& form.getQuantity() >= 0) {
			cart.setQuantity(form.getQuantity());
		}
		if (form.getSelected() != null) {
			cart.setProductSelected(form.getSelected());
		}

		opsForHash.put(redisKey, String.valueOf(productId), gson.toJson(cart));
		return list(uid);
	}

	@Override
	public ResponseVo<CartVo> delete(Integer uid, Integer productId) {
		HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
		String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);

		String value = opsForHash.get(redisKey, String.valueOf(productId));
		if (StringUtils.isEmpty(value)) {
			//没有该商品, 报错
			return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
		}

		opsForHash.delete(redisKey, String.valueOf(productId));
		return list(uid);
	}

	/**\
	 * 全选  全不选
	 * @param uid
	 * @return
	 */
	@Override
	public ResponseVo<CartVo> selectAll(Integer uid) {
		HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
		String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);

		for (Cart cart : listForCart(uid)) {
			cart.setProductSelected(true);
			opsForHash.put(redisKey,
					String.valueOf(cart.getProductId()),
					gson.toJson(cart));
		}

		return list(uid);
	}

	@Override
	public ResponseVo<CartVo> unSelectAll(Integer uid) {
		HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
		String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);

		for (Cart cart : listForCart(uid)) {
			cart.setProductSelected(false);
			opsForHash.put(redisKey,
					String.valueOf(cart.getProductId()),
					gson.toJson(cart));
		}

		return list(uid);
	}

	@Override
	public ResponseVo<Integer> sum(Integer uid) {
		//获取List（购物车）里面所有商品的数量 累加， reduce 从0开始加
		Integer sum = listForCart(uid).stream()
				.map(Cart::getQuantity)
				.reduce(0, Integer::sum);
		return ResponseVo.success(sum);
	}

	//遍历redis 对象
	public List<Cart> listForCart(Integer uid) {
		HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
		String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);
		Map<String, String> entries = opsForHash.entries(redisKey);//获取变量中的键值对。

		List<Cart> cartList = new ArrayList<>();
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			//gson.fromJson  json反序列化
			cartList.add(gson.fromJson(entry.getValue(), Cart.class));
		}

		return cartList;
	}

}
