# sodata

## TODO
- Modal via URL (z.B localhost:8080/ch.so.awjf.forstkreise)
- ilidata.xml
- Lucene Suche
- group by Amt

## Development

First Terminal:
```
mvn clean spring-boot:run
```

Second Terminal:
```
mvn gwt:generate-module gwt:codeserver
```

Or simple devmode (which worked better for java.xml.bind on client side):
```
mvn gwt:generate-module gwt:devmode 
```

Build fat jar and docker image:
```
GITHUB_RUN_NUMBER=9999 mvn clean package
```
