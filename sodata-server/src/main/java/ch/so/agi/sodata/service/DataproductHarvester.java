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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.sodata.UmlautComparatorMaplayer;
import ch.so.agi.sodata.model.Dataproduct;

@Service
public class DataproductHarvester {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObjectMapper objectMapper;

    private List<Dataproduct> dataproducts;

    private Map<String,Dataproduct> dataproductsMap;
    
    public List<Dataproduct> getDataproducts() {
        return dataproducts;
    }

    public Map<String, Dataproduct> getDataproductsMap() {
        return dataproductsMap;
    }

    @PostConstruct
    private void init() throws Exception /*TODO*/ {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(Redirect.ALWAYS)
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://geo-i.so.ch/api/search/v2/?searchtext=ch.so&filter=foreground&limit=5000"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        //log.info(response.body());
        
        // Logik anhand amtlicher Vermessung Layergruppe
        //https://geo.so.ch/api/search/v2/?searchtext=amtliche%20vermessung&filter=foreground&limit=5
        //https://geo.so.ch/api/dataproduct/v1/weblayers?filter=ch.so.agi.av.amtliche_vermessung
        
        //https://geo.so.ch/api/search/v2/?searchtext=Landwirtschaftliche%20Bewirtschaftungseinheiten&filter=foreground&limit=5
        //https://geo.so.ch/api/dataproduct/v1/weblayers?filter=ch.so.alw.landwirtschaft_tierhaltung.landwirtschaftliche_bewirtschaftungseinheiten 
        
        dataproducts = new ArrayList<Dataproduct>();
        
        var rootNode = objectMapper.readTree(response.body());
        var resultsArray = rootNode.get("results");
        for (JsonNode node : resultsArray) {
            log.info(node.get("dataproduct").get("dataproduct_id").asText());
            String dataproductId = node.get("dataproduct").get("dataproduct_id").asText();

            httpRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://geo-i.so.ch/api/dataproduct/v1/weblayers?filter=" + dataproductId))
                    .GET()
                    .build();

            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            var weblayerRootNode = objectMapper.readTree(response.body());
            var weblayerArray = weblayerRootNode.get(dataproductId);
            var weblayerNode = weblayerArray.get(0);
            var ident = weblayerNode.get("name").asText();
            var title = weblayerNode.get("title").asText();
            var theAbstract = weblayerNode.get("abstract").asText("-");
            var visibility = weblayerNode.get("visibility").asBoolean(false);
            var opacity = weblayerNode.get("opacity").asInt(255);
            
            Dataproduct dataproduct = new Dataproduct();
            dataproduct.setIdent(dataproductId);
            dataproduct.setTitle(title);
            dataproduct.setTheAbstract(theAbstract);
            dataproduct.setVisibility(visibility);
            dataproduct.setOpacity(opacity);
            
            dataproducts.add(dataproduct);
            
            if (weblayerNode.has("sublayers")) {
                var sublayers = new ArrayList<Dataproduct>();
                for (JsonNode sublayerNode : weblayerNode.get("sublayers")) {
                    var sublayerIdent = sublayerNode.get("name").asText();
                    var sublayerTitle = sublayerNode.get("title").asText();
                    var sublayerAbstract = sublayerNode.get("abstract").asText("-");
                    var sublayerVisibility = sublayerNode.get("visibility").asBoolean(false);
                    var sublayerOpacity = sublayerNode.get("opacity").asInt(255);
                    Dataproduct sublayerDataproduct = new Dataproduct();
                    sublayerDataproduct.setIdent(sublayerIdent);
                    sublayerDataproduct.setTitle(sublayerTitle);
                    sublayerDataproduct.setTheAbstract(sublayerAbstract);
                    sublayerDataproduct.setVisibility(sublayerVisibility);
                    sublayerDataproduct.setOpacity(sublayerOpacity);
                    sublayerDataproduct.setParentIdent(dataproductId);
                    sublayerDataproduct.setParentTitle(title);
                    sublayers.add(sublayerDataproduct);
                }
                Collections.sort(sublayers, new UmlautComparatorMaplayer());
                
                dataproduct.setSublayers(sublayers);
            }
        }
        
        dataproductsMap = new HashMap<>();
        for (Dataproduct dataproduct : dataproducts) {
            dataproductsMap.put(dataproduct.getIdent(), dataproduct);
        }        
    }
}
