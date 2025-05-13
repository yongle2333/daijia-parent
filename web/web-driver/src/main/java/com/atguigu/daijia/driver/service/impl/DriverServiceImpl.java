package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.login.GuiguLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.dispatch.client.NewOrderFeignClient;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.map.client.LocationFeignClient;
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
    private final LocationFeignClient locationFeignClient;
    private final NewOrderFeignClient newOrderFeignClient;


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


    //判断司机当日是否进行过人脸识别
    @Override
    public Boolean isFaceRecognition(Long driverId) {
        Result<Boolean> result = driverInfoFeignClient.isFaceRecognition(driverId);
        return result.getData();
    }


    //验证司机人脸
    @Override
    public Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm) {
        Result<Boolean> result = driverInfoFeignClient.verifyDriverFace(driverFaceModelForm);
        return result.getData();
    }


    //开始接单服务
    @Override
    public Boolean startService(Long driverId) {
        //判断是否完成了认证
        DriverLoginVo driverLoginVo = driverInfoFeignClient.getDriverLoginInfo(driverId).getData();

        if(driverLoginVo.getAuthStatus() != 2){
            throw new GuiguException(ResultCodeEnum.AUTH_ERROR);
        }
        //判断当日是否完成了人脸识别
        Boolean isFace = driverInfoFeignClient.isFaceRecognition(driverId).getData();
        if(!isFace){
            throw new GuiguException(ResultCodeEnum.FACE_ERROR);
        }
        //更新订单状态 1 ，开始接单
        driverInfoFeignClient.updateServiceStatus(driverId,1);
        //删除redis司机位置信息
        locationFeignClient.removeDriverLocation(driverId);
        //清除司机临时队列的订单数据
        newOrderFeignClient.clearNewOrderQueueData(driverId);
        return true;
    }


    //停止接单服务
    @Override
    public Boolean stopService(Long driverId) {
        //更新接单状态为 0
        driverInfoFeignClient.updateServiceStatus(driverId,0);
        //删除司机位置信息
        locationFeignClient.removeDriverLocation(driverId);
        //清空临时队列数据
        newOrderFeignClient.clearNewOrderQueueData(driverId);
        return true;
    }
}
