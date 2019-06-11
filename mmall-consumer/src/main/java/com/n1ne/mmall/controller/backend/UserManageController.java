package com.n1ne.mmall.controller.backend;

import com.alibaba.dubbo.config.annotation.Reference;
import com.n1ne.mmall.common.Const;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.pojo.User;
import com.n1ne.mmall.service.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @program: mmall
 * @description:
 * @author: n1ne
 * @create: 2019-03-28 14:51
 **/

@RestController
@RequestMapping("/manage/user")
public class UserManageController {

    @Reference
    private IUserService iUserService;

    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    public ServerResponse<User> login(HttpSession session, String username, String password){
        ServerResponse<User> response = iUserService.login(username,password);
        if (response.isSuccess()) {
            User user = response.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN){
                session.setAttribute(Const.CURRENT_USER,user);
                return response;
            }else {
                return ServerResponse.createByErrorMessage("用户非管理员");
            }
        }
        return response;
    }
}
