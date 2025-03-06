package com.atguigu.daijia.customer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author qiu
 * @version 1.0
 */
@ConfigurationProperties(prefix = "wx.miniapp") //配置文件中的前缀
@Component
@Data
public class WxConfigProperties {

    private String appId;
    private String secret;

}
