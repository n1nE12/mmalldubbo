package com.n1ne.mmall.service;

import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.vo.CartVo;

public interface ICartService {
    ServerResponse add(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> list(Integer id);

    ServerResponse<CartVo> update(Integer id, Integer productId, Integer count);

    ServerResponse<CartVo> deleteProduct(Integer id, String productIds);

    ServerResponse<CartVo> selectOrUnSelect(Integer id, Integer productId, Integer checked);

    ServerResponse<Integer> getCartProductCount(Integer id);
}
