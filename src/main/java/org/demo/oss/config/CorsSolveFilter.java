package org.demo.oss.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


/**
 * WebConfig类是一个配置类，用于配置跨域请求的过滤器，解决跨域问题。
 * @author moxiaoli
 */
@Configuration
public class CorsSolveFilter {

//   由于设置了spring security，所以这里的跨域配置就失效了，所以需要在SecurityConfig.java中配置跨域
//    @Bean
//    public CorsFilter corsFilter(){
//        // 1.添加CORS配置信息
//        CorsConfiguration corsConfiguration=new CorsConfiguration();
//        // 1) 允许的域,不要写*，否则cookie就无法使用了
//        corsConfiguration.addAllowedOrigin("*");
//        // 2) 是否发送Cookie信息
//        corsConfiguration.setAllowCredentials(true);
//        // 3) 允许的请求方式
//        corsConfiguration.addAllowedHeader("*");
//        // 4) 允许的头信息
//        corsConfiguration.addAllowedMethod("*");
//        // 5) 有效时长
//        corsConfiguration.setMaxAge(3600L);
//        // 2.添加映射路径，我们拦截一切请求
//        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
//        // 3.返回新的CorsFilter.
//        source.registerCorsConfiguration("/**",corsConfiguration);
//        return new CorsFilter(source);
//    }

    @Bean
    FilterRegistrationBean<CorsFilter> cors(){
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedOrigin("*");
        configuration.addExposedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);
        registrationBean.setFilter(new CorsFilter(source));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

}