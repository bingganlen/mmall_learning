package com.imooc.mall;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**\
 *
 * 拦截一些特定的URL 通过UserLoginIntercapter处理  拦截未登录访问这些接口
 *
 * addPathPatterns("/**")  默认所有接口都拦截
 * excludePathPatterns 除了下面这些接口
 */
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginIntercapter())
                .addPathPatterns("/**")
                .excludePathPatterns("/error","/user/login", "/user/register", "/categories", "/products", "/products/*");
    }
}
