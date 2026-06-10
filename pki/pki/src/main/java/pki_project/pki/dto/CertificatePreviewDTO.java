package pki_project.pki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pki_project.pki.model.enums.CertificateStatus;
import pki_project.pki.model.enums.CertificateType;

import java.util.Date;

public class CertificatePreviewDTO {
    private Long id;
    private String subjectName;
    private String issuerName;
    private CertificateType type;
    private CertificateStatus status;
    private Date startDate;
    private Date endDate;
    boolean isCA;
    boolean digitalSignature;
    boolean keyEncipherment;
    boolean keyCertSign;
    boolean crlSign;
    private String certificatePem;
    private String organizationName;
    private boolean canDownloadPkcs12;

    public CertificatePreviewDTO(Long id,String subjectName, String issuerName, CertificateType type, CertificateStatus status, Date startDate, Date endDate, boolean isCA, boolean digitalSignature, boolean keyEncipherment, boolean keyCertSign, boolean crlSign, String certificatePem,  String organizationName, boolean canDownloadPkcs12) {
        this.id = id;
        this.subjectName = subjectName;
        this.issuerName = issuerName;
        this.type = type;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCA = isCA;
        this.digitalSignature = digitalSignature;
        this.keyEncipherment = keyEncipherment;
        this.keyCertSign = keyCertSign;
        this.crlSign = crlSign;
        this.certificatePem = certificatePem;
        this.organizationName = organizationName;
        this.canDownloadPkcs12 = canDownloadPkcs12;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
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

    public boolean getIsCA() {
        return isCA;
    }

    public void setIsCA(boolean CA) {
        isCA = CA;
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

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
  
    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public boolean isCanDownloadPkcs12() {
        return canDownloadPkcs12;
    }

    public void setCanDownloadPkcs12(boolean canDownloadPkcs12) {
        this.canDownloadPkcs12 = canDownloadPkcs12;
    }

}
