package pki_project.pki.dto;

import java.util.Date;

public class AutogenerateRequestDTO {
    private String commonName;
    private String name;
    private String lastName;
    private String organization;
    private String organizationalUnit;
    private String country;
    private String email;
    private Long issuerId;
    private Date startDate;
    private Date endDate;

    public String getCommonName() {
        return commonName;
    }
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    public String getOrganizationalUnit() {
        return organizationalUnit;
    }
    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Long getIssuerId() {
        return issuerId;
    }
    public void setIssuerId(Long issuerId) {
        this.issuerId = issuerId;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
