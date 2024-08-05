package ch.so.agi.sodata;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ApplicationTests {
    static Logger logger = LoggerFactory.getLogger(ApplicationTests.class);

    @LocalServerPort
    protected String port;
    
    protected TestRestTemplate restTemplate;

    public ApplicationTests(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Test
    public void contextLoads() {
    }

    @Test
    public void index_Ok() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/index.html", String.class))
                .contains("Datenbezug • Kanton Solothurn");
    }
    
    @Test
    public void search_Ok() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/themepublications?query=afu", String.class))
                .contains("fliess");
    }
}
