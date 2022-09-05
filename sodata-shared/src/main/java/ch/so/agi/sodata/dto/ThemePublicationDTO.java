package ch.so.agi.sodata.dto;

//import java.net.URI;
import java.util.Date;

public class ThemePublicationDTO {
    private String identifier;
    private String model;
    private Date lastPublishingDate;
    private Date secondToLastPublishingDate;
//    private Office owner;
//    private Office servicer;
//    private String furtherInformation;
//    private Bbox bbox;
//    private URI previewURL;
//    private Keywords keywords;
    
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public Date getLastPublishingDate() {
        return lastPublishingDate;
    }
    public void setLastPublishingDate(Date lastPublishingDate) {
        this.lastPublishingDate = lastPublishingDate;
    }
    public Date getSecondToLastPublishingDate() {
        return secondToLastPublishingDate;
    }
    public void setSecondToLastPublishingDate(Date secondToLastPublishingDate) {
        this.secondToLastPublishingDate = secondToLastPublishingDate;
    }
//    public Office getOwner() {
//        return owner;
//    }
//    public void setOwner(Office owner) {
//        this.owner = owner;
//    }
//    public Office getServicer() {
//        return servicer;
//    }
//    public void setServicer(Office servicer) {
//        this.servicer = servicer;
//    }
//    public String getFurtherInformation() {
//        return furtherInformation;
//    }
//    public void setFurtherInformation(String furtherInformation) {
//        this.furtherInformation = furtherInformation;
//    }
//    public Bbox getBbox() {
//        return bbox;
//    }
//    public void setBbox(Bbox bbox) {
//        this.bbox = bbox;
//    }
//    public URI getPreviewURL() {
//        return previewURL;
//    }
//    public void setPreviewURL(URI previewURL) {
//        this.previewURL = previewURL;
//    }
//    public Keywords getKeywords() {
//        return keywords;
//    }
//    public void setKeywords(Keywords keywords) {
//        this.keywords = keywords;
//    }
}
