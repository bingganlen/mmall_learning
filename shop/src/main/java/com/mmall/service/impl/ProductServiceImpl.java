package com.mmall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geely
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {


    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    //新增或更新产品
    public ServerResponse saveOrUpdateProduct(Product product){
        if(product != null)
        {
            //图赋值
            if(StringUtils.isNotBlank(product.getSubImages())){//子图不为空
                String[] subImageArray = product.getSubImages().split(",");//逗号分割
                if(subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);
                }
            }

            if(product.getId() != null){ //商品ID为空就是新增商品
                int rowCount = productMapper.updateByPrimaryKey(product);//更新整个产品
                if(rowCount > 0){
                    return ServerResponse.createBySuccess("更新产品成功");
                }
                return ServerResponse.createBySuccess("更新产品失败");
            }else{
                int rowCount = productMapper.insert(product);
                if(rowCount > 0){
                    return ServerResponse.createBySuccess("新增产品成功");
                }
                return ServerResponse.createBySuccess("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");
    }

    //商品上下架
    public ServerResponse<String> setSaleStatus(Integer productId,Integer status){
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());//非法参数
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);// update mmall_product  <set>  where id = #{id,jdbcType=INTEGER}
        if(rowCount > 0){
            return ServerResponse.createBySuccess("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }

    ////后台获取商品
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());//参数错误
        }
        Product product = productMapper.selectByPrimaryKey(productId);//select..where id = #{id,jdbcType=INTEGER}


        if(product == null){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);  //VO
        return ServerResponse.createBySuccess(productDetailVo);
    }


    private ProductDetailVo assembleProductDetailVo(Product product){//为特定前端需要 VO
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));//毫秒数转化
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    /****************************** 动态分页   *********************************/

    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize){
        //startPage--start
        PageHelper.startPage(pageNum,pageSize);  //pageNum页码  pageSize每页容量
        List<Product> productList = productMapper.selectList(); //selectList   select <include refid="Base_Column_List"/>
        //填充自己的sql查询逻辑
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        //pageHelper-收尾
        PageInfo pageResult = new PageInfo(productList);//根据productList分页处理
        pageResult.setList(productListVoList);//给前端展示的不是Product内容   而是我们想要的productListVoList  135 138为了这个而写，不然可去除
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

/**************************** 商品搜索  ***********************************/
// 根据productName 或 productId搜索   selectByNameAndProductId  我们要的是或关系 而不是并
   /* <where>
      <if test="productName != null">
    and name like #{productName}
      </if>
      <if test="productId != null">
    and id = #{productId}
      </if>
    </where>
    /**
     *
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
            //使用StringBuilder代替使用+来进行字符串的串联
            productName = new StringBuilder().append("%").append(productName).append("%").toString(); //%productName%  再转化为String字符串
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);//  selectByNameAndProductId这个sql挺难的的
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    //
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null){//非法参数
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);////select..where id = #{id,jdbcType=INTEGER}
        if(product == null){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){//ON_SALE在线
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    //
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId == null){//参数校验  非法参数
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();//干嘛的 当传大的分类的时候 例如传一个电子产品  分类下面还有手机分类，手机又分为智能机非智能机分类
        //之前写的递归算法   把所有属于这个分类的子分类遍历出来 并且加上本身放到categoryIdList

        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);//获取分类
            if(category == null && StringUtils.isBlank(keyword)){
                //没有该分类,并且还没有关键字,——（根据categoryId查不到分类）   这个时候返回一个空的结果集,不报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);//分页生效
                return ServerResponse.createBySuccess(pageInfo);//返回一个空的结果集
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();//查找分类和子分类
        }//getNext()方法表示获取下一个节点对象（Node）的方法；           getData()方法表示获取该节点的数据的方法。

        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if(StringUtils.isNotBlank(orderBy)){//不是空的时候
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);  // PageHelper.orderBy("price desc");代表降序  所以要分割的拼接出来
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);
                                                                                                   //如果是空白返回null  否则keyword传出去
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
    /**

     <select id="selectByNameAndCategoryIds" parameterType="map" resultMap="BaseResultMap">
     SELECT
     <include refid="Base_Column_List" />
     from mmall_product
     where status = 1
     <if test="productName != null">
     and name like #{productName}
     </if>
     <if test="categoryIdList != null">
     and category_id in
     <foreach  collection="categoryIdList" index="index" item="item" open="(" separator=","  close=")">   左括号开始 逗号分割  右括号结束
     #{item}
     </foreach>
     </if>
     </select>

     */


























}
