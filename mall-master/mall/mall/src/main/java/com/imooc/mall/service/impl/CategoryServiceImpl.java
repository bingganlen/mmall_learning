package com.imooc.mall.service.impl;

import com.imooc.mall.dao.CategoryMapper;
import com.imooc.mall.pojo.Category;
import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.imooc.mall.consts.MallConst.ROOT_PARENT_ID;

/**
 * Created by 廖师兄
 */
@Service
public class CategoryServiceImpl implements ICategoryService {

	@Autowired
	private CategoryMapper categoryMapper;

	/**
	 * 耗时：http(请求微信api) > 磁盘 > 内存
	 * mysql(内网+磁盘)
	 * 多级目录获取
	 * @return
	 */
	@Override
	public ResponseVo<List<CategoryVo>> selectAll() {
		List<Category> categories = categoryMapper.selectAll();

		//查出parent_id=0
//		for (Category category : categories) {
//			if (category.getParentId().equals(ROOT_PARENT_ID)) {
//				CategoryVo categoryVo = new CategoryVo();
//				BeanUtils.copyProperties(category, categoryVo);
//				categoryVoList.add(categoryVo);
//			}
//		}

		//lambda + stream  业务很复杂的时候不要用lambda  别人很难看
		//filter 筛选出ParentId等于0的
		//map 转换
		//collect 最后返回list
		List<CategoryVo> categoryVoList = categories.stream()
				.filter(e -> e.getParentId().equals(ROOT_PARENT_ID))
				.map(this::category2CategoryVo)
				.sorted(Comparator.comparing(CategoryVo::getSortOrder).reversed())
				.collect(Collectors.toList());

		//查询子目录
		findSubCategory(categoryVoList, categories);

		return ResponseVo.success(categoryVoList);
	}

	//上面查询是多级目录列表  这里只查找Id
	@Override
	public void findSubCategoryId(Integer id, Set<Integer> resultSet) {
		List<Category> categories = categoryMapper.selectAll();
		findSubCategoryId(id, resultSet, categories);
	}

	private void findSubCategoryId(Integer id, Set<Integer> resultSet, List<Category> categories) {
		for (Category category : categories) {
			if (category.getParentId().equals(id)) {
				//该商品的父id == id  说明是该类目下的商品
				resultSet.add(category.getId());
				//查找本商品下有没有子商品  递归
				findSubCategoryId(category.getId(), resultSet, categories);
			}
		}
	}


	/**\
	 * id       parentId
	 * 100006	100001
	 * 100007	100001
	 * 100008	100001
	 * @param categoryVoList
	 * @param categories
	 */
	private void findSubCategory(List<CategoryVo> categoryVoList, List<Category> categories) {
		for (CategoryVo categoryVo : categoryVoList) {
			List<CategoryVo> subCategoryVoList = new ArrayList<>();

			for (Category category : categories) {
				//如果查到内容，设置subCategory, 继续往下查
				if (categoryVo.getId().equals(category.getParentId())) {
					CategoryVo subCategoryVo = category2CategoryVo(category);
					subCategoryVoList.add(subCategoryVo);
				}

				subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
				categoryVo.setSubCategories(subCategoryVoList);

				//重新调用  传到参数必须是已经遍历一次的subCategoryVoList   不然会死循环
				findSubCategory(subCategoryVoList, categories);
			}
		}
	}

	/**\
	 * category 转换 CategoryVo
	 * @param category
	 * @return
	 */
	private CategoryVo category2CategoryVo(Category category) {
		CategoryVo categoryVo = new CategoryVo();
		BeanUtils.copyProperties(category, categoryVo);
		return categoryVo;
	}
}
