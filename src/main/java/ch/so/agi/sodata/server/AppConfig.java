package ch.so.agi.sodata.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private List<Dataset> datasets = new ArrayList<Dataset>();

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }
    
    public static class Dataset {
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
        private List<String> files;
        
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
        public List<String> getFiles() {
            return files;
        }
        public void setFiles(List<String> files) {
            this.files = files;
        }
    }
}
