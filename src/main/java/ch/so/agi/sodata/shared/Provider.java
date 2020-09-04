package ch.so.agi.sodata.shared;

import java.util.List;

public class Provider {
    private String name;
    private List<Dataset> datasets;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Dataset> getDatasets() {
        return datasets;
    }
    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }
}
