package ch.so.agi.sodata.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import ch.so.agi.meta2file.model.Item;

//https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonWriteDriver.java

public class GeoJsonWriter {
    private WKTReader wktReader = new WKTReader();

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void write(File outputFile, List<Item> items) throws IOException, ParseException {
        var jsonFactory = new JsonFactory();
        var fos = new FileOutputStream(outputFile);
        var jsonGenerator = jsonFactory.createGenerator(new BufferedOutputStream(fos), JsonEncoding.UTF8);
        
        // header of the GeoJSON file
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", "FeatureCollection");
        String[] authorityAndSRID = {"epsg", "2056"};
        writeCRS(jsonGenerator,authorityAndSRID);
        jsonGenerator.writeArrayFieldStart("features");

        // features
        for (var item : items) {
            var itemIdentifier = item.getIdentifier();
            var itemTitle = item.getTitle();
            var itemLastPublishingDate = item.getLastPublishingDate();
            Map<String,Object> properties = Map.of("identifier",itemIdentifier, "title",itemTitle, "lastPublishingDate",itemLastPublishingDate);
            var geometry = wktReader.read(item.getGeometry());
            writeFeature(jsonGenerator, geometry, properties);
        }
        
        // footer
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        jsonGenerator.close();
    }
    
    private void writeFeature(JsonGenerator jsonGenerator, Geometry geom, Map<String,Object> properties) throws IOException {
        // feature header
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", "Feature");
        //Write the first geometry
        writeGeometry(geom, jsonGenerator);
        //Write the properties
        writeProperties(jsonGenerator, properties);
        // feature footer
        jsonGenerator.writeEndObject();
    }

    private void writeProperties(JsonGenerator jsonGenerator, Map<String,Object> properties) throws IOException {
        if (properties.size() > 0) {
            jsonGenerator.writeObjectFieldStart("properties");
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue() instanceof String s) {
                    jsonGenerator.writeObjectField(key, s);
                } else if (entry.getValue() instanceof LocalDate d) {
                    jsonGenerator.writeObjectField(key, d.format(dateFormatter));
                } else {
                    jsonGenerator.writeObjectField(key, entry.getValue().toString());
                }
            }
            jsonGenerator.writeEndObject();
        }
    }

    private void writeGeometry(Geometry geom, JsonGenerator gen) throws IOException {
        gen.writeObjectFieldStart("geometry");
        if (geom instanceof Polygon) {
            write((Polygon) geom, gen);
        } else if (geom instanceof MultiPolygon) {
            write((MultiPolygon) geom, gen);
        } else {
            throw new RuntimeException("Unsupported Geomery type");
        }
        gen.writeEndObject();
    }
    
    private void write(Polygon geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "Polygon");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        writeCoordinates(geom.getExteriorRing().getCoordinates(), gen);
        for (int i = 0; i < geom.getNumInteriorRing(); ++i) {
            writeCoordinates(geom.getInteriorRingN(i).getCoordinates(), gen);
        }
        gen.writeEndArray();
    }
    
    private void write(MultiPolygon geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "MultiPolygon");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        for (int i = 0; i < geom.getNumGeometries(); ++i) {
            Polygon p = (Polygon) geom.getGeometryN(i);
            gen.writeStartArray();
            writeCoordinates(p.getExteriorRing().getCoordinates(), gen);
            for (int j = 0; j < p.getNumInteriorRing(); ++j) {
                writeCoordinates(p.getInteriorRingN(j).getCoordinates(), gen);
            }
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }

    private void writeCoordinates(Coordinate[] coordinates, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (Coordinate coord : coordinates) {
            writeCoordinate(coord, gen);
        }
        gen.writeEndArray();
    }
    
    private void writeCoordinate(Coordinate coordinate, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(coordinate.x);
        gen.writeNumber(coordinate.y);
        if (!Double.isNaN(coordinate.z)) {
            gen.writeNumber(coordinate.z);
        }
        gen.writeEndArray();
    }

    private void writeCRS(JsonGenerator jsonGenerator, String[] authorityAndSRID) throws IOException {
        if (authorityAndSRID[1] != null) {
            jsonGenerator.writeObjectFieldStart("crs");
            jsonGenerator.writeStringField("type", "name");
            jsonGenerator.writeObjectFieldStart("properties");
            var sb = new StringBuilder("urn:ogc:def:crs:");
            sb.append(authorityAndSRID[0]).append("::").append(authorityAndSRID[1]);
            jsonGenerator.writeStringField("name", sb.toString());
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndObject();
        }
    }
}
