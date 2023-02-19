package ch.so.agi.sodata;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.ForwardedHeaderFilter;

import ch.so.agi.sodata.service.ConfigService;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
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
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    } 
    
    @Bean 
    HttpClient createHttpClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .build();
        return httpClient;
    }
        
    // Anwendung ist fertig gestartet: live aber nicht ready.
    // Importieren der Konfiguration. D.h. der XML-Datei mit den vorhandenen
    // Themapublikationen (aka Datensätzen).
    @Bean
    CommandLineRunner init(ConfigService configService) {
        return args -> {
            configService.readXml();
        };
    }
}
