package com.atguigu.daijia.dispatch.xxl.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * @author qiu
 * @version 1.0
 */
@Component
public class DispatchJobHandler {

    //要把调度中心启动，还有执行器启动（当前执行器,service-dispatch）
    @XxlJob("firstJobHandler")
    public void testJobHandler(){
        System.out.println("xxl-job项目集成测试");
    }
}
