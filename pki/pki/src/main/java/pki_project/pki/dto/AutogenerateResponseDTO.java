package pki_project.pki.dto;

public class AutogenerateResponseDTO {
    private Long certificateId;
    private String certificatePem;
    private String pkcs12Base64;

    public AutogenerateResponseDTO(Long certificateId, String certificatePem, String pkcs12Base64) {
        this.certificateId = certificateId;
        this.certificatePem = certificatePem;
        this.pkcs12Base64 = pkcs12Base64;
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

    public String getPkcs12Base64() {
        return pkcs12Base64;
    }

    public void setPkcs12Base64(String pkcs12Base64) {
        this.pkcs12Base64 = pkcs12Base64;
    }
    
}
