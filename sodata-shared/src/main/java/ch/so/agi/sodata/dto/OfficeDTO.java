package ch.so.agi.sodata.dto;

//import com.google.gwt.http.client.URL;

public class OfficeDTO {
    private String agencyName;
    private String abbreviation;
    private String division;
    private String officeAtWeb;
    private String email;
    
    public String getAgencyName() {
        return agencyName;
    }
    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }
    public String getAbbreviation() {
        return abbreviation;
    }
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    public String getDivision() {
        return division;
    }
    public void setDivision(String division) {
        this.division = division;
    }
    public String getOfficeAtWeb() {
        return officeAtWeb;
    }
    public void setOfficeAtWeb(String officeAtWeb) {
        this.officeAtWeb = officeAtWeb;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    private String phone;
}
