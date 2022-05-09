package ch.so.agi.sodata;

import java.io.File;
import java.nio.file.Files;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        // File.seperator wird benötigt, weil tmpdir im Dockerimage diesen im Gegensatz zu macOS
        // weglässt (auch wenn man TMPDIR=/tmp/ explizit setzt) und Spring Boot diesen bei einer
        // Verzeichnisangabe explizit verlangt.
        registry.addResourceHandler("/subunits/*.json").addResourceLocations("file:"+tmpdir+File.separator);
    }
}
