package ch.so.agi.sodata.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ch.so.agi.sodata.service.ConfigService;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/*
 * Die ilidata.xml-Datei wird beim Parsen der XML-Datei hergestellt und ausserhalb der Anwendung in einem Verzeichnis gespeichert.
 * Es wird kein Controller benötigt, sie wird als statische Ressource publiziert (siehe AppWebMvcConfig.java).
 */
@Controller
public class InterlisRepositoryController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ConfigService configService;

    @Autowired
    private HttpClient httpClient;

    private static final String WORK_DIRECTORY = System.getProperty("java.io.tmpdir");
    
    private static final String WORK_DIRECTORY_PREFIX = "sodata_files";
    
    private static final String DEFAULT_DOWNLOAD_HOST_URL = "https://files.geo.so.ch";
    
    @GetMapping(value="/ilisite.xml")
    public ResponseEntity<?> getIliSite() {
        InputStream is = getClass()
                .getResourceAsStream("/ili/ilisite.xml");
        
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_XML).body(new InputStreamResource(is));            
    }
    
    @GetMapping(value="/ilimodels.xml")
    public ResponseEntity<?> getIliModels() {
        InputStream is = getClass()
                .getResourceAsStream("/ili/ilimodels.xml");
        
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_XML).body(new InputStreamResource(is));            
    }
    
    @GetMapping(value="/files/{filename}")
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        try {
            Path tmpWorkDir = Files.createTempDirectory(Paths.get(WORK_DIRECTORY), WORK_DIRECTORY_PREFIX);
            
            String identifier;
            if (filename.startsWith("ch.")) {
                identifier = filename.substring(0,filename.length()-4);
            } else {                
                identifier = filename.substring(filename.indexOf(".")+1,filename.length()-4);                
            }
            
            String downloadHostUrl = configService.getThemePublicationMap().get(identifier).getDownloadHostUrl();
            if (downloadHostUrl == null) {
                downloadHostUrl = DEFAULT_DOWNLOAD_HOST_URL;
            }
            String requestUrl = downloadHostUrl + "/" + identifier + "/aktuell/" + filename + ".zip";

                        
            File zipFile = Paths.get(tmpWorkDir.toFile().getAbsolutePath(), filename + ".zip").toFile();
            HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(new URI(requestUrl))
                    .timeout(Duration.ofSeconds(120L)).build();
            HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            saveFile(response.body(), zipFile.getAbsolutePath());

            try (ZipFile zip = new ZipFile(zipFile)) {
                zip.extractAll(tmpWorkDir.toFile().getAbsolutePath());
            } catch (ZipException e) {
                log.error(e.getMessage());
                throw new IOException(e);
            }
            
            MediaType mediaType = MediaType.APPLICATION_XML;
            if (filename.toLowerCase().endsWith("itf")) mediaType = MediaType.TEXT_PLAIN; 
            
            File dataFile = Path.of(tmpWorkDir.toFile().getAbsolutePath(), filename).toFile();
            InputStream is = new java.io.FileInputStream(dataFile);
            return ResponseEntity
                    .ok().header("content-disposition", "attachment; filename=" + dataFile.getName())
                    .contentLength(dataFile.length())
                    .contentType(MediaType.APPLICATION_XML).body(new InputStreamResource(is));            
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }        
    }
    
    private static void saveFile(InputStream body, String destinationFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(destinationFile);
        fos.write(body.readAllBytes());
        fos.close();
    }

    @Scheduled(cron="0 0 0/6 * * *")
    //@Scheduled(fixedRate = 10000)
    public void cleanUp() {    
        log.info("Cronjob - deleting old files.");
        java.io.File[] tmpDirs = new java.io.File(WORK_DIRECTORY).listFiles();
        if(tmpDirs!=null) {
            for (java.io.File tmpDir : tmpDirs) {
                if (tmpDir.getName().startsWith(WORK_DIRECTORY_PREFIX)) {
                    try {
                        FileTime creationTime = (FileTime) Files.getAttribute(Paths.get(tmpDir.getAbsolutePath()), "creationTime");                    
                        Instant now = Instant.now();
                        
                        long fileAge = now.getEpochSecond() - creationTime.toInstant().getEpochSecond();                        
                        if (fileAge > 60*60*3) {
                            log.debug("deleting {}", tmpDir.getAbsolutePath());
                            FileSystemUtils.deleteRecursively(tmpDir);
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
}
