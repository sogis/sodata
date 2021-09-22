# sodata

## to be discussed
- YAML statt JSON (-> config generator: sollte sicher eine Java-Lib geben für)
- Servicelinks? 
  * Nur Endpunkt? Wie sinnvoll ist das?
  * Umgang mit geodienste.ch?
- Subunits: 
  * Woher stammt der klickbare Index?
  * Welche Informationen werden benötigt? (Namen, Datum?)
- Liefert Config den finalen Link auf den Datensatz? Wie ist das bei den Subunits?
- Formate vs Links zu den Daten bei Subunits? Sind die Formate in der normalen Config oder im Subunit-Json?
- last editing date als Attribut auch bei Subunit-Dataset. Es entspricht dem "neuesten" Datum.

## Docs
- Suchindex beim Hochfahren. Index im Pod, nicht persistent.
- Suchindex: Leading wildcard ist momentan nicht umgesetzt -> Feedback abwarten. Falls notwendig, siehe "modelfinder".
- base64 to json: werden in system temp gespeichert. sämtliche json aus diesem Verzeichnis sind exponiert.

## TODO
- Testing!!!
 * https://stackoverflow.com/questions/39690094/spring-boot-default-profile-for-integration-tests/56442693
- ~~application.yml ausserhalb Pod verwenden.~~
- LiDAR-Vektorlayer ist leicht käsig -> Abklären, ob WebGL-Renderer funktioniert und vorhanden ist oder mit VectorImageSource (o.ä.).
- ~~Bug: Suchen -> backspace alle Zeichen -> nicht komplette Liste~~ Id war in yml falsch resp. doppelt. Aus diesem Grund kam es zu doppelten Einträgen.
- ~~Bug: Firefox zeigt Aufklappen-Zeichen nicht bei Tabellen~~
- ~~Link/Icon zu geocat.ch sollte auch beim hovern rot erscheinen.~~ Nein. War eher ungewollt, da a:hover noch im css file vorhanden war.
- ilidata.xml: Gebietsauswahl adaptieren. Raster -> Verweis auf Subunits, dito bei Vektor?
- ~~Lucene Suche~~
- Link zur Karte (siehe Mockup)
- versionierte Datensätze?
- ...

## Development

First Terminal:
```
./mvnw spring-boot:run -Penv-dev -pl *-server -am -Dspring-boot.run.profiles=dev
```

Second Terminal:
```
./mvnw gwt:codeserver -pl *-client -am
```

Or without downloading all the snapshots again:
```
./mvnw gwt:codeserver -pl *-client -am -nsu 
```

## Build

### JVM
```
./mvnw -Penv-prod clean package
```

```
docker build -t sogis/sodata-jvm:latest -f sodata-server/src/main/docker/Dockerfile.jvm .
```

Siehe Dockerfile: Die Datensatz-Konfiguration wird unter `/config/datasets.yml` erwartet. Ohne diese Datei bleibt die Tabelle im Browser leer. Siehe Kapitel "Betrieb" für zusätzliche Informationen.

### Native
```
./mvnw -Penv-prod,native clean package
```

```
docker build -t sogis/sodata:latest -f sodata-server/src/main/docker/Dockerfile.native .
```

In diesem Fall muss das native image auf Linux gebuildet werden.


## Run

### JVM
```
java -jar sodata-server/target/sodata.jar --spring.config.location=classpath:/application.yml,optional:file:/Users/stefan/tmp/datasets.yml
```

```
docker run -p8080:8080 -v /Users/stefan/tmp:/config sogis/sodata-jvm:latest
```

### Native

## Betrieb


## Testrequests
- Alle Datensätze: http://localhost:8080/datasets
- Suche: http://localhost:8080/datasets?query=admin

## WMS
### QGIS server
```
docker-compose build
```

```
http://localhost:8083/wms/subunits?SERVICE=WMS&REQUEST=GetCapabilities
```


### Geoserver
```
docker run --rm --name sogis-geoserver -p 8080:8080 -v ~/sources/sodata/geoserver/data_dir:/var/local/geoserver sogis/geoserver:2.18.0
```

## Subunit Layer
```
SELECT 
    json_build_object(
        'type',
        'FeatureCollection',
        'features',
        json_agg(ST_AsGeoJSON(t.*)::json)
    ) 
FROM 
(
    SELECT 
        t_id, gemeindename AS title, to_char( now(), 'YYYY-MM-DD') as last_editing_date, bfs_gemeindenummer || '00.itf.zip' AS filename, ST_SnapToGrid(geometrie, 0.001)
    FROM 
        agi_hoheitsgrenzen_pub.hoheitsgrenzen_gemeindegrenze_generalisiert hgg 
) AS t;


SELECT 
    json_build_object(
        'type',
        'FeatureCollection',
        'features',
        json_agg(ST_AsGeoJSON(t.*)::json)
    ) 
FROM 
(
    SELECT 
       t_id, substring(link, 52, 15) AS title, flugdatum AS last_editing_date, link AS filename, geometrie 
    FROM 
        agi_lidar_pub.lidarprodukte_lidarprodukt 
    WHERE 
        link LIKE '%lidar_2019.dtm/%'
) AS t;
```