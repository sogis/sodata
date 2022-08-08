package ch.so.agi.sodata.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.sodata.model.dataproductservice.Dataproduct;
import ch.so.agi.sodata.model.dataproductservice.DataproductResponse;
import ch.so.agi.sodata.model.dataproductservice.Result;
import ch.so.agi.sodata.model.dataproductservice.SimpleDataproduct;
import ch.so.agi.sodata.model.dataproductservice.Sublayer;

@Service
public class DataproductHarvester {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObjectMapper objectMapper;
    
    private List<SimpleDataproduct> mapLayers;

    public List<SimpleDataproduct> getMapLayers() {
        return mapLayers;
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
        
        // Rahmenbedingung:
        // Der Abstract einer Layergruppe soll auch Inhalt des Abtracts eines Single Layers sein. 
        // Falls der Singlelayer einen eigenen Abtract enthält wird der Layergruppen-Abstract
        // hinzugefügt. Wird wohl sauberer sein mit Projekt Datenbezug. Jetzt könnte es Copy/Paste
        // Dupleten haben.
        // Die Layergruppe wird nicht indexiert, sondern nur als Feld eines Singlelayers.
        
        
        // Eventuell List mit gruppierten Objekten? 

        mapLayers = new ArrayList<SimpleDataproduct>();
        
        DataproductResponse dataproductResponse = objectMapper.readValue(response.body(), DataproductResponse.class);
        
        for (Result result : dataproductResponse.getResults()) {
            Dataproduct dataproduct = result.getDataproduct();
            
            String dataproductId =  result.getDataproduct().getDataproductId();
            List<Sublayer> dataproductSublayers = result.getDataproduct().getSublayers();
            
            httpRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://geo-i.so.ch/api/dataproduct/v1/weblayers?filter=" + dataproductId))
                    .GET()
                    .build();

            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            Map<String, Object> mapLayerMap = objectMapper.readValue(response.body(), HashMap.class);
            List<Map> infoMap = (List<Map>) mapLayerMap.get(dataproductId);
            
            String rootDataproductId = dataproductId;
            String rootDisplay = dataproduct.getDisplay();
            String rootAbstract = (String) infoMap.get(0).get("abstract");
            boolean rootVisibility = (boolean) infoMap.get(0).get("visibility");
            int rootOpacity = (int) infoMap.get(0).get("opacity");
            
            if (dataproduct.getSublayers() == null) {
                log.info("ich bin ein singlelayer: "+ dataproductId);
                SimpleDataproduct simpleDataproduct = new SimpleDataproduct();
                simpleDataproduct.setDataproductId(dataproductId);
                simpleDataproduct.setDisplay(rootDisplay);
                simpleDataproduct.setLayerAbstract(rootAbstract);
                simpleDataproduct.setVisibility(rootVisibility);
                simpleDataproduct.setOpacity(rootOpacity);
                mapLayers.add(simpleDataproduct);
            } else {
                log.info("ich bin eine layergroup: "+ dataproductId);

                for (var layerInfo : infoMap) {
                    List<Map<String,Object>> sublayers = (List<Map<String,Object>>) layerInfo.get("sublayers");
                    for (Map<String,Object> sublayer : sublayers) {
                        String sublayerName = (String) sublayer.get("name");
                        String sublayerTitle = (String) sublayer.get("title");
                        String sublayerAbstract = (String) sublayer.get("abstract");
                        boolean sublayerVisibility = (boolean) sublayer.get("visibility");
                        int sublayerOpacity = (int) sublayer.get("opacity");
                        
                        SimpleDataproduct simpleDataproduct = new SimpleDataproduct();
                        simpleDataproduct.setDataproductId(sublayerName);
                        simpleDataproduct.setDisplay(sublayerTitle);
                        simpleDataproduct.setLayerAbstract(rootAbstract + " " + sublayerAbstract);
                        simpleDataproduct.setVisibility(sublayerVisibility);
                        simpleDataproduct.setOpacity(sublayerOpacity);
                        simpleDataproduct.setLayerGroupDataproductId(rootDataproductId);
                        simpleDataproduct.setLayerGroupDisplay(rootDisplay);
                        mapLayers.add(simpleDataproduct);
                    }
                }
            }
        }
    }
}
