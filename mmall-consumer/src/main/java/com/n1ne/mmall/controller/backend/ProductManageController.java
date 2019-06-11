package com.n1ne.mmall.controller.backend;

import com.alibaba.dubbo.config.annotation.Reference;
import com.google.common.collect.Maps;
import com.n1ne.mmall.common.Const;
import com.n1ne.mmall.common.ResponseCode;
import com.n1ne.mmall.common.ServerResponse;
import com.n1ne.mmall.pojo.Product;
import com.n1ne.mmall.pojo.User;
import com.n1ne.mmall.service.IFileService;
import com.n1ne.mmall.service.IProductService;
import com.n1ne.mmall.service.IUserService;
import com.n1ne.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @program: mmall
 * @description:
 * @author: n1ne
 * @create: 2019-04-01 16:03
 **/

@RestController
@RequestMapping("/manage/product")
public class ProductManageController {


    @Reference
    private IUserService iUserService;

    @Reference
    private IProductService iProductService;

    @Reference
    private IFileService iFileService;

    
    /** 
    * @Description:
    * @Param: [session, product] 
    * @return: com.n1ne.mmall.common.ServerResponse 
    * @Author: n1nE
    * @Date: 4/4/19 
    */ 
    @RequestMapping(value = "save.do")
    public ServerResponse productSave(HttpSession session, Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.saveOrUpdateProduct(product);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");

    }

    
    /** 
    * @Description:  
    * @Param: [session, productId, status] 
    * @return: com.n1ne.mmall.common.ServerResponse 
    * @Author: n1nE
    * @Date: 4/4/19 
    */ 
    @RequestMapping(value = "set_sale_status.do")
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.setSaleStatus(productId, status);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");

    }

    /** 
    * @Description:  
    * @Param: [session, productId] 
    * @return: com.n1ne.mmall.common.ServerResponse 
    * @Author: n1nE
    * @Date: 4/4/19 
    */ 
    @RequestMapping(value = "detail.do")
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.manageProductDetail(productId);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");

    }

    /**
     * @Description: 后台获取产品列表
     * @Param: [session, pageNum, pageSize]
     * @return: com.n1ne.mmall.common.ServerResponse
     * @Author: n1nE
     * @Date: 4/2/19
     */
    @RequestMapping(value = "list.do")
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.getProductList(pageNum, pageSize);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");

    }

    /** 
    * @Description: 搜索 
    * @Param: [session, productName, productId, pageNum, pageSize] 
    * @return: com.n1ne.mmall.common.ServerResponse 
    * @Author: n1nE
    * @Date: 4/4/19 
    */ 
    @RequestMapping(value = "search.do")
    public ServerResponse searchProduct(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            iProductService.searchProduct(productName, productId, pageNum, pageSize);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");

    }
    
    /**
    * @Description: 文件上传
    * @Param: [session, productName, productId, pageNum, pageSize]
    * @return: com.n1ne.mmall.common.ServerResponse
    * @Author: n1nE
    * @Date: 4/4/19
    */
    @RequestMapping(value = "upload.do")
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file , HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName  = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix"+targetFileName);
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }
        return ServerResponse.createByErrorMessage("用户无权限，需要管理员权限");

    }

    /** 
    * @Description: 富文本上传 
    * @Param: [session, file, request, response] 
    * @return: java.util.Map 
    * @Author: n1nE
    * @Date: 4/7/19 
    */ 
    @RequestMapping("richtext_img_upload.do")
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap = Maps.newHashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录管理员");
            return resultMap;
        }

        if(iUserService.checkAdminRole(user).isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }

}
