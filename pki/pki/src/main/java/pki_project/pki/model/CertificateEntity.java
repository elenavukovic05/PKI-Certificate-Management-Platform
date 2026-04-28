package pki_project.pki.model;

import jakarta.persistence.*;
import pki_project.pki.model.enums.CertificateStatus;
import pki_project.pki.model.enums.CertificateType;

import java.util.Date;

@Entity
@Table(name = "certificates")
public class CertificateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    // === SUBJECT podaci (denormalizovani) ===
    @Column(nullable = false, length = 1000)
    private String subjectName;  // X500Name.toString()

    @Column(nullable = false, columnDefinition = "TEXT")
    private String subjectPublicKey; // Base64 enkodovan

    // === ISSUER podaci (denormalizovani) ===
    @Column(nullable = false, length = 1000)
    private String issuerName;   // X500Name.toString()

    @Column(nullable = false, columnDefinition = "TEXT")
    private String issuerPublicKey;  // Base64 enkodovan

    // === SERTIFIKAT IZDAVALAC (referenca na CertificateEntity koji je potpisao ovaj) ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_certificate_id")
    private CertificateEntity issuerCertificate;

    // === TIP I STATUS ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateStatus status = CertificateStatus.VALID;

    // === DATUMI ===
    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date endDate;

    // === EKSTENZIJE ===
    boolean isCA;
    boolean digitalSignature;
    boolean keyEncipherment;
    boolean keyCertSign;
    boolean crlSign;

    // === PODACI SERTIFIKATA ===
    @Column(nullable = false, columnDefinition = "TEXT")
    private String certificatePem;  // ceo sertifikat u PEM formatu

    @Column(columnDefinition = "TEXT")
    private String encryptedPrivateKey;  // AES/GCM enkriptovan, Base64

    // === VLASNIŠTVO ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;  // za EE sertifikate

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // === POVLAČENJE ===
    private String revocationReason;

    // === KONSTRUKTORI ===
    public CertificateEntity() {
    }

    public CertificateEntity(String serialNumber, String subjectName,
                             CertificateEntity issuerCertificate, CertificateType type, Date startDate,
                             Date endDate, boolean isCA, boolean digitalSignature,
                             boolean keyEncipherment, boolean keyCertSign, boolean crlSign,
                             String certificatePem, String encryptedPrivateKey) {
        this.serialNumber = serialNumber;
        this.subjectName = subjectName;
        this.issuerCertificate = issuerCertificate;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCA = isCA;
        this.digitalSignature = digitalSignature;
        this.keyEncipherment = keyEncipherment;
        this.keyCertSign = keyCertSign;
        this.crlSign = crlSign;
        this.certificatePem = certificatePem;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.status = CertificateStatus.VALID;
    }

    // === GETTERI I SETTERI ===
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

    public String getSubjectPublicKey() {
        return subjectPublicKey;
    }

    public void setSubjectPublicKey(String subjectPublicKey) {
        this.subjectPublicKey = subjectPublicKey;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getIssuerPublicKey() {
        return issuerPublicKey;
    }

    public void setIssuerPublicKey(String issuerPublicKey) {
        this.issuerPublicKey = issuerPublicKey;
    }

    public CertificateEntity getIssuerCertificate() {
        return issuerCertificate;
    }

    public void setIssuerCertificate(CertificateEntity issuerCertificate) {
        this.issuerCertificate = issuerCertificate;
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

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }
}