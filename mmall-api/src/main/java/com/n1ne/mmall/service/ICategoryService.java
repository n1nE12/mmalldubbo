package com.n1ne.mmall.service;

import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.pojo.Category;

import java.util.List;
import java.util.Set;


public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallerlCatagory(Integer parentId);

    ServerResponse<List> selectCategoryAndChildrenById(Integer categoryId);

    Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId);
}
