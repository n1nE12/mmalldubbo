package com.n1ne.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.n1ne.mmall.common.Const;
import com.n1ne.mmall.common.ResponseCode;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.dao.CategoryMapper;
import com.n1ne.mmall.dao.ProductMapper;
import com.n1ne.mmall.pojo.Category;
import com.n1ne.mmall.pojo.Product;
import com.n1ne.mmall.service.ICategoryService;
import com.n1ne.mmall.service.IProductService;
import com.n1ne.mmall.util.DateTimeUtil;
import com.n1ne.mmall.util.PropertiesUtil;
import com.n1ne.mmall.vo.ProductDetailVo;
import com.n1ne.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: mmall
 * @description:
 * @author: n1ne
 * @create: 2019-04-01 16:20
 **/

@Service(interfaceName = "iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    ICategoryService iCategoryService;


    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    /**
     * @Description: 更新或新建产品
     * @Param: [product]
     * @return: com.n1ne.mmall.common.ServerResponse
     * @Author: n1nE
     * @Date: 4/2/19
     */
    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            if (StringUtils.isBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
                if (product.getId() != null) {
                    int rowCount = productMapper.updateByPrimaryKey(product);
                    if (rowCount > 0) {
                        return ServerResponse.createBySuccessMessage("更新产品信息成功");
                    }
                    return ServerResponse.createByErrorMessage("更新产品失败");
                } else {
                    int rowCount = productMapper.insert(product);
                    if (rowCount > 0) {
                        return ServerResponse.createBySuccessMessage("新增产品成功");
                    }
                    return ServerResponse.createByErrorMessage("新增产品失败");

                }
            }

        }
        return ServerResponse.createByErrorMessage("参数有误");
    }


    /**
     * @Description: 更新产品销售状态
     * @Param: [productId, status]
     * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
     * @Author: n1nE
     * @Date: 4/2/19
     */
    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("产品状态修改成功");
        }
        return ServerResponse.createByErrorMessage("产品状态修改失败");
    }


    /**
     * @Description: 获取产品信息
     * @Param: [productId]
     * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.vo.ProductDetailVo>
     * @Author: n1nE
     * @Date: 4/2/19
     */
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }


    /**
    * @Description: 搜索
    * @Param: [productName, productId, pageNum, pageSize]
    * @return: com.n1ne.mmall.common.ServerResponse<com.github.pagehelper.PageInfo>
    * @Author: n1nE
    * @Date: 4/4/19
    */
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndId(productName, productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /** 
    * @Description: 前台获取商品信息 
    * @Param: [productId] 
    * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.vo.ProductDetailVo> 
    * @Author: n1nE
    * @Date: 4/7/19 
    */ 
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }


    /** 
    * @Description:  
    * @Param: [keyword, categoryId, pageNum, pageSize, orderBy] 
    * @return: com.n1ne.mmall.common.ServerResponse<com.github.pagehelper.PageInfo> 
    * @Author: n1nE
    * @Date: 4/7/19 
    */ 
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();

        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);

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
     * @Description: POJO——>VO
     * @Param: [product]
     * @return: com.n1ne.mmall.vo.ProductDetailVo
     * @Author: n1nE
     * @Date: 4/2/19
     */
    private ProductDetailVo assembleProductDetailVo(Product product) {
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

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }


}
