package ch.so.agi.sodata.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DownloadController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @RequestMapping(value = "/proxy", method = RequestMethod.GET)
    public String proxy(@RequestParam(required = true) String file) {
        log.info(file);
        return "redirect:" + file;
    }
}
