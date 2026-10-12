package com.lyk.coursearrange.config;

import com.lyk.coursearrange.common.AuthenticationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * @author: 15760
 * @Date: 2020/3/24
 * @Descripe: 拦截请求
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 暂时禁用拦截器进行调试
        // registry.addInterceptor(authenticationInterceptor())
        //         .addPathPatterns("/**");    // 拦截所有请求，通过判断是否有 @LoginRequired 注解决定是否需要登录
    }

    /**
     * 配置静态资源映射
     * 将上传的文件映射到URL路径，使其可以通过HTTP访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录
        String projectPath = System.getProperty("user.dir");
        String uploadPath = projectPath + File.separator + "uploads" + File.separator;
        
        // 将 /uploads/** 路径映射到本地上传目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        
        System.out.println("静态资源映射配置成功，上传目录: " + uploadPath);
    }

}
