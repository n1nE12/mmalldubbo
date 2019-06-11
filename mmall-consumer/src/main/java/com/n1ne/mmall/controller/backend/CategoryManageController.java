package com.n1ne.mmall.controller.backend;

import com.alibaba.dubbo.config.annotation.Reference;
import com.n1ne.mmall.common.Const;
import com.n1ne.mmall.common.ResponseCode;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.pojo.Category;
import com.n1ne.mmall.pojo.User;
import com.n1ne.mmall.service.ICategoryService;
import com.n1ne.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @program: mmall
 * @description: 分类功能接口
 * @author: n1ne
 * @create: 2019-03-25 16:14
 **/

@RestController
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Reference
    private IUserService iUserService;

    @Reference
    private ICategoryService iCategoryService;

    /**
     * @Description: 添加品类
     * @Param: [session, categoryName, parentId]
     * @return: com.n1ne.mmall.common.ServerResponse
     * @Author: n1nE
     * @Date: 3/26/19
     */
    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");
    }

    /**
     * @Description: 修改品类名
     * @Param: [session, categoryId, categoryName]
     * @return: com.n1ne.mmall.common.ServerResponse
     * @Author: n1nE
     * @Date: 3/26/19
     */
    @RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
    public ServerResponse setCategoryName(HttpSession session, int categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");
    }

    /**
     * @Description: 获取子品类
     * @Param: [session, parentId]
     * @return: com.n1ne.mmall.common.ServerResponse<java.util.List < com.n1ne.mmall.pojo.Category>>
     * @Author: n1nE
     * @Date: 3/26/19
     */
    @RequestMapping(value = "get_category.do")
    public ServerResponse<List<Category>> getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getChildrenParallerlCatagory(categoryId);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");
    }

    /** 
    * @Description: 递归获取子品类 
    * @Param: [session, categoryId] 
    * @return: com.n1ne.mmall.common.ServerResponse<java.util.List<com.n1ne.mmall.pojo.Category>> 
    * @Author: n1nE
    * @Date: 4/1/19 
    */ 
    @RequestMapping(value = "get_deep_category.do")
    public ServerResponse<List> getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");
    }


}
