package com.jeequan.jeepay.mgr.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * webmvc配置
 * @date 2021/6/8 17:12
 */
@Configuration
public class WebmvcConfig implements WebMvcConfigurer {

    @Autowired
    private ApiResInterceptor apiResInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiResInterceptor);
    }
}
