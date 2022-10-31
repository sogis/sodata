# sodata

## todo
- Doku
 * Suchindex beim Hochfahren. Index im Pod, nicht persistent.
- Testing
 * https://stackoverflow.com/questions/39690094/spring-boot-default-profile-for-integration-tests/56442693
- ...

## Beschreibung

Das Repository verwaltet den Quellcode der Datensuche. Die Datensuche ist das Web-GUI zum Beziehen von Geodaten.

## Komponenten

Die Datensuche besteht aus einer einzelnen Komponente (einer Webanwendung). Sie wiederum ist Bestandteil der funktionalen Einheit "Datenbezug" (https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/Datenbezug/Datenbezug.md).

## Konfigurieren und Starten

Die Anwendung kann am einfachsten mittels Env-Variablen gesteuert werden. Es stehen aber auch die normalen Spring Boot Konfigurationsmöglichkeiten zur Verfügung (siehe "Externalized Configuration").

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `CONFIG_FILE` | Vollständiger, absoluter Pfad der Themebereitstellungs-Konfigurations-XML-Datei. | `/config/datasearch.xml` |
| `ITEMS_GEOJSON_DIR` | Verzeichnis in das die GeoJSON-Dateien der Regionen gespeichert werden. Sämtliche JSON-Dateien in diesem Verzeichnis werden öffentlich exponiert. | `#{systemProperties['java.io.tmpdir']}` (= Temp-Verzeichnis des OS) |
| `FILES_SERVER_URL` | Url des Servers, auf dem die Geodaten gespeichert sind. | `https://files.geo.so.ch` |


### Java



### Docker





## Externe Abhängigkeiten

## Konfiguration und Betrieb in der GDI

## Interne Struktur

gwt... json... ..native


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

Test single class in subproject:
```
./mvnw test -Dtest=GeoJsonWriterTest -pl sodata-server
```

## Build

### JVM
```
./mvnw -Penv-prod clean package
```

```
docker build -t sogis/sodata-jvm:latest -f sodata-server/src/main/docker/Dockerfile.jvm .
```

~~Siehe Dockerfile: Die Datensatz-Konfiguration wird unter `/config/datasets.yml` erwartet. Ohne diese Datei bleibt die Tabelle im Browser leer. Siehe Kapitel "Konfiguration" für zusätzliche Informationen.~~

### Native
```
CONFIG_FILE=$PWD/sodata-server/datasearch.xml ITEMS_GEOJSON_DIR=/tmp/ ./mvnw clean -Pnative test
./mvnw -DskipTests -Penv-prod,native package
```

```
docker build -t sogis/sodata:latest -f sodata-server/src/main/docker/Dockerfile.native .
```

In diesem Fall muss das native image auf Linux gebuildet werden.


## Run

### JVM
```
java -jar sodata-server/target/sodata.jar --spring.config.location=path/to/datasearch.xml
```

```
docker run -p8080:8080 -v /path/to/datasearch.xml:/config/datasearch.xml sogis/sodata-jvm:latest
```

### Native
```
./sodata-server/target/sodata-server --spring.config.location=classpath:/application.yml,optional:file:/Users/stefan/tmp/datasets.yml
```

```
docker run -p8080:8080 -v /path/to/datasearch.xml:/config/datasearch.xml sogis/sodata:latest
```

## Konfiguration (Umgebungsvariablen)

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `CONFIG_FILE` | Der Pfad zur XML-Config-Datei. | `/app/config.xml` |
| `LOG_LEVEL_FRAMEWORK` | Das Logging-Level des Spring Boot Frameworks. | `info` |
| `LOG_LEVEL_APPLICATION` | Das Logging-Level der Anwendung (= selber geschriebener Code). | `info` (**ändern**)|
| `LOG_LEVEL_DB_CONNECTION_POOL` | **FIXME** Das Logging-Level des DB-Connection-Poolsocket. | `info` |



### wird anders werden / deprecated
Es gibt zwei Konfigurationsdateien: 

- `application.yml`: Steuert die Anwendung im Allgemeinen, z.B. Loglevel oder Anzahl Suchresultate.
- `datasets.yml`: Beinhaltet die Datensätze, die angeboten werden und in der Tabelle im Browserfenster angezeigt werden. 

In der Annahme, dass die Applikations-Konfiguration eher statisch ist, ist sie in der Anwendung gespeichert. Einige Werte können bereits jetzt mittels ENV-Variable überschrieben werden. Auch kann mittels Profilen (`application-<PROFIL>.yml`) zusätzliche Konfiguration übermittelt werden. In diesem Fall muss das Profil gesetzt werden und die YAML-Datei dem Container oder der Jar-Datei verfügbar gemacht werden.

Die Datensatz-Konfiguration ist eher dynamisch, d.h. sie ändert häufig. Aus diesem Grund wird sie nicht in das Image oder in die Jar-Datei "gebrannt" und muss immer beim Starten der Anwendung mitangegeben werden. Der Docker-Container erwartet unter `/config/datasets.yml` die Datensatzkonfigurationsdatei. Der Container startet auch ohne diese Datei. In diesem Fall bleibt die Tabelle mit den Datensätzen leer.

## Varia

### SQL-Queries für Subunit-Geojson

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