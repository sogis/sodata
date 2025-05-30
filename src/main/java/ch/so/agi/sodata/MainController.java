package ch.so.agi.sodata;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
        headers.forEach((key, value) -> {
            log.debug(String.format("Header '%s' = %s", key, value));
        });
        log.debug("server name: " + request.getServerName());
        log.debug("context path: " + request.getContextPath());
        log.debug("ping"); 
        return new ResponseEntity<String>("sodata", HttpStatus.OK);
    }

}
