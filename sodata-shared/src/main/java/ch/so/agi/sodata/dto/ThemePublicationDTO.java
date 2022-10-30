package ch.so.agi.sodata.dto;

import java.util.List;

/*
 * - URL/URI kann nicht verwendet werden, da diese nicht emuliert werden.
 * - Date könnte verwendet werden, führt aber zu Umständen wegen Timezone, wohl 
 * auch weil Ursprungstyp LocalDate ist, was es eigentlich einfacher hätte machen
 * sollen.
 * - BBOX wird weder für die Suche noch im GUI verwendet.
 */

public class ThemePublicationDTO {
    private String identifier;
    private String model;
    private String title;
    private String shortDescription;
    private boolean hasSubunits;
    private String lastPublishingDate;
    private String secondToLastPublishingDate;
    private BboxDTO bbox; // Stac. Benötigt es nicht für Datensuche.
    private OfficeDTO owner;
    private OfficeDTO servicer;
    private String furtherInformation;
    private String previewUrl;
    private List<String> keywords; // Backend. Benötigt es nicht für Datensuche.
    private List<String> synonyms; // Backend. Benötigt es nicht für Datensuche.
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
    public boolean isHasSubunits() {
        return hasSubunits;
    }
    public void setHasSubunits(boolean hasSubunits) {
        this.hasSubunits = hasSubunits;
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
    public BboxDTO getBbox() {
        return bbox;
    }
    public void setBbox(BboxDTO bbox) {
        this.bbox = bbox;
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
