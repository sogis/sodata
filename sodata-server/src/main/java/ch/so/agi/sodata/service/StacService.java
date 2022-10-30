package ch.so.agi.sodata.service;

import java.nio.file.Paths;

import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StacService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void foo() {
        log.info("foo");
        
        String VENV_EXECUTABLE = StacService.class.getClassLoader().getResource(Paths.get("venv", "bin", "graalpy").toString()).getPath();

        
        Context context = Context.create();
                
//        Context context = Context.newBuilder("python")
//                .allowAllAccess(true)
//                .option("python.Executable", VENV_EXECUTABLE)
//                .option("python.ForceImportSite", "true")
//                // .engine(engine)
//                .build();
    }
}
