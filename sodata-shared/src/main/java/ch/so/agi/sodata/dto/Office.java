package ch.so.agi.sodata.dto;

import java.net.URI;

public class Office {
    private String agencyName;
    private String abbreviation;
    private String division;
    private URI officeAtWeb;
    private URI email;
    
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
    public URI getOfficeAtWeb() {
        return officeAtWeb;
    }
    public void setOfficeAtWeb(URI officeAtWeb) {
        this.officeAtWeb = officeAtWeb;
    }
    public URI getEmail() {
        return email;
    }
    public void setEmail(URI email) {
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
