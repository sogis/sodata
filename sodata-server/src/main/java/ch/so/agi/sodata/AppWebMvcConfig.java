package ch.so.agi.sodata;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppWebMvcConfig implements WebMvcConfigurer {
    @Value("${app.itemsGeoJsonDir}")
    private String itemsGeoJsonDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // File.seperator wird benötigt, weil tmpdir im Dockerimage diesen im Gegensatz zu macOS
        // weglässt (auch wenn man TMPDIR=/tmp/ explizit setzt) und Spring Boot diesen bei einer
        // Verzeichnisangabe explizit verlangt.
        registry.addResourceHandler("/subunits/*.json").addResourceLocations("file:"+itemsGeoJsonDir+File.separator);
    }
}
