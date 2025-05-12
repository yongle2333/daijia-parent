package com.atguigu.daijia.dispatch.xxl.job;

import com.atguigu.daijia.dispatch.mapper.XxlJobLogMapper;
import com.atguigu.daijia.dispatch.service.NewOrderService;
import com.atguigu.daijia.model.entity.dispatch.XxlJobLog;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import groovyjarjarpicocli.CommandLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author qiu
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JobHandler {

    private final XxlJobLogMapper xxlJobLogMapper;
    private final NewOrderService newOrderService;

    @XxlJob("newOrderTaskHandler")
    public void newOrderTaskHandler(){

        //记录任务调度日志
        XxlJobLog xxlJobLog = new XxlJobLog();
        xxlJobLog.setJobId(XxlJobHelper.getJobId()); //获得当前执行任务的id
        long startTime = System.currentTimeMillis();

        try{
            //搜索附近代驾司机
            newOrderService.executeTask(XxlJobHelper.getJobId());

            //成功状态
            xxlJobLog.setStatus(1);
        } catch (Exception e){

            //失败状态
            xxlJobLog.setStatus(0);
            xxlJobLog.setError(e.getMessage());
            e.printStackTrace();
        } finally {
            long time = System.currentTimeMillis() - startTime;
            xxlJobLog.setTimes(time);
            xxlJobLogMapper.insert(xxlJobLog);
        }

    }

}
