package ch.so.agi.sodata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.so.agi.sodata.search.InvalidLuceneQueryException;
import ch.so.agi.sodata.search.LuceneSearcher;
import ch.so.agi.sodata.search.LuceneSearcherException;
import ch.so.agi.sodata.search.Result;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${lucene.queryDefaultRecords}")
    private Integer QUERY_DEFAULT_RECORDS;

    @Value("${lucene.queryMaxRecords}")
    private Integer QUERY_MAX_RECORDS;   

    @Autowired
    Settings settings;
    
    @Autowired
    private AppProperties config; 
    
    @Autowired
    private LuceneSearcher indexSearcher;
    
    private Map<String, Dataset> datasetMap;
    
    @PostConstruct
    public void init() throws Exception {        
        datasetMap = new HashMap<String, Dataset>();
        for (Dataset dataset : config.getDatasets()) {
            // Mir ist nicht ganz klar, warum statische Resourcen, die im public-Ordner liegen
            // auch via subunits-Pfad sichtbar sind. 
            if (dataset.getSubunitsBase64() != null) {
                String tmpdir = System.getProperty("java.io.tmpdir");
                String filename = dataset.getId();
                File subunitFile = Paths.get(tmpdir, filename + ".json").toFile();
                try (FileOutputStream fos = new FileOutputStream(subunitFile); ) {
                    String b64 = dataset.getSubunitsBase64();
                    byte[] decoder = Base64.getDecoder().decode(b64);
                    fos.write(decoder);                    
                  } catch (IOException e) {
                    e.printStackTrace();
                    throw new Exception(e);
                  }
                dataset.setSubunits(filename + ".json");
                dataset.setSubunitsBase64(null); // Soll nicht in den Client transferiert werden.
            }            
            datasetMap.put(dataset.getId(), dataset);            
        }
    }
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("sodata", HttpStatus.OK);
    }

    @GetMapping("/datasets")
    public List<Dataset> searchDatasets(@RequestParam(value="query", required=false) String queryString) {
        if (queryString == null) {
            return config.getDatasets();

        } else {
            Result results = null;
            try {
                results = indexSearcher.searchIndex(queryString, QUERY_DEFAULT_RECORDS, false);
                log.info("Search for '" + queryString +"' found " + results.getAvailable() + " and retrieved " + results.getRetrieved() + " records");            
            } catch (LuceneSearcherException | InvalidLuceneQueryException e) {
                throw new IllegalStateException(e);
            }

            List<Map<String, String>> records = results.getRecords();
            List<Dataset> resultList = records.stream()
                    .map(r -> {
                        return datasetMap.get(r.get("id"));
                    })
                    .collect(Collectors.toList());
            return resultList;
        }
    }
}
