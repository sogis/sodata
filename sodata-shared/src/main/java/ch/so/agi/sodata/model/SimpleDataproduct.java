package ch.so.agi.sodata.model;

public class SimpleDataproduct {
    private String dataproductId;
    private String title;
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
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
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
