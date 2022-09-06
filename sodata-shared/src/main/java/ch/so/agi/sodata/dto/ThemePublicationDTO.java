package ch.so.agi.sodata.dto;

import java.util.List;

/*
 * - URL/URI kann nicht verwendet werden, da diese nicht emuliert werden.
 * - Date könnte verwendet werden, führt aber zu Umständen wegen Timezone, wohl 
 * auch weil Ursprungstyp LocalDate ist, was es eigentlich einfacher hätte machen
 * sollen.
 */

public class ThemePublicationDTO {
    private String identifier;
    private String model;
    private String title;
    private String shortDescription;
    private String lastPublishingDate;
    private String secondToLastPublishingDate;
    private OfficeDTO owner;
    private OfficeDTO servicer;
    private String furtherInformation;
//    private Bbox bbox; // benötige ich nicht für Lucene und GUI
    private String previewUrl;
    private List<String> keywords;
    private List<String> synonyms;
    private List<FileFormatDTO> fileFormats;
    private List<TableInfoDTO> tablesInfo;
    
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
    public String getLastPublishingDate() {
        return lastPublishingDate;
    }
    public void setLastPublishingDate(String lastPublishingDate) {
        this.lastPublishingDate = lastPublishingDate;
    }
    public String getSecondToLastPublishingDate() {
        return secondToLastPublishingDate;
    }
    public void setSecondToLastPublishingDate(String secondToLastPublishingDate) {
        this.secondToLastPublishingDate = secondToLastPublishingDate;
    }
    public OfficeDTO getOwner() {
        return owner;
    }
    public void setOwner(OfficeDTO owner) {
        this.owner = owner;
    }
    public OfficeDTO getServicer() {
        return servicer;
    }
    public void setServicer(OfficeDTO servicer) {
        this.servicer = servicer;
    }
    public String getFurtherInformation() {
        return furtherInformation;
    }
    public void setFurtherInformation(String furtherInformation) {
        this.furtherInformation = furtherInformation;
    }
//    public Bbox getBbox() {
//        return bbox;
//    }
//    public void setBbox(Bbox bbox) {
//        this.bbox = bbox;
//    }
    public String getPreviewUrl() {
        return previewUrl;
    }
    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
    public List<String> getKeywords() {
        return keywords;
    }
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    public List<String> getSynonyms() {
        return synonyms;
    }
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }
    public List<FileFormatDTO> getFileFormats() {
        return fileFormats;
    }
    public void setFileFormats(List<FileFormatDTO> fileFormats) {
        this.fileFormats = fileFormats;
    }
    public List<TableInfoDTO> getTablesInfo() {
        return tablesInfo;
    }
    public void setTablesInfo(List<TableInfoDTO> tablesInfo) {
        this.tablesInfo = tablesInfo;
    }
}
