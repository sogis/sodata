package ch.so.agi.sodata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringApplicationTests extends ApplicationTests {

    public SpringApplicationTests(@Autowired TestRestTemplate restTemplate) {
        super(restTemplate);
    }
}
