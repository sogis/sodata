package ch.so.agi.sodata.shared;

import jsinterop.annotations.JsType;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
public final class Dataset {
    private String id;
    private String version;
    private String owner;
    private String model;
    private String epsgCode;
    private String resolutionScope;
    private String publishingDate;
    private String lastEditingDate;
    private String title;
    private String shortDescription;
    private String keywords;
    private String provider;
    private String servicer;
    private String technicalContact;
    private String furtherInformation;
    private String furtherMetadata;
    private String knownWMS;
    private String subunits;
    private String[] files;
    
    @JsOverlay
    public String getId() {
        return id;
    }
    @JsOverlay
    public void setId(String id) {
        this.id = id;
    }
    @JsOverlay
    public String getVersion() {
        return version;
    }
    @JsOverlay
    public void setVersion(String version) {
        this.version = version;
    }
    @JsOverlay    
    public String getOwner() {
        return owner;
    }
    @JsOverlay
    public void setOwner(String owner) {
        this.owner = owner;
    }
    @JsOverlay    
    public String getModel() {
        return model;
    }
    @JsOverlay
    public void setModel(String model) {
        this.model = model;
    }
    @JsOverlay    
    public String getEpsgCode() {
        return epsgCode;
    }
    @JsOverlay
    public void setEpsgCode(String epsgCode) {
        this.epsgCode = epsgCode;
    }
    @JsOverlay    
    public String getResolutionScope() {
        return resolutionScope;
    }
    @JsOverlay
    public void setResolutionScope(String resolutionScope) {
        this.resolutionScope = resolutionScope;
    }
    @JsOverlay
    public String getPublishingDate() {
        return publishingDate;
    }
    @JsOverlay
    public void setPublishingDate(String publishingDate) {
        this.publishingDate = publishingDate;
    }
    @JsOverlay
    public String getLastEditingDate() {
        return lastEditingDate;
    }
    @JsOverlay
    public void setLastEditingDate(String lastEditingDate) {
        this.lastEditingDate = lastEditingDate;
    }
    @JsOverlay
    public String getTitle() {
        return title;
    }
    @JsOverlay
    public void setTitle(String title) {
        this.title = title;
    }
    @JsOverlay
    public String getShortDescription() {
        return shortDescription;
    }
    @JsOverlay
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    @JsOverlay
    public String getKeywords() {
        return keywords;
    }
    @JsOverlay
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    @JsOverlay    
    public String getProvider() {
        return provider;
    }
    @JsOverlay    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    @JsOverlay
    public String getServicer() {
        return servicer;
    }
    @JsOverlay
    public void setServicer(String servicer) {
        this.servicer = servicer;
    }
    @JsOverlay
    public String getTechnicalContact() {
        return technicalContact;
    }
    @JsOverlay
    public void setTechnicalContact(String technicalContact) {
        this.technicalContact = technicalContact;
    }
    @JsOverlay
    public String getFurtherInformation() {
        return furtherInformation;
    }
    @JsOverlay
    public void setFurtherInformation(String furtherInformation) {
        this.furtherInformation = furtherInformation;
    }
    @JsOverlay
    public String getFurtherMetadata() {
        return furtherMetadata;
    }
    @JsOverlay
    public void setFurtherMetadata(String furtherMetadata) {
        this.furtherMetadata = furtherMetadata;
    }
    @JsOverlay
    public String getKnownWMS() {
        return knownWMS;
    }
    @JsOverlay
    public void setKnownWMS(String knownWMS) {
        this.knownWMS = knownWMS;
    }
    @JsOverlay
    public String getSubunits() {
        return subunits;
    }
    @JsOverlay
    public void setSubunits(String subunits) {
        this.subunits = subunits;
    }    
    @JsOverlay
    public String[] getFiles() {
        return files;
    }
    @JsOverlay    
    public void setFiles(String[] files) {
        this.files = files;
    } 
}
