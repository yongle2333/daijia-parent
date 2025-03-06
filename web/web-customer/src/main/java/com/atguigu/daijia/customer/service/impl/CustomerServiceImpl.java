package com.atguigu.daijia.customer.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.customer.client.CustomerInfoFeignClient;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {


    //注入远程调用
    private final CustomerInfoFeignClient client;

    private final RedisTemplate redisTemplate;

    @Override
    public String login(String code) {
        //1.通过code 进行远程调用，返回用户id
        Result<Long> loginResult = client.login(code);
        //2.判断如果返回失败了，返回错误提示
        if (loginResult.getCode() != 200) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //3.获取远程调用返回用户id
        Long customerId = loginResult.getData();
        //4.判断返回的用户id为空,为空，则返回错误提示
        if(null == customerId){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //5.生成token字符串
        //默认uuid是4354jvf-52gv-65431jnv之类的
        String token = UUID.randomUUID().toString().replaceAll("-","");
        //6.把用户id放入到Redis中，设置过期时间
        //key:token value:customerId
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX+token,
                customerId.toString(),
                RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.MINUTES);
        //7.返回token,存入前端

        return token;
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        //1.从请求头获取token字符串
        //方法二：@RequsetHeader(value = "token) String token
        //String token = request.getHeader("token");
        //2.根据token查询redis
        //3.查询token在redis中的对应用户id
//        String customerId = (String) redisTemplate.opsForValue()
//                .get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
//        if(!StringUtils.hasText(customerId)){
//            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
//        }

        //******直接从ThreadLocal中获取用户id

        //4.根据用户id进行远程调用,得到用户信息
        Result<CustomerLoginVo> customerLoginVoResult =
                client.getCustomerLoginInfo(customerId);
        Integer code = customerLoginVoResult.getCode();
        if(code != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        CustomerLoginVo customerLoginVo = customerLoginVoResult.getData();
        if(null == customerLoginVo){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //5.返回用户信息
        return customerLoginVo;
    }


    //更新用户微信手机号
    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        Result<Boolean> booleanResult = client.updateWxPhoneNumber(updateWxPhoneForm);
        return true;
    }
}
