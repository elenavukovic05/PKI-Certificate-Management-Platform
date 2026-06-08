package pki_project.pki.dto;

public class AutogenerateResponseDTO {
    private Long certificateId;
    private String certificatePem;
    private String privateKeyPem;

    public AutogenerateResponseDTO(Long certificateId, String certificatePem, String privateKeyPem) {
        this.certificateId = certificateId;
        this.certificatePem = certificatePem;
        this.privateKeyPem = privateKeyPem;
    }

    public Long getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(Long certificateId) {
        this.certificateId = certificateId;
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public String getPrivateKeyPem() {
        return privateKeyPem;
    }

    public void setPrivateKeyPem(String privateKeyPem) {
        this.privateKeyPem = privateKeyPem;
    }
    
}
