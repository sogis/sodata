package ch.so.agi.sodata.shared;

import jsinterop.annotations.JsType;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
public class Dataset {
    private String id;
    private String version;
    private String owner;
    private String epsgCode;
    private String resolutionScope;
    private String publishingDate;
    private String lastEditingDate;
    private String title;
    private String shortDescription;
    private String keywords;
    private String servicer;
    private String technicalContact;
    private String furtherInformation;
    private String furtherMetadata;
    private String knownWMS;
    private String[] files;
    
    @JsProperty
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @JsProperty    
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    @JsProperty    
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    @JsProperty    
    public String getEpsgCode() {
        return epsgCode;
    }
    public void setEpsgCode(String epsgCode) {
        this.epsgCode = epsgCode;
    }
    @JsProperty    
    public String getResolutionScope() {
        return resolutionScope;
    }
    public void setResolutionScope(String resolutionScope) {
        this.resolutionScope = resolutionScope;
    }
    @JsProperty    
    public String getPublishingDate() {
        return publishingDate;
    }
    public void setPublishingDate(String publishingDate) {
        this.publishingDate = publishingDate;
    }
    @JsProperty
    public String getLastEditingDate() {
        return lastEditingDate;
    }
    public void setLastEditingDate(String lastEditingDate) {
        this.lastEditingDate = lastEditingDate;
    }
    @JsProperty
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @JsProperty
    public String getShortDescription() {
        return shortDescription;
    }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    @JsProperty
    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    @JsProperty
    public String getServicer() {
        return servicer;
    }
    public void setServicer(String servicer) {
        this.servicer = servicer;
    }
    @JsProperty
    public String getTechnicalContact() {
        return technicalContact;
    }
    public void setTechnicalContact(String technicalContact) {
        this.technicalContact = technicalContact;
    }
    @JsProperty
    public String getFurtherInformation() {
        return furtherInformation;
    }
    public void setFurtherInformation(String furtherInformation) {
        this.furtherInformation = furtherInformation;
    }
    @JsProperty
    public String getFurtherMetadata() {
        return furtherMetadata;
    }
    public void setFurtherMetadata(String furtherMetadata) {
        this.furtherMetadata = furtherMetadata;
    }
    @JsProperty
    public String getKnownWMS() {
        return knownWMS;
    }
    public void setKnownWMS(String knownWMS) {
        this.knownWMS = knownWMS;
    }
    @JsProperty
    public String[] getFiles() {
        return files;
    }
    public void setFiles(String[] files) {
        this.files = files;
    } 
}
