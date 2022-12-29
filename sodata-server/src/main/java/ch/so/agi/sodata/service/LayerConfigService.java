package ch.so.agi.sodata.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.sodata.UmlautComparatorDataproduct;
import ch.so.agi.sodata.dto.DataproductDTO;
import ch.so.agi.sodata.repository.LuceneDataproductRepository;

@Service
public class LayerConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObjectMapper objectMapper;

    @Value("${app.searchServiceUrl}")
    private String searchServiceUrl;
    
    @Value("${app.dataproductServiceUrl}")
    private String dataproductServiceUrl;

    @Autowired
    private LuceneDataproductRepository luceneDataproductRepository;

    private List<DataproductDTO> dataproductList = new ArrayList<>();
    
    private Map<String,DataproductDTO> dataproductsMap = new HashMap<>();
    
    public List<DataproductDTO> getDataproductList() {
        return dataproductList;
    }

    public Map<String, DataproductDTO> getDataproductsMap() {
        return dataproductsMap;
    }

    public void readJson () throws URISyntaxException, IOException, InterruptedException {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(Redirect.ALWAYS)
                .build();

        var httpRequest = HttpRequest.newBuilder()
                .uri(new URI(searchServiceUrl))
                .GET()
                .build();
        
        var response = httpClient
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());

        dataproductList = new ArrayList<DataproductDTO>();

        var rootNode = objectMapper.readTree(response.body());
        var resultsArray = rootNode.get("results");
        for (JsonNode node : resultsArray) {
            log.info(node.get("dataproduct").get("dataproduct_id").asText());
            String dataproductId = node.get("dataproduct").get("dataproduct_id").asText();

            httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(dataproductServiceUrl + dataproductId))
                    .GET()
                    .build();

            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            //log.info(response.body());
            
            var weblayerRootNode = objectMapper.readTree(response.body());
            var weblayerArray = weblayerRootNode.get(dataproductId);
            var weblayerNode = weblayerArray.get(0);
            var ident = weblayerNode.get("name").asText();
            var title = weblayerNode.get("title").asText();
            var theAbstract = weblayerNode.get("abstract").asText("-");
            var visibility = weblayerNode.get("visibility").asBoolean(false);
            var opacity = weblayerNode.get("opacity").asInt(255);
            
            DataproductDTO dataproduct = new DataproductDTO();
            dataproduct.setIdent(dataproductId);
            dataproduct.setTitle(title);
            dataproduct.setTheAbstract(theAbstract);
            dataproduct.setVisibility(visibility);
            dataproduct.setOpacity(opacity);
            
            var themeTitle = extractThemeTitle(theAbstract);
            if (themeTitle != null) {
                dataproduct.setThemeTitle(themeTitle);
            }
                                  
            if (weblayerNode.has("sublayers")) {
                var sublayers = new ArrayList<DataproductDTO>();
                log.debug("-----------------------------------------");
                for (JsonNode sublayerNode : weblayerNode.get("sublayers")) {
                    var sublayerIdent = sublayerNode.get("name").asText();
                    var sublayerTitle = sublayerNode.get("title").asText();
                    log.debug("   " + sublayerTitle);
                    var sublayerAbstract = sublayerNode.get("abstract").asText("-");
                    var sublayerVisibility = sublayerNode.get("visibility").asBoolean(false);
                    var sublayerOpacity = sublayerNode.get("opacity").asInt(255);
                    DataproductDTO sublayerDataproduct = new DataproductDTO();
                    sublayerDataproduct.setIdent(sublayerIdent);
                    sublayerDataproduct.setTitle(sublayerTitle);
                    sublayerDataproduct.setTheAbstract(sublayerAbstract);
                    sublayerDataproduct.setVisibility(sublayerVisibility);
                    sublayerDataproduct.setOpacity(sublayerOpacity);
                    sublayerDataproduct.setParentIdent(dataproductId);
                    sublayerDataproduct.setParentTitle(title);
                    
                    var sublayerThemeTitle = extractThemeTitle(sublayerAbstract);
                    if (sublayerThemeTitle != null) {
                        sublayerDataproduct.setThemeTitle(sublayerThemeTitle);
                        // sinnvoll?
                        // Siehe Gewässerschutz: da stimmt es jedoch nicht.
                        // Warscheinlich weil externe Quellen?!
                        // Zeit wohl ein wenig unsere Layergruppen vs. Map 
                        // Problematik.
                        if (dataproduct.getThemeTitle() == null) {
                            dataproduct.setThemeTitle(sublayerThemeTitle);
                        }
                    }
                    
                    sublayers.add(sublayerDataproduct);
                }
                Collections.sort(sublayers, new UmlautComparatorDataproduct());
                
                dataproduct.setSublayers(sublayers);
                
                dataproductList.add(dataproduct);
            }
        }
        
        for (DataproductDTO dataproduct : dataproductList) {
            dataproductsMap.put(dataproduct.getIdent(), dataproduct);
        }   
                
        luceneDataproductRepository.saveAll(dataproductList);
    } 
    
    // Brutal handgestrickt. In einer idealen Welt ist das Thema
    // natürlich strukturiert greifbar.
    private String extractThemeTitle(String theAbstract) {
        int themeStringIdx = theAbstract.indexOf("Teil des Themas");
        if (themeStringIdx > -1) {
            var partialAbstractString = theAbstract.substring(themeStringIdx);
            int startIdx = partialAbstractString.indexOf("<b>");
            int endIdx = partialAbstractString.indexOf("</b>");
            
            return partialAbstractString.substring(startIdx+3, endIdx-1);
        }
        return null;
    }
}
