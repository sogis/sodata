package ch.so.agi.sodata;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootApplication
@ServletComponentScan
@Configuration
public class Application extends SpringBootServletInitializer {
  
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
  
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }  
    
    @Bean 
    XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // TODO: wieder entfernen, wenn stabil

//        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        xmlMapper.registerModule(new JavaTimeModule());
        return xmlMapper; 
    }
    
    // Anwendung ist fertig gestartet.
    @Bean
    CommandLineRunner init(ConfigService configService) {
        return args -> {
            configService.readXml();
        };
    }

}
