 package ch.so.agi.sodata;

import java.nio.file.Paths;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import ch.so.agi.sodata.service.ConfigService;
import ch.so.agi.sodata.service.StacService;

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
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    } 
        
    // see: https://blogs.oracle.com/javamagazine/post/java-graalvm-polyglot-python-r
    // Relevant für mich?
//    @Bean
//    Engine createEngine() {
//        return Engine.newBuilder().build();
//    }

//    @Bean
//    Context createContext(/*Engine engine*/) {
//        String VENV_EXECUTABLE = Application.class.getClassLoader().getResource(Paths.get("venv", "bin", "graalpy").toString()).getPath();
//
//        return Context.newBuilder("python")
//                .allowAllAccess(true)
//                .option("python.Executable", VENV_EXECUTABLE)
//                .option("python.ForceImportSite", "true")
//                //.engine(engine)
//                .build();
//
//    }
    
    
    
    
    // Anwendung ist fertig gestartet. 
    // Kubernetes: Live aber nicht ready.
    // Importieren der Konfiguration. D.h. der XML-Datei mit den vorhandenen
    // Themapublikationen (aka Datensätzen).
    @Bean
    CommandLineRunner init(ConfigService configService) {
        return args -> {
            configService.readXml();
            
            // Testeshalber stac hier.  
//            StacService stacService = new StacService();
//            stacService.foo();

            
            System.out.println(configService.getThemePublicationList().size());
        };
    }
}
