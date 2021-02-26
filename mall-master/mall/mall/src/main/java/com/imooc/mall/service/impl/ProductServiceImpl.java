package com.imooc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.service.IProductService;
import com.imooc.mall.vo.ProductDetailVo;
import com.imooc.mall.vo.ProductVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.imooc.mall.enums.ProductStatusEnum.*;
import static com.imooc.mall.enums.ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE;

/**
 * Created by 廖师兄
 */
@Service
@Slf4j
public class ProductServiceImpl implements IProductService {

	@Autowired
	private ICategoryService categoryService;

	@Autowired
	private ProductMapper productMapper;

	/**\
	 * 分页
	 * @param categoryId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@Override
	public ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize) {
		Set<Integer> categoryIdSet = new HashSet<>();
		if (categoryId != null) {
			//只查询categoryId？ 不，他的子类目商品  子子商品
			categoryService.findSubCategoryId(categoryId, categoryIdSet);
			categoryIdSet.add(categoryId);//上一行代码只查询子类  需要加上它自身
		}

		PageHelper.startPage(pageNum, pageSize);
		//根据上面已经查询出的id 查询出
		/*select * from mall_product where status = 1
		<if test="categoryIdSet.size() > 0">
				and category_id in
      		<foreach collection="categoryIdSet" item="item" index="index" open="(" separator="," close=")">
        		#{item}
      		</foreach>
    	</if>*/
		List<Product> productList = productMapper.selectByCategoryIdSet(categoryIdSet);

		//Product转换成ProductVO
		List<ProductVo> productVoList = productList.stream()
				.map(e -> {
					ProductVo productVo = new ProductVo();
					BeanUtils.copyProperties(e, productVo);
					return productVo;
				})
				.collect(Collectors.toList());

		PageInfo pageInfo = new PageInfo<>(productList);
		pageInfo.setList(productVoList);
		return ResponseVo.success(pageInfo);
	}

	//商品详情
	@Override
	public ResponseVo<ProductDetailVo> detail(Integer productId) {
		Product product = productMapper.selectByPrimaryKey(productId);

		//只对确定性条件判断 下架或删除
		if (product.getStatus().equals(OFF_SALE.getCode())
				|| product.getStatus().equals(DELETE.getCode())) {
			return ResponseVo.error(PRODUCT_OFF_SALE_OR_DELETE);
		}

		ProductDetailVo productDetailVo = new ProductDetailVo();
		BeanUtils.copyProperties(product, productDetailVo);
		//敏感数据处理  库存  有人爬虫会专门查库存  库存如果大于100就显示100 小于100显示原本库存
		productDetailVo.setStock(product.getStock() > 100 ? 100 : product.getStock());
		return ResponseVo.success(productDetailVo);
	}
}
