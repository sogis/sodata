package ch.so.agi.sodata;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class ClientSettings {   
    private String filesServerUrl;

    public String getFilesServerUrl() {
        return filesServerUrl;
    }

    public void setFilesServerUrl(String filesServerUrl) {
        this.filesServerUrl = filesServerUrl;
    }
}
