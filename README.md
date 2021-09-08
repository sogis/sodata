# sodata

## Docs
- Index beim Hochfahren. Index im Pod, nicht persistent.
- Index: Leading wildcard ist momentan nicht umgesetzt -> Feedback abwarten. Falls notwendig, siehe "modelfinder".

## TODO
- Testing!
- ~~Bug: Suchen -> backspace alle Zeichen -> nicht komplette Liste~~ Id war in yml falsch resp. doppelt. Aus diesem Grund kam es zu doppelten Einträgen.
- ~~Bug: Firefox zeigt Aufklappen-Zeichen nicht bei Tabellen~~
- ~~Link/Icon zu geocat.ch sollte auch beim hovern rot erscheinen.~~ Nein. War eher ungewollt, da a:hover noch im css file vorhanden war.

- ilidata.xml: Gebietsauswahl adaptieren. Raster -> Verweis auf Subunits, dito bei Vektor?
- Lucene Suche
- Link zur Karte (siehe Mockup)
- versionierte Datensätze?
- ...

## Development

First Terminal:
```
./mvnw spring-boot:run -Penv-dev -pl *-server -am (-Dspring-boot.run.profiles=XXXX)
```

Second Terminal:
```
./mvnw gwt:codeserver -pl *-client -am
```

Or without downloading all the snapshots again:
```
./mvnw gwt:codeserver -pl *-client -am -nsu 
```

Build fat jar and docker image:
```
GITHUB_RUN_NUMBER=9999 mvn clean package
```

## Build
- foo

## Run
```
java -jar sodata-server/target/sodata.jar --spring.profiles.active=prod
```

## Testrequests
- Alle Datensätze: http://localhost:8080/datasets
- Suche: http://localhost:8080/datasets?query=admin


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