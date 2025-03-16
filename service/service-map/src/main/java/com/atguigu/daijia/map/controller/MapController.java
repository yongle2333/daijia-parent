package com.atguigu.daijia.map.controller;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.map.service.MapService;
import com.atguigu.daijia.model.form.map.CalculateDrivingLineForm;
import com.atguigu.daijia.model.vo.map.DrivingLineVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "地图API接口管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/map")
@SuppressWarnings({"unchecked", "rawtypes"})
public class MapController {

    private final MapService mapService;



    @Operation(summary = "计算驾驶路线")
    @PostMapping("/calculateDrivingLine")
    public Result<DrivingLineVo> calculateDrivingLine(@RequestBody CalculateDrivingLineForm calculateDrivingLineForm){

        return Result.ok(mapService.calculateDrivingLine(calculateDrivingLineForm));
    }
}

