package ch.so.agi.sodata.dto;

import java.util.List;

public class DataproductDTO {
    private String ident;
    private String title;
    private String theAbstract;
    private boolean visibility;
    private int opacity;
    private List<DataproductDTO> sublayers;
    private String parentIdent;
    private String parentTitle;
    private String themeTitle;
    
    public String getIdent() {
        return ident;
    }
    public void setIdent(String ident) {
        this.ident = ident;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTheAbstract() {
        return theAbstract;
    }
    public void setTheAbstract(String theAbstract) {
        this.theAbstract = theAbstract;
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
    public List<DataproductDTO> getSublayers() {
        return sublayers;
    }
    public void setSublayers(List<DataproductDTO> sublayers) {
        this.sublayers = sublayers;
    }
    public String getParentIdent() {
        return parentIdent;
    }
    public void setParentIdent(String parentIdent) {
        this.parentIdent = parentIdent;
    }
    public String getParentTitle() {
        return parentTitle;
    }
    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }
    public String getThemeTitle() {
        return themeTitle;
    }
    public void setThemeTitle(String themeTitle) {
        this.themeTitle = themeTitle;
    }
}