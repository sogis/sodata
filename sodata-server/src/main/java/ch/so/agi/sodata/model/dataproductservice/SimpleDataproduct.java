package ch.so.agi.sodata.model.dataproductservice;

public class SimpleDataproduct {
    private String dataproductId;
    private String display;
    private String layerAbstract;
    private boolean visibility;
    private int opacity;
    private String layerGroupDataproductId;
    private String layerGroupDisplay;
    
    public String getDataproductId() {
        return dataproductId;
    }
    public void setDataproductId(String dataproductId) {
        this.dataproductId = dataproductId;
    }
    public String getDisplay() {
        return display;
    }
    public void setDisplay(String display) {
        this.display = display;
    }
    public String getLayerAbstract() {
        return layerAbstract;
    }
    public void setLayerAbstract(String layerAbstract) {
        this.layerAbstract = layerAbstract;
    }
    public boolean isVisibility() {
        return visibility;
    }
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
    public int getOpacity() {
        return opacity;
    }
    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }
    public String getLayerGroupDataproductId() {
        return layerGroupDataproductId;
    }
    public void setLayerGroupDataproductId(String layerGroupDataproductId) {
        this.layerGroupDataproductId = layerGroupDataproductId;
    }
    public String getLayerGroupDisplay() {
        return layerGroupDisplay;
    }
    public void setLayerGroupDisplay(String layerGroupDisplay) {
        this.layerGroupDisplay = layerGroupDisplay;
    }
}
