package ch.so.agi.sodata.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.so.agi.sodata.ClientSettings;
import ch.so.agi.sodata.dto.DataproductDTO;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import ch.so.agi.sodata.repository.InvalidLuceneQueryException;
import ch.so.agi.sodata.repository.LuceneDataproductRepository;
import ch.so.agi.sodata.repository.LuceneSearcherException;
import ch.so.agi.sodata.repository.LuceneThemePublicationRepository;
import ch.so.agi.sodata.service.ConfigService;
import ch.so.agi.sodata.service.LayerConfigService;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${lucene.queryMaxRecords}")
    private Integer QUERY_MAX_RECORDS;   

    @Autowired
    private ClientSettings settings;
        
    @Autowired
    private LuceneThemePublicationRepository themePublicationsRepository;
    
    @Autowired
    private LuceneDataproductRepository dataproductRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private LayerConfigService layerConfigService;
        
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("sodata", HttpStatus.OK);
    }
    
    @RequestMapping(value = "/settings", method = RequestMethod.GET, produces = { "application/json" })
    public ClientSettings settings() {
        return settings;
    }

    @RequestMapping(value = "/themepublications", method = RequestMethod.GET, produces = { "application/json" })
    public List<ThemePublicationDTO> searchThemePublications(@RequestParam(value="query", required=false) String searchTerms) { 
        if (searchTerms == null || searchTerms.trim().length() == 0) {
            return configService.getThemePublicationList();
        } else {
            List<Map<String, String>> results = null;
            try {
                results = themePublicationsRepository.findByQuery(searchTerms, QUERY_MAX_RECORDS);
                log.debug("Theme publication search for '" + searchTerms +"' found " + results.size() + " records");            
            } catch (LuceneSearcherException | InvalidLuceneQueryException e) {
                throw new IllegalStateException(e);
            }

            List<ThemePublicationDTO> resultList = results.stream()
                    .map(r -> {
                        return configService.getThemePublicationMap().get(r.get("id"));
                    })
                    .collect(Collectors.toList());
            return resultList;
        }
    }
    
    @RequestMapping(value = "/maplayers", method = RequestMethod.GET, produces = { "application/json" })
    public List<DataproductDTO> searchDataproducs(@RequestParam(value="query", required=false) String searchTerms) {
        if (searchTerms == null || searchTerms.trim().length() == 0) {
            return layerConfigService.getDataproductList();
        } else {
            List<Map<String, String>> results = null;
            try {
                results = dataproductRepository.findByQuery(searchTerms, QUERY_MAX_RECORDS);
                log.debug("Map layer search for '" + searchTerms +"' found " + results.size() + " records");
            } catch (LuceneSearcherException | InvalidLuceneQueryException e) {
                throw new IllegalStateException(e);
            }
            
            // Achtung: So werden Layergruppen ausgeschlossen, die kein Thema haben, da
            // da die Dataproduct-Map hierachisch aufgebaut ist, sprich die Sublayer halt
            // wirklich auch Sublayer sind.
            // Im Lucene Index wird zwar "MOpublic" gefunden aber nur für die Sublayer. 
            // r.get("ident") ist ident des Sublayers. Den gibt es abe nicht in der Map.
            // Ist aber auch bei anderen Suchattributen so: Wenn die Layergruppe quasi
            // nicht ein Superset der Suchattribute aller Sublayer ist, gehen diese
            // Layer(gruppen) verloren.
            
            // Bedarf eine gewissen Überarbeitung...
            List<DataproductDTO> resultList = results.stream()
                    .filter(r -> {
                        if (r.get("parentident") != null) {
                            return Objects.nonNull(layerConfigService.getDataproductsMap().get(r.get("parentident")));
                        } else {
                            return Objects.nonNull(layerConfigService.getDataproductsMap().get(r.get("ident")));
                        }
                    })
                    .map(r -> {
                        if (r.get("parentident") != null) {
                            return layerConfigService.getDataproductsMap().get(r.get("parentident"));
                        } else {
                            return layerConfigService.getDataproductsMap().get(r.get("ident"));
                        }
                    })
                    .distinct()
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
            results = themePublicationsRepository.findByQuery(searchTerms, 50);
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
