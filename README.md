# sodata

## todo
- Testing
  * https://stackoverflow.com/questions/39690094/spring-boot-default-profile-for-integration-tests/56442693
- ...

## Beschreibung

Das Repository verwaltet den Quellcode der Datensuche. Die Datensuche ist das Web-GUI zum Beziehen von Geodaten.

## Komponenten

Die Datensuche besteht aus einer einzelnen Komponente (einer Webanwendung). Sie wiederum ist Bestandteil der funktionalen Einheit "Datenbezug" (https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/Datenbezug/Datenbezug.md).

## Konfigurieren und Starten

Die Anwendung kann am einfachsten mittels Env-Variablen gesteuert werden. Es stehen aber auch die normalen Spring Boot Konfigurationsmöglichkeiten zur Verfügung (siehe "Externalized Configuration").

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `CONFIG_FILE` | Vollständiger, absoluter Pfad der Themebereitstellungs-Konfigurations-XML-Datei. | `/config/datasearch.xml` |
| `ITEMS_GEOJSON_DIR` | Verzeichnis, in das die GeoJSON-Dateien der Regionen gespeichert werden. Sämtliche JSON-Dateien in diesem Verzeichnis werden öffentlich exponiert. | `#{systemProperties['java.io.tmpdir']}` (= Temp-Verzeichnis des OS) |
| `FILES_SERVER_URL` | Url des Servers, auf dem die Geodaten gespeichert sind. | `https://files.geo.so.ch` |


### Java

Falls die _datasearch.xml_-Datei im Verzeichnis _/config/_ vorliegt, reicht:
```
java -jar sodata-server/target/sodata.jar 
```

Ansonsten kann die Datei explizit angegeben werden:

```
java -jar sodata-server/target/sodata.jar --app.configFile=/path/to/datasearch.xml
```

### Native Image

Analog Java:

```
./sodata-server/target/sodata-server [...]
```

### Docker

Die _datasearch.xml_-Datei kann direkt in das Image gebrannt werden. In diesem Fall sollte sie in den Ordner _/config/_ gebrannt werden, was zu folgendem Start-Befehl führt:

```
docker run -p8080:8080 sogis/sodata:latest
```

Wird die Datei nicht in das Image gebrannt, ergibt sich folgender Befehl:

```
docker run -p8080:8080 -v /path/to/datasearch.xml:/config/datasearch.xml sogis/sodata:latest
```

## Externe Abhängigkeiten

Keine.

## Konfiguration und Betrieb in der GDI

@Michael: todo

## Interne Struktur

@Stefan: todo

- GWT
- Maven Multimodule Projekt: Was ist wo?
- datasearch.xml: Parsen -> Lucene. Kann nur hochgefahrne werden, falls vorhanden (bewusster Entscheid)
- Native Image
- Lucene-Index ist nicht persistiert. Wenn zwei Container hochgefahren werden, gibt es zwei (hoffentlich) gleiche Indizes.
- Zum Entwicklen und Testen wird mit Profil-spezifischen application.yml-Dateien gearbeitet.

## Entwicklung

### Run 
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

Test single class in subproject:
```
./mvnw test -Dtest=GeoJsonWriterTest -pl sodata-server
```

### Build

#### JVM
```
./mvnw -Penv-prod clean package
```

```
docker build -t sogis/sodata-jvm:latest -f sodata-server/src/main/docker/Dockerfile.jvm .
```


#### Native
Damit die Tests mit dem Native Image funktionieren, muss mittels Env-Variablen passend konfiguriert werden:

```
CONFIG_FILE=$PWD/sodata-server/datasearch.xml ITEMS_GEOJSON_DIR=/tmp/ ./mvnw clean -Pnative test
./mvnw -DskipTests -Penv-prod,native package
```

```
docker build -t sogis/sodata:latest -f sodata-server/src/main/docker/Dockerfile.native .
```

In diesem Fall muss das Native Image auf Linux gebuildet werden.

