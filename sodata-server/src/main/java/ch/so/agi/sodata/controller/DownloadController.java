package ch.so.agi.sodata.controller;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class DownloadController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private HttpClient httpClient;
    
    @PostConstruct
    public void init() throws Exception {
        httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                //.followRedirects(Redirect.NEVER)
                .build();
    }

//    @RequestMapping(value = "user/avatar/{userId}", method = RequestMethod.GET)
//    @ResponseBody
//    public ResponseEntity<InputStreamResource> downloadUserAvatarImage(@PathVariable Long userId) {
//        GridFSDBFile gridFsFile = fileService.findUserAccountAvatarById(userId);
//
//        return ResponseEntity.ok()
//                .contentLength(gridFsFile.getLength())
//                .contentType(MediaType.parseMediaType(gridFsFile.getContentType()))
//                .body(new InputStreamResource(gridFsFile.getInputStream()));
//    }
    
    // generische "Binary"-Mediatype.
    // APPLICATION_OCTET_STREAM oder so.

    @RequestMapping(value = "/files/{request}", method = RequestMethod.GET, produces = { "application/xml" })
    public ResponseEntity<String> proxy(String request) {
        
        
        
        return null;
    } 
    
}
