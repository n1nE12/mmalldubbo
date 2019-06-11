package com.n1ne.mmall.service;

import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.pojo.User;

public interface IUserService {

    void delete(Integer integer);

    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> chechVaild(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> getInformation(Integer id);

    public ServerResponse checkAdminRole(User user);
}
