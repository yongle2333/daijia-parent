package com.atguigu.daijia.order.service.impl;

import com.atguigu.daijia.model.entity.order.OrderInfo;
import com.atguigu.daijia.model.entity.order.OrderStatusLog;
import com.atguigu.daijia.model.enums.OrderStatus;
import com.atguigu.daijia.model.form.order.OrderInfoForm;
import com.atguigu.daijia.order.mapper.OrderInfoMapper;
import com.atguigu.daijia.order.mapper.OrderStatusLogMapper;
import com.atguigu.daijia.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {


    private final OrderInfoMapper orderInfoMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;


    //乘客下单(插入数据)
    @Override
    public Long saveOrderInfo(OrderInfoForm orderInfoForm) {
        //向orderInfo表中添加数据
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoForm,orderInfo);

        //订单号
        String orderNo = UUID.randomUUID().toString().replaceAll("-","");
        orderInfo.setOrderNo(orderNo);
        //订单状态(等待接单)
        orderInfo.setStatus(OrderStatus.WAITING_ACCEPT.getStatus());
        orderInfoMapper.insert(orderInfo);

        //订单状态日志记录
        this.log(orderInfo.getId(),orderInfo.getStatus());

        //TODO 接单标识

        //返回订单id
        return orderInfo.getId();
    }

    //订单状态日志添加
    public void log(Long orderId, Integer status){
        OrderStatusLog orderStatusLog = new OrderStatusLog();
        orderStatusLog.setOrderId(orderId);
        orderStatusLog.setOrderStatus(status);
        orderStatusLog.setOperateTime(new Date());
        orderStatusLogMapper.insert(orderStatusLog);
    }



    //根据订单id查询订单状态
    @Override
    public Integer getOrderStatus(Long orderId) {

        //可以这样来查询new LambdaQueryWrapper<>()，
        //也可以直接selectById(),只不过这样的全表查询索引失效影响性能
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if(orderInfo == null){
            return OrderStatus.NULL_ORDER.getStatus();
        }

        return orderInfo.getStatus();
    }
}
