package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.login.GuiguLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.PasswordRecipientId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverServiceImpl implements DriverService {


    private final DriverInfoFeignClient driverInfoFeignClient;
    private final RedisTemplate redisTemplate;


    //小程序授权登录
    @Override
    public String login(String code) {
        //通过code进行远程调用，获得司机id
        Result<Long> longResult = driverInfoFeignClient.login(code);
        if(longResult.getCode() != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        Long driverId = longResult.getData();
        if(null == driverId){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //根据司机id得到token
        String token = UUID.randomUUID().toString().replaceAll("-","");
        //将司机id和token 存入redis,并设置过期时间
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token,
                driverId.toString(),
                RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.MINUTES);
        return token;
    }


    //获取司机登录信息
    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        //根据id远程调用获取司机登录信息
        Result<DriverLoginVo> loginVoResult = driverInfoFeignClient.getDriverLoginInfo(driverId);
        return loginVoResult.getData();
    }

    //获取司机认证信息
    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        Result<DriverAuthInfoVo> authInfoVoResult = driverInfoFeignClient.getDriverAuthInfo(driverId);
        return authInfoVoResult.getData();
    }

    //更新司机认证信息
    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        Result<Boolean> booleanResult = driverInfoFeignClient.updateDriverAuthInfo(updateDriverAuthInfoForm);
        return booleanResult.getData();
    }


    //创建司机人脸模型
    @Override
    public Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        Result<Boolean> booleanResult = driverInfoFeignClient.creatDriverFaceModel(driverFaceModelForm);
        return booleanResult.getData();
    }
}
