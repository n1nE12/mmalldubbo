package com.n1ne.mmall.service;

import com.github.pagehelper.PageInfo;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.pojo.Product;
import com.n1ne.mmall.vo.ProductDetailVo;


public interface IProductService {
    ServerResponse getProductList(int pageNum, int pageSize);

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy);
}
