package ch.so.agi.sodata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.locationtech.jts.io.ParseException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import ch.so.agi.meta2file.model.ThemePublication;
import ch.so.agi.sodata.dto.ThemePublicationDTO;

//import ch.so.agi.meta2file.model.ThemePublication;

@Service
public class ConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Value("${app.itemsGeoJsonDir}")
    private String itemsGeoJsonDir;
    
    @Value("${app.configFile}")
    private String CONFIG_FILE;   

    @Autowired
    private XmlMapper xmlMapper;

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
        log.info("config file: " + new File(CONFIG_FILE).getAbsolutePath());
                
        var xif = XMLInputFactory.newInstance();
        var xr = xif.createXMLStreamReader(new FileInputStream( new File(CONFIG_FILE)));

        while (xr.hasNext()) {
            xr.next();
            if (xr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if ("themePublication".equals(xr.getLocalName())) {
                    System.out.println(xr.getLocalName());

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
                    
                    //log.info(itemsGeoJsonDir);

                    // -> Bean
                    //ModelMapper modelMapper = new ModelMapper();
                    //ThemePublicationDTO themePublicationDTO = modelMapper.map(themePublication, ThemePublicationDTO.class);
                    //System.out.println(themePublicationDTO.getLastPublishingDate());
                    
                    // siehe http://modelmapper.org/user-manual/converters/ für spezielle Umwandlungen (list/array...)
                    
                    // oder das Mapping ganz manuell oder halt automatisch, d.h. das was einfach ist und der Rest
                    // muss eventuall sowieso sehr speziell behandelt werden (geometrien -> geojson).
                }
            }
        }
    }
}
