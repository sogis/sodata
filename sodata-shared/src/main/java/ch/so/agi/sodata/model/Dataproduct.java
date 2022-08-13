package ch.so.agi.sodata.model;

import java.util.List;

public class Dataproduct {
    private String ident;
    private String title;
    private String theAbstract;
    private boolean visibility;
    private int opacity;
    private List<Dataproduct> sublayers;
    private String parentIdent;
    private String parentTitle;
    
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
    public List<Dataproduct> getSublayers() {
        return sublayers;
    }
    public void setSublayers(List<Dataproduct> sublayers) {
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
}
