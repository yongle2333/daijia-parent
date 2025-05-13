package com.atguigu.daijia.map.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.constant.SystemConstant;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.map.service.LocationService;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.map.SearchNearByDriverForm;
import com.atguigu.daijia.model.form.map.UpdateDriverLocationForm;
import com.atguigu.daijia.model.vo.map.NearByDriverVo;
import io.lettuce.core.api.async.RedisGeoAsyncCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {

    private final RedisTemplate redisTemplate;
    private final DriverInfoFeignClient driverInfoFeignClient;


    //开启接单服务：更新司机经纬度位置
    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        //把司机的位置信息添加到redis中的geo中    //geo基于zset命令实现的,搜索是log(n)
        Point point = new Point(updateDriverLocationForm.getLongitude().doubleValue(),
                updateDriverLocationForm.getLatitude().doubleValue());
        redisTemplate.opsForGeo().add(RedisConstant.DRIVER_GEO_LOCATION,point,
                updateDriverLocationForm.getDriverId().toString());
        return true;

    }




    //关闭接单服务：删除司机经纬度位置
    @Override
    public Boolean removeDriverLocation(Long driverId) {
        redisTemplate.opsForGeo().remove(RedisConstant.DRIVER_GEO_LOCATION,driverId.toString());
        return true;
    }


    //搜索附近满足条件的司机
    @Override
    public List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm) {
        //搜索经纬度5公里内的司机
        //1.操作redis中的geo

        //创建point对象
        Point point = new Point(searchNearByDriverForm.getLongitude().doubleValue(),
                searchNearByDriverForm.getLatitude().doubleValue());
        //定义距离
        Distance distance = new Distance(SystemConstant.NEARBY_DRIVER_RADIUS,
                                RedisGeoCommands.DistanceUnit.KILOMETERS);

        //创建circle对象，传入point,distance两个对象
        Circle circle = new Circle(point,distance);

        //定义GEO参数,设置返回结果包含的内容
        RedisGeoCommands.GeoRadiusCommandArgs args =
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()      //包含距离
                        .includeCoordinates()   //包含坐标
                        .sortAscending();       //升序

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(RedisConstant.DRIVER_GEO_LOCATION, circle, args);

        //2.查询redis返回的list集合
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = results.getContent();
        //3.对list集合进行处理
        //遍历list集合，获取每个司机的信息
        //根据司机的个性化信息进行判断
        //可以优化一下，一次性查出，不用遍历去查，减少数据库连接开销，用stream剔除,提高系统并发性能(之后了解)

        //*****************优化优化优化********************
        /*
            第二次优化，直接一步到位
         */
        List<Long> driverIds = content.stream()
                .map(item -> Long.parseLong(item.getContent().getName()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(driverIds)){
            return Collections.emptyList();
        }

        //批量查询司机设置信息
        Result<Map<Long, DriverSet>> batchDriverSetsResult = driverInfoFeignClient.batchGetDriverSets(driverIds);
        Map<Long, DriverSet> driverSetMap = batchDriverSetsResult.getData();

        return content.stream()
                .map(geoResult -> {
                    //获取司机id
                    Long driverId = Long.parseLong(geoResult.getContent().getName());

                    //批量查询获取司机设置信息
                    DriverSet driverSet = driverSetMap.get(driverId);
                    if(driverSet == null){
                        return null;
                    }

                    //计算当前司机与用户的距离
                    BigDecimal currnetDistance = BigDecimal.valueOf(geoResult.getDistance().getValue()).setScale(2,RoundingMode.HALF_UP);

                    //接单条件一：订单里程order_distance 司机接单里程 >= 用户需求里程
                    boolean isOrderValid =
                            driverSet.getOrderDistance().doubleValue() == 0
                         || driverSet.getOrderDistance().compareTo(searchNearByDriverForm.getMileageDistance()) >= 0;

                    boolean isAcceptValid =
                            driverSet.getAcceptDistance().doubleValue() == 0
                         || driverSet.getAcceptDistance().compareTo(currnetDistance) >= 0;

                    // 同时满足两个条件才返回有效结果
                    if (isOrderValid && isAcceptValid) {
                        NearByDriverVo vo = new NearByDriverVo();
                        vo.setDriverId(driverId);
                        vo.setDistance(currnetDistance);
                        return vo;
                    }
                    return null;

                })
                .filter(Objects::nonNull)   //过滤不满足条件的司机
                .collect(Collectors.toList());



        /*
            第一次优化

//        List<Long> driverIds = content.stream()
//                .map(item -> Long.parseLong(item.getContent().getName()))
//                .collect(Collectors.toList());

        //批量查询司机设置信息
        Result<Map<Long, DriverSet>> batchDriverSetsResult = driverInfoFeignClient.batchGetDriverSets(driverIds);
        Map<Long, DriverSet> driverSetMap = batchDriverSetsResult.getData();

         */
        /*第一次用迭代器（低性能）
//        List<NearByDriverVo> list = new ArrayList<>();
//        if(!CollectionUtils.isEmpty(content)){
//            //用迭代器
//            Iterator<GeoResult<RedisGeoCommands.GeoLocation<String>>> iterator = content.iterator();
//            while (iterator.hasNext()){
//                //迭代器中的每部分
//                GeoResult<RedisGeoCommands.GeoLocation<String>> item = iterator.next();
//                Long driverId = Long.parseLong(item.getContent().getName());
//                //远程调用，获取司机的个性化设置信息
//                Result<DriverSet> driverSetResult = driverInfoFeignClient.getDriverSet(driverId);
//                DriverSet driverSet = driverSetResult.getData();
//
//                //判断订单里程order_distance (可以接代驾的里程)
//                BigDecimal orderDistance = driverSet.getOrderDistance();
//                //orderDistance == 0 ,司机没有限制的,多远距离都接
//                //orderDistance == 30 ,司机接单30公里的代驾
//                //接单距离(司机个性化设置) - 当前单子(用户订单) < 0,不符合条件
//                if(orderDistance.doubleValue() != 0
//                        && orderDistance.subtract(searchNearByDriverForm.getMileageDistance()).doubleValue() < 0){
//                    continue;
//                }
//
//                //判断接单里程accept_distance (司机距离乘客开始位置的距离)
//                //当前接单距离
//                double value = item.getDistance().getValue();
//                BigDecimal currnetDistance = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
//                BigDecimal acceptDistance = driverSet.getAcceptDistance();
//                if(acceptDistance.doubleValue() != 0
//                        && acceptDistance.subtract(currnetDistance).doubleValue() < 0){
//                    continue;
//                }
//
//                //封装符合条件的数据
//                NearByDriverVo nearByDriverVo = new NearByDriverVo();
//                nearByDriverVo.setDriverId(driverId);
//                nearByDriverVo.setDistance(currnetDistance);
//                list.add(nearByDriverVo);
//            }
//        }
//        //如果要改，那就要加一个批量查询的接口，接口最好返回一个map，方便本地循环获取信息
//        //或者在循环李收集好司机id,然后远程调用批量获取司机设置，再遍历筛选
//
//        return list;

         */
    }
}
