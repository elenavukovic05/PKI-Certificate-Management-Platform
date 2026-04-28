package pki_project.pki.dto;

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

    public CertificatePreviewDTO(Long id,String subjectName, String issuerName, CertificateType type, CertificateStatus status, Date startDate, Date endDate, boolean isCA, boolean digitalSignature, boolean keyEncipherment, boolean keyCertSign, boolean crlSign) {
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

    public boolean isCA() {
        return isCA;
    }

    public void setCA(boolean CA) {
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
}
