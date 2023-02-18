package ch.so.agi.sodata.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.so.agi.meta2file.model.ThemePublication;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import ch.so.agi.sodata.repository.LuceneThemePublicationRepository;
import ch.so.agi.sodata.util.GeoJsonWriter;
import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxWriter;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;

@Service
public class ConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Value("${app.itemsGeoJsonDir}")
    private String itemsGeoJsonDir;
    
    @Value("${app.configFile}")
    private String CONFIG_FILE;   

    @Value("${app.ilidataDir}")
    private String ilidataDir;

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

    // Alternative Variante, falls Map/List zu gross wird (zu viel Speicher benötigt): 
    // Im Container lokal vorliegende H2-Datenbank. Das Java-Objekt wird rein-serialisiert und identifier
    // als Schlüssel nach der Lucene-Suche.
    
    // TODO rename method
    public void readXml() throws XMLStreamException, IOException, ParseException {
        // Falls der XmlMapper als Bean definiert wird, überschreibt er den Default-Object-Mapper,
        // welcher Json-Output liefert. Falls der XmlMapper als Bean benötigt wird, muss ich nochmals
        // über die Bücher.
        // Die Methode wird momentan nur ein einziges Mal direkt nach dem Hochfahren aufgerufen und
        // somit scheint der XmlMapper nicht als Bean benötigt zu werden.
        var xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // TODO: wieder entfernen, wenn stabil? Oder tolerant sein?

        log.debug("config file: " + new File(CONFIG_FILE).getAbsolutePath());
                
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
        
        // In einem zweiten Durchlauf erstellen wir die ilidata.xml-Datei.     
        try {
            this.createIlidataXml();
        } catch (Ili2cFailure | IOException | IoxException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }
        
    }
    
    // FIXME DTO reichen nicht, da die subunits nicht mehr vorhanden sind.
    // Entweder ein 2. Mal parsen (XmlMapper in postconstruct) oder direkt
    // beim ersten mal die ilidata.xml-Datei schreiben (ohne ein Geheu zu machen).
    
    private void createIlidataXml() throws IOException, Ili2cFailure, IoxException {
        String ILIDATA16 = "DatasetIdx16.ili";
        
        String tmpdir = System.getProperty("java.io.tmpdir");
        File ilidataFile = Paths.get(tmpdir, ILIDATA16).toFile();
        InputStream resource = new ClassPathResource("ili/"+ILIDATA16).getInputStream();
        Files.copy(resource, ilidataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        ArrayList<String> filev = new ArrayList<>(List.of(ilidataFile.getAbsolutePath()));
        TransferDescription td = Ili2c.compileIliFiles(filev, null);

        String ILI_TOPIC="IliRepository09.RepositoryIndex";
        String BID="DatasetIdx16.DataIndex";

        File outputFile = Paths.get(ilidataDir, "ilidata.xml").toFile();
        IoxWriter ioxWriter = new XtfWriter(outputFile, td);

        ioxWriter.write(new StartTransferEvent("SOGIS-20230218", "", null));
        ioxWriter.write(new StartBasketEvent(ILI_TOPIC,BID));
        
        // i separat, wegen subunits
        
        int tid = 1;
        for (ThemePublicationDTO themePublication : themePublicationList) {
//            themePublication.it
//            if () {
//                
//            }
            
            
            Iom_jObject iomObj = new Iom_jObject("DatasetIdx16.DataIndex.DatasetMetadata", String.valueOf(tid+1));
            iomObj.setattrvalue("id", themePublication.getIdentifier());
            iomObj.setattrvalue("originalId", themePublication.getIdentifier());
            iomObj.setattrvalue("version", "current");
            iomObj.setattrvalue("owner", themePublication.getOwner().getOfficeAtWeb());

            
            ioxWriter.write(new ch.interlis.iox_j.ObjectEvent(iomObj));
        }

        
        ioxWriter.write(new EndBasketEvent());
        ioxWriter.write(new EndTransferEvent());
        ioxWriter.flush();
        ioxWriter.close();
    }
}
