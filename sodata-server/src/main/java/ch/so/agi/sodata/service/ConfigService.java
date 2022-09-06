package ch.so.agi.sodata.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.locationtech.jts.io.ParseException;
import org.modelmapper.ModelMapper;
import org.modelmapper.module.jsr310.Jsr310Module;
import org.modelmapper.module.jsr310.Jsr310ModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.so.agi.meta2file.model.ThemePublication;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import ch.so.agi.sodata.repository.ThemePublicationRepository;
import ch.so.agi.sodata.util.GeoJsonWriter;

//import ch.so.agi.meta2file.model.ThemePublication;

@Service
public class ConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Value("${app.itemsGeoJsonDir}")
    private String itemsGeoJsonDir;
    
    @Value("${app.configFile}")
    private String CONFIG_FILE;   
    
    @Autowired
    private ThemePublicationRepository themePublicationRepository;
    
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

//    //TEMP
//    private List<ThemePublicationDTO> themePublicationList;
//
//    public List<ThemePublicationDTO> getThemePublicationList() {
//        return themePublicationList;
//    }
//    
//    public void setThemePublicationList(List<ThemePublicationDTO> themePublicationList) {
//        this.themePublicationList = themePublicationList;
//    }
//    //TEMP

    /**
     * - Kann ich überhaupt ganz einfach POJO machen, d.h. ohne Annotationen?
     * - 1. Versuch (ohne Memory-Optimierung): Benötigter Inhalt in Memory-Map vorhalten.
     * Wahrscheinlich ohne Geometriezeugs, das wird beim Parsen wegpersistiert und mit einem Fremdschlüssel
     * versehen.
     * - Beim Parsen den notwendigen Teil in den Lucene-Index schreiben (Repository...)
     * - Beim Suchen wird dann der Index durchsucht und dann die Objekte aus der Memory-Map geholt
     * (jetzt glaub schon so).
     * - Eventuell die Metadaten erst ohne-Request holen, weil die doch relativ umfangreich sein können.
     * Dann müsste das mit Map noch gescheit gemacht werden, nicht dass man 2 Maps vorhalten muss. Ah oder doch:
     * Einmal ohne Meta und eine Meta-Pur mit FK.
     * 
     * - 2. Memory-optimized: Anstelle einer Memory-Map eine h2-db. Das Java-Objekt wird rein-serialisiert und ident
     * als Schlüssel nach der Lucene-Suche.
     * @throws ParseException 
     * 
     */

    public void readXml() throws XMLStreamException, IOException, ParseException {
        // Falls der XmlMapper als Bean definiert wird, überschreibt er den den Default-Object-Mapper,
        // welcher Json-Output liefert. Falls es den XmlMapper als Bean benötigt, muss ich nochmals
        // über die Bücher.
        // Die Methode wird momentan nur ein einziges Mal direkt nach dem Hochfahren aufgerufen.
        var xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // TODO: wieder entfernen, wenn stabil? Oder tolerant sein?
        //xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //xmlMapper.registerModule(new JavaTimeModule());

        log.info("config file: " + new File(CONFIG_FILE).getAbsolutePath());
                
        var xif = XMLInputFactory.newInstance();
        var xr = xif.createXMLStreamReader(new FileInputStream( new File(CONFIG_FILE)));

        while (xr.hasNext()) {
            xr.next();
            if (xr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if ("themePublication".equals(xr.getLocalName())) {
                    var themePublication = xmlMapper.readValue(xr, ThemePublication.class);
                    var identifier = themePublication.getIdentifier();
                    var items = themePublication.getItems();
                    
                    // Die GeoJson-Datei mit den Subunits zur Auswahl im Client 
                    // wird nur benötigt, falls es wirklich etwas auszuwählen gibt.
                    // D.h. wenn es mindestens 2 Items (=Subunits) gibt.
                    if (items.size() > 1) {
                        var gsw = new GeoJsonWriter();
                        gsw.write(Paths.get(itemsGeoJsonDir, identifier + ".json").toFile(), items); 
                    }

                    ThemePublicationDTO themePublicationDTO = modelMapper.map(themePublication, ThemePublicationDTO.class);

                    //themePublicationRepository.save(themePublicationDTO);
                    themePublicationMap.put(identifier, themePublicationDTO);
                    themePublicationList.add(themePublicationDTO);
                }
            }
        }
        themePublicationRepository.saveAll(themePublicationList);
    }
}
