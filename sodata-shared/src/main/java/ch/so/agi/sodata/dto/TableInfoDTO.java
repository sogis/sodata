package ch.so.agi.sodata.dto;

public class TableInfoDTO {
    private String sqlName;
    private String title;
    private String shortDescription;
    
    public String getSqlName() {
        return sqlName;
    }
    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
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
}
