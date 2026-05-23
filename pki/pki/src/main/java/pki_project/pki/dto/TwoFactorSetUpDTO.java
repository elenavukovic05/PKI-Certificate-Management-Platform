package pki_project.pki.dto;

public class TwoFactorSetUpDTO {
    private String secret;   // manuel entering setup key in auth app
    private String qrUrl;    // otpauth:// URL (frontend generating QR code)
 
    public TwoFactorSetUpDTO(String secret, String qrUrl) {
        this.secret = secret;
        this.qrUrl = qrUrl;
    }
 
    public String getSecret() { return secret; }
    public String getQrUrl() { return qrUrl; }
}
