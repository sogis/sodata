package ch.so.agi.sodata;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@TestInstance(Lifecycle.PER_CLASS)
@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DockerApplicationTests extends ApplicationTests {
    private static int exposedPort = 8080;

    public static GenericContainer<?> appContainer = new GenericContainer<>(DockerImageName.parse("sogis/sodata:latest"))
            .waitingFor(Wait.forHttp("/actuator/health"))
            .withExposedPorts(8080)
//            .withCopyToContainer(Transferable.of("./src/test/java/resources.datasearch.xml"), "/config/datasearch.xml")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/datasearch.xml"), "/config/datasearch.xml")
            .withLogConsumer(new Slf4jLogConsumer(logger));

    public DockerApplicationTests(@Autowired TestRestTemplate restTemplate) {
        super(restTemplate);
    }

    @BeforeAll
    public void startContainers() {
        appContainer.start();
        
        // Damit die Tests den zufälligen Port des ilivalidator-web-services kennen und mit ihm
        // kommunizieren können.
        port = String.valueOf(appContainer.getMappedPort(exposedPort)); 
    }
    
    @AfterAll
    public void stopContainers() {
        appContainer.stop();
    }
}

