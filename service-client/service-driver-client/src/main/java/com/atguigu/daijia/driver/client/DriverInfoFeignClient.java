package com.atguigu.daijia.driver.client;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(value = "service-driver")
public interface DriverInfoFeignClient {

    //小程序授权登录
    @GetMapping("/driver/info/login/{code}")
    Result<Long> login(@PathVariable("code") String code);

    //获取司机的登录信息
    @GetMapping("/driver/info/getDriverLoginInfo/{driverId}")
    Result<DriverLoginVo> getDriverLoginInfo(@PathVariable("driverId") Long driverId);

    //获取司机认证信息
    @GetMapping("/driver/info/getDriverAuthInfo/{driverId}")
    Result<DriverAuthInfoVo> getDriverAuthInfo(@PathVariable("driverId") Long driverId);

    //更新司机认证信息
    @PostMapping("/driver/info/updateDriverAuthInfo")
    Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm);

    //创建司机人脸模型
    @PostMapping("/driver/info/creatDriverFaceModel")
    Result<Boolean> creatDriverFaceModel(@RequestBody DriverFaceModelForm driverFaceModelForm);

    //获取司机的个性化设置信息
    @GetMapping("/driver/info/getDriverSet/{driverId}")
    Result<DriverSet> getDriverSet(@PathVariable Long driverId);

    //批量获取司机个性设置信息***
    @PostMapping("/driver/info/batchGetDriverSets")
    Result<Map<Long,DriverSet>> batchGetDriverSets(@RequestBody List<Long> driverIds);


    //判断司机当日是否进行过人脸识别
    @GetMapping("/driver/info/isFaceRecognition/{driverId}")
    Result<Boolean> isFaceRecognition(@PathVariable("driverId") Long driverId);


    //验证司机人脸
    @PostMapping("/driver/info/verifyDriverFace")
    Result<Boolean> verifyDriverFace(@RequestBody DriverFaceModelForm driverFaceModelForm);



    @GetMapping("/driver/info/updateServiceStatus/{driverId}/{status}")
    Result<Boolean> updateServiceStatus(@PathVariable Long driverId,@PathVariable Integer status);
}
















