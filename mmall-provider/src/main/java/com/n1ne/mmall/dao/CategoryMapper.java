package com.n1ne.mmall.dao;

import com.n1ne.mmall.pojo.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    int updateNamebyId(@Param("categoryId") Integer categoryId, @Param("categoryName") String categoryName);

    List<Category> selectCategoryChidrenByParentId(Integer parentId);
}