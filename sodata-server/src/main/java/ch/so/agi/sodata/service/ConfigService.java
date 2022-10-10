package ch.so.agi.sodata.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.locationtech.jts.io.ParseException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.so.agi.meta2file.model.ThemePublication;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import ch.so.agi.sodata.repository.LuceneThemePublicationRepository;
import ch.so.agi.sodata.util.GeoJsonWriter;

@Service
public class ConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Value("${app.itemsGeoJsonDir}")
    private String itemsGeoJsonDir;
    
    @Value("${app.configFile}")
    private String CONFIG_FILE;   
    
    @Autowired
    private LuceneThemePublicationRepository luceneThemePublicationRepository;
    
    private ModelMapper modelMapper = new ModelMapper();

    private Map<String,ThemePublicationDTO> themePublicationMap = new HashMap<>();
    
    private List<ThemePublicationDTO> themePublicationList = new ArrayList<>();
    
    public Map<String, ThemePublicationDTO> getThemePublicationMap() {
        return themePublicationMap;
    }

    public void setThemePublicationMap(Map<String, ThemePublicationDTO> themePublicationMap) {
        this.themePublicationMap = themePublicationMap;
    }

    public List<ThemePublicationDTO> getThemePublicationList() {
        return themePublicationList;
    }

    public void setThemePublicationList(List<ThemePublicationDTO> themePublicationList) {
        this.themePublicationList = themePublicationList;
    }


    /*
     * - Kann ich überhaupt ganz einfach POJO machen, d.h. ohne Annotationen?
     * - 1. Versuch (ohne Memory-Optimierung): Benötigter Inhalt in Memory-Map vorhalten.
     * - 2. Memory-optimized: Anstelle einer Memory-Map eine h2-db. Das Java-Objekt wird rein-serialisiert und ident
     * als Schlüssel nach der Lucene-Suche.
     * 
     */
    public void readXml() throws XMLStreamException, IOException, ParseException {
        // Falls der XmlMapper als Bean definiert wird, überschreibt er den Default-Object-Mapper,
        // welcher Json-Output liefert. Falls der XmlMapper als Bean benötigt wird, muss ich nochmals
        // über die Bücher.
        // Die Methode wird momentan nur ein einziges Mal direkt nach dem Hochfahren aufgerufen und
        // somit scheint der XmlMapper nicht als Bean benötigt zu werden.
        var xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // TODO: wieder entfernen, wenn stabil? Oder tolerant sein?
        //xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //xmlMapper.registerModule(new JavaTimeModule());

        log.debug("config file: " + new File(CONFIG_FILE).getAbsolutePath());
//        System.out.println("config file: " + new File(CONFIG_FILE).getAbsolutePath());
//        System.err.println("config file: " + new File(CONFIG_FILE).getAbsolutePath());
                
        var xif = XMLInputFactory.newInstance();
        var xr = xif.createXMLStreamReader(new FileInputStream( new File(CONFIG_FILE)));

        while (xr.hasNext()) {
            xr.next();
            if (xr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if ("themePublication".equals(xr.getLocalName())) {
                    var themePublication = xmlMapper.readValue(xr, ThemePublication.class);
                    var identifier = themePublication.getIdentifier();
                    var items = themePublication.getItems();
                    
                    log.debug("Identifier: "+ themePublication.getIdentifier());
                    
                    ThemePublicationDTO themePublicationDTO = modelMapper.map(themePublication, ThemePublicationDTO.class);

                    // Die GeoJSON-Datei mit den Subunits zur Auswahl im Client 
                    // wird nur benötigt, falls es wirklich etwas auszuwählen gibt.
                    // D.h. wenn es mindestens 2 Items (=Subunits) gibt.
                    if (items.size() > 1) {
                        themePublicationDTO.setHasSubunits(true);
                        
                        File geoJsonFile = Paths.get(itemsGeoJsonDir, identifier + ".json").toFile();
                        var gsw = new GeoJsonWriter();
                        gsw.write(geoJsonFile, items); 
                        log.debug("GeoJSON file written: " + geoJsonFile);
                    } 

                    themePublicationMap.put(identifier, themePublicationDTO);
                    themePublicationList.add(themePublicationDTO);
                }
            }
        }
        luceneThemePublicationRepository.saveAll(themePublicationList);
    }
}
