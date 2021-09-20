package ch.so.agi.sodata;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        registry.addResourceHandler("/subunits/*.json").addResourceLocations("file:"+tmpdir);
    }
}
