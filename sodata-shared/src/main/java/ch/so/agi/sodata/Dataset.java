package ch.so.agi.sodata;

import java.util.List;

public class Dataset {
    private String id;
    private String version;
    private String owner;
    private String model;
    private String epsgCode;
    private String resolutionScope;
    private String publishingDate; 
    private String lastEditingDate; // TODO: Date type binding ist nicht möglich, da es dazu eine Spring-Annotation benötigt, die im shared-Modul nicht sichtbar ist.
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
    private List<String> fileFormats;
    private List<DatasetTable> tables;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getEpsgCode() {
        return epsgCode;
    }
    public void setEpsgCode(String epsgCode) {
        this.epsgCode = epsgCode;
    }
    public String getResolutionScope() {
        return resolutionScope;
    }
    public void setResolutionScope(String resolutionScope) {
        this.resolutionScope = resolutionScope;
    }
    public String getPublishingDate() {
        return publishingDate;
    }
    public void setPublishingDate(String publishingDate) {
        this.publishingDate = publishingDate;
    }
    public String getLastEditingDate() {
        return lastEditingDate;
    }
    public void setLastEditingDate(String lastEditingDate) {
        this.lastEditingDate = lastEditingDate;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getShortDescription() {
        return shortDescription;
    }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    public String getServicer() {
        return servicer;
    }
    public void setServicer(String servicer) {
        this.servicer = servicer;
    }
    public String getTechnicalContact() {
        return technicalContact;
    }
    public void setTechnicalContact(String technicalContact) {
        this.technicalContact = technicalContact;
    }
    public String getFurtherInformation() {
        return furtherInformation;
    }
    public void setFurtherInformation(String furtherInformation) {
        this.furtherInformation = furtherInformation;
    }
    public String getFurtherMetadata() {
        return furtherMetadata;
    }
    public void setFurtherMetadata(String furtherMetadata) {
        this.furtherMetadata = furtherMetadata;
    }
    public String getKnownWMS() {
        return knownWMS;
    }
    public void setKnownWMS(String knownWMS) {
        this.knownWMS = knownWMS;
    }
    public String getSubunits() {
        return subunits;
    }
    public void setSubunits(String subunits) {
        this.subunits = subunits;
    }
    public List<String> getFileFormats() {
        return fileFormats;
    }
    public void setFileFormats(List<String> fileFormats) {
        this.fileFormats = fileFormats;
    }
    public List<DatasetTable> getTables() {
        return tables;
    }
    public void setTables(List<DatasetTable> tables) {
        this.tables = tables;
    }
}
