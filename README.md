# sodata

## Beschreibung

Das Repository verwaltet den Quellcode der Datensuche. Die Datensuche ist das Web-GUI zum Beziehen von Geodaten.

## Komponenten

Die Datensuche besteht aus einer einzelnen Komponente (einer Webanwendung). Sie wiederum ist Bestandteil der funktionalen Einheit "Datenbezug" (https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/Datenbezug/Datenbezug.md).

## Konfigurieren und Starten

Die Anwendung kann am einfachsten mittels Env-Variablen gesteuert werden. Es stehen aber auch die normalen Spring Boot Konfigurationsmöglichkeiten zur Verfügung (siehe "Externalized Configuration").

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `CONFIG_FILE` | Vollständiger, absoluter Pfad der Themenbereitstellungs-Konfigurations-XML-Datei. | `/config/datasearch.xml` |
| `ITEMS_GEOJSON_DIR` | Verzeichnis, in das die GeoJSON-Dateien der Regionen gespeichert werden. Sämtliche JSON-Dateien in diesem Verzeichnis werden öffentlich exponiert. | `#{systemProperties['java.io.tmpdir']}` (= Temp-Verzeichnis des OS) |

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

Die Anwendung ist mit GWT und Spring Boot in Java geschrieben. Typischerweise ist eine solche Anwendung in drei Maven-Module unterteilt:

- sodata-server: Serverseitige Businesslogik. Erstellen des Suchindexes und Bereitstellen einer Such-API.
- sodata-client: GWT-Code aus dem die clientseitige Javascript-Anwendung transpiliert wird.
- sodata-shared: Gemeinsamer Code (Server und Client), insbesonderen DTO.

Die _datasearch.xml_-Datei wird zum Hochfahren benötigt, d.h. ohne Datei fährt die Anwendung nicht hoch. Dies ist ein bewusster Entscheid, kann aber auch wieder geändert werden. Es soll vor allem verhindert werden, dass die Anwendung vermeintlich korrekt funktioniert aber gar keine Liste anzeigt. Die _datasearch.xml_-Datei wird mit den Modell-Java-Klassen des _meta2file_-Projektes geparsed. Es wird eine Lucene-Index erzeugt. Der Index ist nur im Container persistiert und wird nach jedem Neustart des Containers neu erstellt, d.h. falls zwei Container/Pods laufen, verwenden sie nicht den identischen Index.

Für das Native Image sind entweder genügend Tests notwendig, damit die benötigte Information zusammengesammelt werden kann, oder die Anwendung muss mit dem Agent gestartet werden und manuell rumgeklickt werden.

Zum Entwicklen und Testen wird mit Profil-spezifischen application.yml-Dateien gearbeitet.

### Download der Daten
Die Daten auf einem anderen Server können mit einem Klick auf einen Button oder auf eine Einteilung in der Karte heruntergeladen werden. Es entsprich also eigentlich einem "Window.open()". Aus diesem Grund öffnet sich kurz ein neuer Tab, der sich aber sofort wieder schliesst. Eine elegantere Variante wäre, wenn man dies mit "fetch" in der Anwendung macht. Aber das geht nicht, weil Cors ein Problem ist (-> ProxyController) und bei sehr grossen Dateien wirkt es m.E. unlogischer und intransparenter. Es entstünde auch mehr Code. Aus diesem Grund auf der "hemdsärmlichen" Variante belassen.

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

Falls der Client (der Codeserver) nicht startet und es eine Fehlermeldung bezüglich eine bereits verwendeten Ports wirft, kann man den Codeserver auch abschiessen:

```
netstat -vanp tcp | grep 9876
```

Und anschliessendes `kill -9 <PID>`.

### Build

#### JVM
```
./mvnw -Penv-prod clean package -DexcludedGroups="docker"
```

#### Native

```
./mvnw -Penv-prod,native clean package -DexcludedGroups="docker"
```
Die _datasearch.xml_-Datei wird durch Testcontainers in den Container kopiert.

```
docker build -t sogis/sodata:latest -f sodata-server/src/main/docker/Dockerfile.native .
```

In diesem Fall muss das Native Image auf Linux gebuildet werden.

