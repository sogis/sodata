package ch.so.agi.sodata.repository;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import ch.so.agi.sodata.dto.ThemePublicationDTO;

@Repository
public class ThemePublicationRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init() throws IOException {
        log.info("prepare index...........");
    }
    
    public void save(ThemePublicationDTO themePublication) {
        log.info("save: " + themePublication.getIdentifier());
        
        // if index == null -> init index? Oder geht hier auch postconstruct? Wäre noch sinnvoll, denn das Abfüllen kommt
        // ja erst später mit dem CommandlineRunner
        
        
    }
    
    public List<ThemePublicationDTO> findByQuery(String searchTerms) {
        return null;
    }
}
