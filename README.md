# sodata

## TODO
- Bug: Suchen -> backspace alle Zeichen -> nicht komplette Liste
- Bug: Firefox zeigt Aufklappen-Zeichen nicht bei Tabellen
- Link/Icon zu geocat.ch sollte auch beim hovern rot erscheinen.

- ilidata.xml: Gebietsauswahl adaptieren. Raster -> Verweis auf Subunits, dito bei Vektor?
- Lucene Suche
- Link zur Karte (siehe Mockup)
- versionierte Datensätze?
- ...

## Development

First Terminal:
```
./mvnw clean spring-boot:run
```

Second Terminal:
```
./mvnw gwt:generate-module gwt:codeserver
```

Or simple devmode (which worked better for java.xml.bind on client side):
```
./mvnw gwt:generate-module gwt:devmode 
```

Build fat jar and docker image:
```
GITHUB_RUN_NUMBER=9999 mvn clean package
```


### QGIS server
```
docker-compose build
```

```
http://localhost:8083/wms/subunits?SERVICE=WMS&REQUEST=GetCapabilities
```
