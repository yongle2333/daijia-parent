package com.atguigu.daijia.map.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.map.service.MapService;
import com.atguigu.daijia.model.form.map.CalculateDrivingLineForm;
import com.atguigu.daijia.model.vo.map.DrivingLineVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RefreshScope   //支持动态刷新配置文件
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class MapServiceImpl implements MapService {
    private final RestTemplate restTemplate;


    //计算驾驶路线
    @Value("${tencent.map.key}")
    private String key;
    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        //请求腾讯的接口
        //使用HttpClient，或者Spring封装的工具RestTemplate
        //定义要调用的腾讯地址
        String url = "https://apis.map.qq.com/ws/direction/v1/driving/?from={from}&to={to}&key={key}";
        //封装传递参数
        Map<String,String> map = new HashMap<>();
        //开始位置
        map.put("from",calculateDrivingLineForm.getStartPointLatitude()+","+calculateDrivingLineForm.getStartPointLongitude());
        //结束位置
        map.put("to",calculateDrivingLineForm.getEndPointLatitude()+","+calculateDrivingLineForm.getEndPointLongitude());
        map.put("key",key);

        //使用RestTemplate调用GET
        JSONObject result = restTemplate.getForObject(url, JSONObject.class, map);
        //处理返回结果
        //判断是否调用成功
        int status = result.getIntValue("status");  //调用getIntValue可能会产生空指针的异常
        if(status != 0){
            throw new GuiguException(ResultCodeEnum.MAP_FAIL);
        }

        //获取返回路线信息
        JSONObject route =
                result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
        DrivingLineVo drivingLineVo = new DrivingLineVo();
        //方案预估时间
        drivingLineVo.setDuration(route.getBigDecimal("duration"));
        //距离 向上取整，保留两位小数
        drivingLineVo.setDistance(route.getBigDecimal("distance")
                .divide(new BigDecimal(1000))   //**************
                .setScale(2, RoundingMode.HALF_UP));
        //路线
        drivingLineVo.setPolyline(route.getJSONArray("polyline"));

        return drivingLineVo;
    }
}














