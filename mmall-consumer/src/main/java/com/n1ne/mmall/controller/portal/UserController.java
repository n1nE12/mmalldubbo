package com.n1ne.mmall.controller.portal;

import com.alibaba.dubbo.config.annotation.Reference;
import com.n1ne.mmall.common.Const;
import com.n1ne.mmall.common.ResponseCode;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.pojo.User;
import com.n1ne.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @program: mall
 * @description:
 * @author: n1ne
 * @create: 2019-03-14 14:48
 **/

//@CrossOrigin(origins="http://localhost:63343",allowCredentials="true")
@RequestMapping("/user/")
@RestController
public class UserController {

    @Reference
    private IUserService iUserService;


    /**
     * @Description: 登陆
     * @Param: [username, password, session]
     * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.pojo.User>
     * @Author: n1nE
     * @Date: 3/18/19
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> response = iUserService.login(username, password);


        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * @Description: 登出
     * @Param: [session]
     * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
     * @Author: n1nE
     * @Date: 3/19/19
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * @Description: 注册
     * @Param: [user]
     * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
     * @Author: n1nE
     * @Date: 3/19/19
     */
    @RequestMapping(value = "register.do", method = RequestMethod.GET)
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * @Description: 校验
     * @Param: [str, type]
     * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
     * @Author: n1nE
     * @Date: 3/19/19
     */
    @RequestMapping(value = "check_vaild.do", method = RequestMethod.GET)
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.chechVaild(str, type);
    }


    /**
     * @Description: 获取用户信息
     * @Param: [session]
     * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.pojo.User>
     * @Author: n1nE
     * @Date: 3/19/19
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登陆，无法获取当前用户信息");
    }


    /**
     * @Description: 忘记密码问题
     * @Param: [username]
     * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
     * @Author: n1nE
     * @Date: 3/19/19
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.GET)
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }


    /**
    * @Description: 检验问题答案
    * @Param: [username, question, answer]
    * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
    * @Author: n1nE
    * @Date: 3/21/19
    */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.GET)
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
    * @Description:  通过问题重设密码
    * @Param: [username, passwordNew, forgetToken]
    * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
    * @Author: n1nE
    * @Date: 3/21/19
    */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.GET)
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    /** 
    * @Description:  在线状态重设密码
    * @Param: [session, passwordOld, passwordNew] 
    * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String> 
    * @Author: n1nE
    * @Date: 3/21/19 
    */ 
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
    * @Description: 用户更改信息
    * @Param: [session, user]
    * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.pojo.User>
    * @Author: n1nE
    * @Date: 3/21/19
    */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    public ServerResponse<User> update_information(HttpSession session, User user){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /** 
    * @Description:  获取用户信息
    * @Param: [session] 
    * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.pojo.User> 
    * @Author: n1nE
    * @Date: 3/21/19 
    */ 
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    public ServerResponse<User> get_information(HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }

}
