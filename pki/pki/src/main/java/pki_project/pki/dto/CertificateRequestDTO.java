package pki_project.pki.dto;

import pki_project.pki.model.enums.CertificateType;

import java.util.Date;
import java.util.List;

public class CertificateRequestDTO {
    String commonName;
    String name;
    String lastName;
    String organization;
    String email;
    String organizationalUnit;
    String country;
    Date startDate;
    Date endDate;
    Long issuerId;
    boolean isCA;
    boolean digitalSignature;
    boolean keyEncipherment;
    boolean keyCertSign;
    boolean crlSign;
    Integer pathLength;
    private List<String> subjectAlternativeNames;
    private Long organizationId;
    private CertificateType type;
    private Long ownerId;

    public CertificateRequestDTO(String commonName, String name, String lastName, String organization,
                                 String email, String organizationalUnit, String country, Date startDate,
                                 Date endDate, Long issuerId, boolean isCA, boolean digitalSignature,
                                 boolean keyEncipherment, boolean keyCertSign, boolean crlSign, Long organizationId, CertificateType type) {
        this.commonName = commonName;
        this.name = name;
        this.lastName = lastName;
        this.organization = organization;
        this.email = email;
        this.organizationalUnit = organizationalUnit;
        this.country = country;
        this.startDate = startDate;
        this.endDate = endDate;
        this.issuerId = issuerId;
        this.isCA = isCA;
        this.digitalSignature = digitalSignature;
        this.keyEncipherment = keyEncipherment;
        this.keyCertSign = keyCertSign;
        this.crlSign = crlSign;
        this.organizationId = organizationId;
        this.type = type;
    }

    public CertificateRequestDTO() {
    }

    public CertificateRequestDTO(String commonName, String name, String lastName, String organization, String email, String organizationalUnit, String country, Date startDate, Date endDate, Long issuerId, boolean isCA, boolean digitalSignature, boolean keyEncipherment, boolean keyCertSign, boolean crlSign, Integer pathLength, List<String> subjectAlternativeNames, Long organizationId, CertificateType type, Long ownerId) {
        this.commonName = commonName;
        this.name = name;
        this.lastName = lastName;
        this.organization = organization;
        this.email = email;
        this.organizationalUnit = organizationalUnit;
        this.country = country;
        this.startDate = startDate;
        this.endDate = endDate;
        this.issuerId = issuerId;
        this.isCA = isCA;
        this.digitalSignature = digitalSignature;
        this.keyEncipherment = keyEncipherment;
        this.keyCertSign = keyCertSign;
        this.crlSign = crlSign;
        this.pathLength = pathLength;
        this.subjectAlternativeNames = subjectAlternativeNames;
        this.organizationId = organizationId;
        this.type = type;
        this.ownerId = ownerId;
    }

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

    public List<String> getSubjectAlternativeNames() {
        return subjectAlternativeNames;
    }

    public void setSubjectAlternativeNames(List<String> subjectAlternativeNames) {
        this.subjectAlternativeNames = subjectAlternativeNames;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Long getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(Long issuerId) {
        this.issuerId = issuerId;
    }

    public boolean isCA() {
        return isCA;
    }

    public void setIsCA(boolean isCA) {
        this.isCA = isCA;
    }

    public boolean isDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(boolean digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public boolean isKeyEncipherment() {
        return keyEncipherment;
    }

    public void setKeyEncipherment(boolean keyEncipherment) {
        this.keyEncipherment = keyEncipherment;
    }

    public boolean isKeyCertSign() {
        return keyCertSign;
    }

    public void setKeyCertSign(boolean keyCertSign) {
        this.keyCertSign = keyCertSign;
    }

    public boolean isCrlSign() {
        return crlSign;
    }

    public void setCrlSign(boolean crlSign) {
        this.crlSign = crlSign;
    }

    public Integer getPathLength() {
        return pathLength;
    }

    public void setPathLength(Integer pathLength) {
        this.pathLength = pathLength;
    }

    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
