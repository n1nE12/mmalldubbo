package com.n1ne.mmall.service.impl;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.dao.CategoryMapper;
import com.n1ne.mmall.pojo.Category;
import com.n1ne.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;



/**
 * @program: mmall
 * @description:
 * @author: n1ne
 * @create: 2019-03-25 16:27
 **/

@Service(interfaceName = "iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (StringUtils.isBlank(categoryName) || parentId == null) {
            return ServerResponse.createByErrorMessage("参数有误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (StringUtils.isBlank(categoryName) || categoryId == null) {
            return ServerResponse.createByErrorMessage("参数有误");
        }
        int rowCount = categoryMapper.updateNamebyId(categoryId, categoryName);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    public ServerResponse<List<Category>> getChildrenParallerlCatagory(Integer parentId) {
        List<Category> categoryList = categoryMapper.selectCategoryChidrenByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("该品类下未找到子品类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    @Override
    public ServerResponse<List> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        categorySet = findChildCategory(categorySet,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            for (Category categoryItem : categorySet) {
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    @Override
    public Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectCategoryChidrenByParentId(categoryId);
        for (Category categoryItem : categoryList) {
            findChildCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }


}
