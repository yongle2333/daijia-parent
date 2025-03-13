package com.atguigu.daijia.driver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author qiu
 * @version 1.0
 */
@Component
@Data
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxConfigProperties {
    private String appId;
    private String secret;
}
