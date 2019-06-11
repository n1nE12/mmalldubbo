package com.n1ne.mmall.service.impl;

import com.n1ne.mmall.common.Const;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.common.TokenCache;
import com.n1ne.mmall.dao.UserMapper;
import com.n1ne.mmall.pojo.User;
import com.n1ne.mmall.service.IUserService;
import com.n1ne.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * @program: mall
 * @description:
 * @author: n1ne
 * @create: 2019-03-14 14:46
 **/

@Service(interfaceName = "iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void delete(Integer integer) {
    }


    /**
     * @Description: 登陆逻辑
     * @Param: [username, password]
     * @return: com.n1ne.mmall.common.ServerResponse<com.n1ne.mmall.pojo.User>
     * @Author: n1nE
     * @Date: 3/18/19
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    /**
     * @Description: 注册逻辑
     * @Param: [user]
     * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
     * @Author: n1nE
     * @Date: 3/19/19
     */
    @Override
    public ServerResponse<String> register(User user) {

        ServerResponse vaildResponse = this.chechVaild(user.getUsername(),Const.USERNAME);
        if (!vaildResponse.isSuccess()){
            return vaildResponse;
        }
        vaildResponse = this.chechVaild(user.getEmail(),Const.EMAIL);
        if (!vaildResponse.isSuccess()){
            return vaildResponse;
        }


        user.setRole(Const.Role.ROLE_CUSTOMER);

        // md5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);

        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");

    }

    /**
     * @Description: 校验逻辑
     * @Param: [str, type]
     * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
     * @Author: n1nE
     * @Date: 3/19/19
     */
    @Override
    public ServerResponse<String> chechVaild(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已注册");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }


    /**
    * @Description: 安全问题
    * @Param: [username]
    * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
    * @Author: n1nE
    * @Date: 3/21/19
    */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse vaildResponse = this.chechVaild(username,Const.USERNAME);
        if (vaildResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("未设置安全问题");
    }

    /**
    * @Description: 检验问题答案
    * @Param: [username, question, answer]
    * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
    * @Author: n1nE
    * @Date: 3/21/19
    */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        // TODO: 3/19/19  
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    /**
    * @Description: 重设密码
    * @Param: [username, passwordNew, forgetToken]
    * @return: com.n1ne.mmall.common.ServerResponse<java.lang.String>
    * @Author: n1nE
    * @Date: 3/21/19
    */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }

        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);

        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        if (StringUtils.equals(token,forgetToken)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,passwordNew);
            if (rowCount > 0) {
                ServerResponse.createBySuccessMessage("密码重置成功");
            }
        }else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }


    public ServerResponse<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }



    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);

    }

    public ServerResponse checkAdminRole(User user){
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
