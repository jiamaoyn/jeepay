package com.jeetask.telegram.bootstrap;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.jeetask.telegram.config.SystemYmlConfig;
import org.hibernate.validator.HibernateValidator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;

/**
 * @Author terrfly
 * @Date 2019/11/7 15:19
 * @Description spring-boot 主启动程序
 **/
@SpringBootApplication
@EnableScheduling
@MapperScan("com.jeequan.jeepay.service.mapper")    //Mybatis mapper接口路径
@ComponentScan(basePackages = "com.jeequan.jeepay.*")   //由于MainApplication没有在项目根目录， 需要配置basePackages属性使得成功扫描所有Spring组件；
@ComponentScan(basePackages = "com.jeetask.telegram.*")   //由于MainApplication没有在项目根目录， 需要配置basePackages属性使得成功扫描所有Spring组件；
@Configuration
public class JeepayTelegramApplication {

    @Autowired
    private SystemYmlConfig systemYmlConfig;

    /**
     * main启动函数
     **/
    public static void main(String[] args) {

        //启动项目
        SpringApplication.run(JeepayTelegramApplication.class, args);

    }


    /**
     * fastJson 配置信息
     **/
    @Bean
    public HttpMessageConverters fastJsonConfig() {

        //新建fast-json转换器
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        //fast-json 配置信息
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        converter.setFastJsonConfig(config);

        //设置响应的 Content-Type
        converter.setSupportedMediaTypes(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8}));
        return new HttpMessageConverters(converter);
    }

    /**
     * Mybatis plus 分页插件
     **/
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //分页插件
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return mybatisPlusInterceptor;
    }

    /**
     * 默认为 失败快速返回模式
     **/
    @Bean
    public Validator validator() {

        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .failFast(true)
                .buildValidatorFactory();
        return validatorFactory.getValidator();
    }

    /**
     * 允许跨域请求
     **/
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        if (systemYmlConfig.getAllowCors()) {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);   //带上cookie信息
//          config.addAllowedOrigin(CorsConfiguration.ALL);  //允许跨域的域名， *表示允许任何域名使用
            config.addAllowedOriginPattern(CorsConfiguration.ALL);  //使用addAllowedOriginPattern 避免出现 When allowCredentials is true, allowedOrigins cannot contain the special value "*" since that cannot be set on the "Access-Control-Allow-Origin" response header. To allow credentials to a set of origins, list them explicitly or consider using "allowedOriginPatterns" instead.
            config.addAllowedHeader(CorsConfiguration.ALL);   //允许任何请求头
            config.addAllowedMethod(CorsConfiguration.ALL);   //允许任何方法（post、get等）
            source.registerCorsConfiguration("/**", config); // CORS 配置对所有接口都有效
        }
        return new CorsFilter(source);
    }

}
