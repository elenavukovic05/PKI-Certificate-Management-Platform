package pki_project.pki.model;

import jakarta.persistence.*;
import pki_project.pki.model.enums.CertificateType;

import java.util.Date;

@Entity
@Table(name = "certificates")
public class CertificateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String serialNumber;
    @Column(nullable = false, length = 1000)
    private String subjectName;
    @ManyToOne
    @JoinColumn(name = "issuer_id")
    private CertificateEntity issuer;
    @Enumerated(EnumType.STRING)
    private CertificateType type;
    private Date startDate;
    private Date endDate;
    private boolean isRevoked;
    boolean isCA;
    boolean digitalSignature;
    boolean keyEncipherment;
    boolean keyCertSign;
    boolean crlSign;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String certificatePem;

    @Column(columnDefinition = "TEXT")
    private String encryptedPrivateKeyPem;

    public CertificateEntity() {
    }

    public CertificateEntity(String serialNumber, String subjectName,
                             CertificateEntity issuer, CertificateType type, Date startDate,
                             Date endDate, boolean isRevoked, boolean isCA, boolean digitalSignature,
                             boolean keyEncipherment, boolean keyCertSign, boolean crlSign, String certificatePem,
                             String encryptedPrivateKeyPem) {

        this.serialNumber = serialNumber;
        this.subjectName = subjectName;
        this.issuer = issuer;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isRevoked = isRevoked;
        this.isCA = isCA;
        this.digitalSignature = digitalSignature;
        this.keyEncipherment = keyEncipherment;
        this.keyCertSign = keyCertSign;
        this.crlSign = crlSign;
        this.certificatePem = certificatePem;
        this.encryptedPrivateKeyPem = encryptedPrivateKeyPem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public CertificateEntity getIssuer() {
        return issuer;
    }

    public void setIssuer(CertificateEntity issuer) {
        this.issuer = issuer;
    }

    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
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

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
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

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public String getEncryptedPrivateKeyPem() {
        return encryptedPrivateKeyPem;
    }

    public void setEncryptedPrivateKeyPem(String encryptedPrivateKeyPem) {
        this.encryptedPrivateKeyPem = encryptedPrivateKeyPem;
    }
}
