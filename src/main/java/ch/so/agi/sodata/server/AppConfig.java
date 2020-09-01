package ch.so.agi.sodata.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import ch.so.agi.sodata.shared.Dataset;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private List<Dataset> datasets = new ArrayList<Dataset>();

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }
}
