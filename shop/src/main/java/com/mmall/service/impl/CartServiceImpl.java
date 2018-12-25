package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    //添加购物车
    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        //通过用户和产品ID查到该购物车
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart == null){
            //这个产品不在这个购物车里,需要新增一个这个产品的记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);//数量
            cartItem.setChecked(Const.Cart.CHECKED);//默认选中状态
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else{//这个产品已经在购物车里了.

            count = cart.getQuantity() + count;  //如果产品已存在,数量相加
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);  //updateByPrimaryKeySelective经常重用
        }
        return this.list(userId);
    }

    //更新删除购物车
    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);//更新数量
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.list(userId);   //原  CartVo cartVo  = this.getCartVoLimit(userId); return  ServerResponse.createBySuccess(cardVo);
    }

    public ServerResponse<CartVo> deleteProduct(Integer userId,String productIds){
        List<String> productList = Splitter.on(",").splitToList(productIds);// 用逗号分割字符串， 并添加到集合
        if(CollectionUtils.isEmpty(productList)){//空的话返回前端参数错误
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productList);//删除数量
        return this.list(userId);
    }
    /*
    delete from mmall_cart
    where user_id = #{userId}
    <if test="productIdList != null">
      and product_id in
      <foreach close=")" collection="productIdList" index="index" item="item" open="(" separator=",">
        #{item}
      </foreach>
    </if>
     */

    //查
    public ServerResponse<CartVo> list (Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }


    //选择或者反选所有的
    public ServerResponse<CartVo> selectOrUnSelect (Integer userId,Integer productId,Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }
    /*
    UPDATE  mmall_cart
    set checked = #{checked},   修改成传过来的checked
    update_time = now()
    where user_id = #{userId}
    <if test="productId != null">
      and product_id = #{productId}
    </if>
     */

    //获取当前购物车产品数量
    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));//select IFNULL(sum(quantity),0) as count from mmall_cart where user_id = #{userId}
    }                                                                                      // 查询数量的总和 别名count   IFNULL 如果查出数量总和为空  我们把它赋予0














    //限制商品  例如库存不足无法购买的情况
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);  //where user_id = #{userId}

        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");//购物车总价初始化为0

        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){//不是空   遍历它
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());//查询商品
                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());//副标题
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());//库存
                    //判断库存
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        //库存充足的时候   库存大于购买量
                        buyLimitCount = cartItem.getQuantity();//允许
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();//商品库存为允许购买的最大值
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();//为了更新数量而创建的对象
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);//数量
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);//更新库存
                    }
                    cartProductVo.setQuantity(buyLimitCount);//最新的数量变了
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));//  mul 乘法  价格*数量
                    cartProductVo.setProductChecked(cartItem.getChecked());//勾选
                }

                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中   加法 自身+
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);//总价
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));//是不是全选
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    //是不是全选
    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;   //SELECT  count(1) from mmall_cart where checked = 0 and user_id = #{userId}
                                                                                   //查表中有没有未勾选的 并且用户ID是我们传过来的  如果值是0 代表全部勾选  否则返回false
    }


























}
