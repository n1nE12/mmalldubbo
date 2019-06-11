package com.n1ne.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.n1ne.mmall.common.Const;
import com.n1ne.mmall.common.ResponseCode;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.dao.CartMapper;
import com.n1ne.mmall.dao.ProductMapper;
import com.n1ne.mmall.pojo.Cart;
import com.n1ne.mmall.pojo.Product;
import com.n1ne.mmall.service.ICartService;
import com.n1ne.mmall.util.BigDecimalUtil;
import com.n1ne.mmall.util.PropertiesUtil;
import com.n1ne.mmall.vo.CartProductVo;
import com.n1ne.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: mmall
 * @description:
 * @author: n1ne
 * @create: 2019-04-08 16:16
 **/
@Service(interfaceName = "ICartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    /**
     * @Description: 添加商品进入购物车
     * @Param: [userId, productId, count]
     * @return: com.n1ne.mmall.common.ServerResponse
     * @Author: n1nE
     * @Date: 4/9/19
     */
    @Override
    public ServerResponse add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (cart == null) {
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        } else {
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }


    /**
     * @Description: 更新购物车信息
     * @Param: [userId, productId, count]
     * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.vo.CartVo>
     * @Author: n1nE
     * @Date: 4/9/19
     */
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.list(userId);
    }

    /** 
    * @Description:  
    * @Param: [userId] 
    * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.vo.CartVo> 
    * @Author: n1nE
    * @Date: 4/9/19 
    */ 
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /** 
    * @Description:
    * @Param: [userId] 
    * @return: com.n1ne.mmall.vo.CartVo 
    * @Author: n1nE
    * @Date: 4/9/19 
    */ 
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        //库存充足的时候
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }


    /**
     * @Description:
     * @Param: [userId]
     * @return: boolean
     * @Author: n1nE
     * @Date: 4/9/19
     */
    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }
    
    /** 
    * @Description:  
    * @Param: [userId, productIds] 
    * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.vo.CartVo> 
    * @Author: n1nE
    * @Date: 4/9/19 
    */ 
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId, productList);
        return this.list(userId);
    }


    /**
     * @Description:
     * @Param: [userId, productId, checked]
     * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.vo.CartVo>
     * @Author: n1nE
     * @Date: 4/9/19
     */
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.list(userId);
    }

    
    /** 
    * @Description:  
    * @Param: [userId] 
    * @return: com.n1ne.mmall.common.ServerResponse<java.lang.Integer> 
    * @Author: n1nE
    * @Date: 4/9/19 
    */ 
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }


}
