package ch.so.agi.sodata.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.so.agi.sodata.AppProperties;
import ch.so.agi.sodata.Dataset;
import ch.so.agi.sodata.Settings;
import ch.so.agi.sodata.search.InvalidLuceneQueryException;
import ch.so.agi.sodata.search.LuceneSearcherV1_0;
import ch.so.agi.sodata.search.LuceneSearcherException;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${lucene.queryMaxRecords}")
    private Integer QUERY_MAX_RECORDS;   

    @Autowired
    Settings settings;
    
    @Autowired
    private AppProperties config; 
    
    @Autowired
    private LuceneSearcherV1_0 indexSearcher;
    
    @Autowired
    ObjectMapper objectMapper;

    private Map<String, Dataset> datasetMap;
    
    // TODO
    
    // Absoluter Pfad zu themepublicatins.xml in application.properties als ENV mit Default.
    // XmlThemePublication -> GwtThemePublication Mapper
    
    // Eigenes Properties-Package falls mehrere Klassen? 
    
    @PostConstruct
    public void init() throws Exception {        
        datasetMap = new HashMap<String, Dataset>();
        for (Dataset dataset : config.getDatasets()) {
            String tmpdir = System.getProperty("java.io.tmpdir");
            String filename = dataset.getId();
            File subunitFile = Paths.get(tmpdir, filename + ".json").toFile();

            if (dataset.getSubunits() != null) {
                InputStream resource = new ClassPathResource("public/"+filename+".json").getInputStream();
                Files.copy(resource, subunitFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                dataset.setSubunits(filename + ".json");
            }
            
            if (dataset.getSubunitsBase64() != null) {
                try (FileOutputStream fos = new FileOutputStream(subunitFile); ) {
                    String b64 = dataset.getSubunitsBase64();
                    byte[] decoder = Base64.getDecoder().decode(b64);
                    fos.write(decoder);                    
                  } catch (IOException e) {
                    e.printStackTrace();
                    throw new Exception(e);
                  }
                dataset.setSubunits(filename + ".json");
                dataset.setSubunitsBase64(null); // Base64 soll nicht zum Client geschickt werden.
            }            
            datasetMap.put(dataset.getId(), dataset);            
        }
    }
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("sodata", HttpStatus.OK);
    }
    
    @GetMapping("/datasets")
    public List<Dataset> searchDatasets(@RequestParam(value="query", required=false) String searchTerms) {
        
        // TODO: ggf https://github.com/sogis/modelfinder/blob/main/modelfinder-server/src/main/java/ch/so/agi/search/LuceneSearcher.java#L247
        // Entscheiden beim Refactoring.
        if (searchTerms == null) {
            return config.getDatasets();
        } else {
            List<Map<String, String>> results = null;
            try {
                results = indexSearcher.searchIndex(searchTerms, QUERY_MAX_RECORDS);
                log.debug("Search for '" + searchTerms +"' found " + results.size() + " records");            
            } catch (LuceneSearcherException | InvalidLuceneQueryException e) {
                throw new IllegalStateException(e);
            }

            List<Dataset> resultList = results.stream()
                    .map(r -> {
                        return datasetMap.get(r.get("id"));
                    })
                    .collect(Collectors.toList());
            return resultList;
        }
    }
    
    @GetMapping(value="/opensearchdescription.xml", produces=MediaType.APPLICATION_XML_VALUE) 
    public ResponseEntity<?> opensearchdescription() {
        String xml = """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
  <ShortName>Geodaten Kanton Solothurn</ShortName>
  <Url type="text/html" method="get" template="%s?filter={searchTerms}"/>
  <Url type="application/x-suggestions+json" method="get" template="%s/search/suggestions?q={searchTerms}"/>
  <LongName>Datenbezug • Kanton Solothurn</LongName>
  <Image height="16" width="16" type="image/x-icon">data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAACmAAAApgHdff84AAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAARdJREFUOI3tj7FKw1AUhv/orY0R08nFBwhIlgqXLMEX6OxsW9w6BKvo5uxqH8JXEHGsBLUWh0SHDtZAM4kheHMrRjHXwRICLdK7+4/nnO/7OQoAbG9Wb2mJVCERH4p/1rujBAD0sqo1tPKSjOD4PV0FgAUZaFb+BQABgA9VTZZrNSnw8/wiyQVc1yP94FBKwG96ETB5QUihv8myLMsFCWNjGVgIgYTzcS4IguDa87zveQWu64q3OL4szlYazeaDmDM79foAQAkAFieCrzRNOSFki1Kq/dV+2unEV93ufhiG/tTSNM3WXrv9zBibao2iSDiOM7Isa7fIKDNK1m3bPjEMY0OvVNYAgDH2+jQcPt73+0ec85fi8Q+aXZsh3ERCggAAAABJRU5ErkJggg==</Image>
</OpenSearchDescription>   
        """.formatted(getHost(), getHost());
        
        return new ResponseEntity<String>(xml, HttpStatus.OK);
    }
    
    @GetMapping(value="/search/suggestions", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> suggestModels(@RequestParam(value="q", required=false) String searchTerms) {
        List<Map<String, String>> results = null;

        try {
            results = indexSearcher.searchIndex(searchTerms, 50);
            log.debug("Search for '" + searchTerms +"' found " + results.size() + " records");            
        } catch (LuceneSearcherException | InvalidLuceneQueryException e) {
            throw new IllegalStateException(e);
        }

        ArrayNode suggestions = objectMapper.createArrayNode();
        suggestions.add(searchTerms);
        
        ArrayNode completions = objectMapper.createArrayNode();
        results.forEach(it -> {
           completions.add(it.get("title")); 
        });
        suggestions.add(completions);
        log.debug(suggestions.toPrettyString());
        
        return new ResponseEntity<JsonNode>(suggestions, HttpStatus.OK);
    }

    private String getHost() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
