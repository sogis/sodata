package ch.so.agi.sodata;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;

import ch.so.agi.meta2file.model.Item;
import ch.so.agi.meta2file.model.ThemePublication;

public class GeoJsonWriterTest {

    @Test
    public void GeoJson_Ok() throws IOException, ParseException {
        var themePublication = new ThemePublication();
        themePublication.setIdentifier("ch.so.agi.lidar_2019.dtm_hillshade");
        
        var items = new ArrayList<Item>();
        {
            var item = new Item();
            item.setIdentifier("2593500_1227000");
            item.setTitle("2593500_1227000");
            item.setLastPublishingDate(LocalDate.parse("2019-03-01"));
            item.setGeometry("POLYGON((2593500 1227500, 2594000 1227500, 2594000 1227000, 2593500 1227000, 2593500 1227500))");
            items.add(item);
        }
        {
            var item = new Item();
            item.setIdentifier("2592000_1228000");
            item.setTitle("2592000_1228000");
            item.setLastPublishingDate(LocalDate.parse("2019-03-01"));
            item.setGeometry("POLYGON((2592000 1228500, 2592500 1228500, 2592500 1228000, 2592000 1228000, 2592000 1228500))");
            items.add(item);
        }
        themePublication.setItems(items);

        var tmpFolder = Files.createTempDirectory("geojsonwritertest-").toFile();
        var jsonFile = Paths.get(tmpFolder.getAbsolutePath(), themePublication.getIdentifier() + ".json").toFile();
        //var jsonFile = Paths.get("/Users/stefan/tmp/ch.so.agi.lidar_2019.dtm_hillshade.json").toFile();
        var gsw = new GeoJsonWriter();
        gsw.write(jsonFile, items); 
        var jsonContent = new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())));

        // Falls dieser Ansatz nicht robust genug ist, muss wohl ein low-level Jackson-Ansatz gewählt werden.
        // Mit HashMap alleine wird es mühsam. 
        // Grund für das eine mögliche Unrobustheit sind z.B. Leerschläge.
        assertTrue(jsonContent.contains("\"coordinates\":[[[2593500.0,1227500.0],[2594000.0,1227500.0],[2594000.0,1227000.0],[2593500.0,1227000.0],[2593500.0,1227500.0]]]"));
        assertTrue(jsonContent.contains("\"identifier\":\"2593500_1227000\""));
        assertTrue(jsonContent.contains("\"title\":\"2593500_1227000\""));
        assertTrue(jsonContent.contains("\"lastPublishingDate\":\"2019-03-01\""));
        
        assertTrue(jsonContent.contains("\"coordinates\":[[[2592000.0,1228500.0],[2592500.0,1228500.0],[2592500.0,1228000.0],[2592000.0,1228000.0],[2592000.0,1228500.0]]]"));
        assertTrue(jsonContent.contains("\"identifier\":\"2592000_1228000\""));
        assertTrue(jsonContent.contains("\"title\":\"2592000_1228000\""));
    } 
}
