package com.atguigu.daijia.common.login;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.common.util.AuthContextHolder;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author qiu
 * @version 1.0
 */
@Component
@Aspect     //表示切面类
@RequiredArgsConstructor
public class GuiguLoginAspect {

    private final RedisTemplate redisTemplate;

    //加切入点表达式
    @Around("execution(* com.atguigu.daijia.*.controller.*.*(..)) && @annotation(guiguLogin)")
    public Object login(ProceedingJoinPoint proceedingJoinPoint, GuiguLogin guiguLogin) throws Throwable{

        //1.获取request对象
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) attributes;
        HttpServletRequest request = sra.getRequest();
        //2.从请求头获取token
        String token = request.getHeader("token");
        //3.判断token是否为空，为空返回登录提示
        if(!StringUtils.hasText(token)){
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        //4.token不为空则查询redis
        String customerId = (String) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        //5.查询redis中对应的用户id，把用户id存入ThreadLocal中
        if(StringUtils.hasText(customerId)){
            AuthContextHolder.setUserId(Long.parseLong(customerId));
        }
        //执行业务方法
        return proceedingJoinPoint.proceed();
    }
}
