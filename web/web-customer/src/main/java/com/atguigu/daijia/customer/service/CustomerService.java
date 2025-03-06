package com.atguigu.daijia.customer.service;

import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import jakarta.servlet.http.HttpServletRequest;

public interface CustomerService {


    //微信小程序登录
    String login(String code);



    //获取用户登录的信息
    CustomerLoginVo getCustomerLoginInfo(Long customerId);

    //更新用户微信手机号
    Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm);
}
