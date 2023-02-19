package ch.so.agi.sodata;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApplicationTests {

    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
    }

    @Test
    public void index_Ok() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/index.html", String.class))
                .contains("Datenbezug • Kanton Solothurn");
    }
    
    // Reminder to write tests for data repository
    // ilisite
    // ilimodels
    // ilidata
    // download file (?)
    @Test
    public void fail() {
        assertThat("foo".contains("bar"));
    }
    
    // Warum auskommentiert?
    
//    @Test
//    public void search_Ok() throws Exception {
//        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/themepublications?query=afu", String.class))
//                .contains("fliess");
//    }
}
