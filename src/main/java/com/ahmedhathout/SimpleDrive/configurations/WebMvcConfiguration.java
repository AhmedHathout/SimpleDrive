package com.ahmedhathout.SimpleDrive.configurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = {"com.ahmedhathout.SimpleDrive.controller"})
public class WebMvcConfiguration implements WebMvcConfigurer {

    /**
     * Adds some basic views that do not need to be present in a controller
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/about").setViewName("about");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/logout").setViewName("homepage");
    }


    /**
     * Without this method, anything in the resources besides the templates will not be read by the app.
     * @see <a href="https://memorynotfound.com/adding-static-resources-css-javascript-images-thymeleaf"></a>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/webjars/**",
                "/img/**",
                "/css/**",
                "/js/**")
                .addResourceLocations(
                        "classpath:/META-INF/resources/webjars/",
                        "classpath:/static/img/",
                        "classpath:/static/css/",
                        "classpath:/static/js/");
    }

}
