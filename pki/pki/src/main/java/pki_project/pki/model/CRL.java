package pki_project.pki.model;

import jakarta.persistence.*;

@Entity
public class CRL {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ca_certificate_id", nullable = false, unique = true)
    private CertificateEntity caCertificate;

    @Column(columnDefinition = "BYTEA", nullable = false)
    private byte[] crl;

    public CRL() {
    }

    public CRL(Long id, CertificateEntity caCertificate, byte[] crl) {
        this.id = id;
        this.caCertificate = caCertificate;
        this.crl = crl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CertificateEntity getCaCertificate() {
        return caCertificate;
    }

    public void setCaCertificate(CertificateEntity caCertificate) {
        this.caCertificate = caCertificate;
    }

    public byte[] getCrl() {
        return crl;
    }

    public void setCrl(byte[] crl) {
        this.crl = crl;
    }
}
