package com.atguigu.daijia.driver.controller;

import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.atguigu.daijia.common.login.GuiguLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.LocationService;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.map.UpdateDriverLocationForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RelationSupport;

@Slf4j
@Tag(name = "位置API接口管理")
@RestController
@RequiredArgsConstructor
@RequestMapping(value="/location")
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationController {

    private final LocationService locationService;



    @Operation(summary = "开启接单服务：更新司机经纬度位置")
    @GuiguLogin
    @PostMapping("/updateDriverLocation")
    public Result<Boolean> updateDriverLocation(@RequestBody UpdateDriverLocationForm updateDriverLocationForm){
        Long driverId = AuthContextHolder.getUserId();
        updateDriverLocationForm.setDriverId(driverId);
        return Result.ok(locationService.updateDriverLocation(updateDriverLocationForm));

    }





}

