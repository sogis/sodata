package ch.so.agi.sodata;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ApplicationTests {

    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;

	@Test
	void contextLoads() {	    
	}
	
    @Test
    public void index_Ok() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/index.html", String.class))
                .contains("Datenbezug • Kanton Solothurn");
    }
    
    // Dummy für Graal, damit Lucene verwendet wird.
    // TODO: inkl. Datasets
    @Test
    public void query_Ok() throws Exception {
        this.restTemplate.getForObject("http://localhost:" + port + "/datasets?query=av", String.class);
    }

}
