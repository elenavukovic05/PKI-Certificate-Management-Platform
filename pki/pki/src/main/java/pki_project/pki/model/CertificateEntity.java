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

    // === SUBJECT podaci (denormalizovani iz Subject klase) ===
    @Column(nullable = false)
    private String subjectX500Name;  // X500Name.toString()

    @Column(nullable = false, columnDefinition = "TEXT")
    private String subjectPublicKey; // Base64 enkodovan

    // === ISSUER podaci (denormalizovani iz Issuer klase) ===
    @Column(nullable = false)
    private String issuerX500Name;   // X500Name.toString()
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String issuerPublicKey;  // Base64 enkodovan

    // === SERTIFIKAT IZDAVALAC (referenca na drugi CertificateEntity) ===
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
    private boolean isCA;
    private boolean digitalSignature;
    private boolean keyEncipherment;
    private boolean keyCertSign;
    private boolean crlSign;

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

    public CertificateEntity() {}

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

    public String getSubjectX500Name() {
        return subjectX500Name;
    }

    public void setSubjectX500Name(String subjectX500Name) {
        this.subjectX500Name = subjectX500Name;
    }

    public String getSubjectPublicKey() {
        return subjectPublicKey;
    }

    public void setSubjectPublicKey(String subjectPublicKey) {
        this.subjectPublicKey = subjectPublicKey;
    }

    public String getIssuerX500Name() {
        return issuerX500Name;
    }

    public void setIssuerX500Name(String issuerX500Name) {
        this.issuerX500Name = issuerX500Name;
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