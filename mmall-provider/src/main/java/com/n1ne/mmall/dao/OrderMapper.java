package com.n1ne.mmall.dao;

import com.n1ne.mmall.pojo.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);


    Order selectByOrderNo(Long orderNo);



    List<Order> selectByUserId(Integer userId);


    List<Order> selectAllOrder();
}