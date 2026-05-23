package pki_project.pki.dto;

public class TwoFactorVerifyDTO {
    private int code;   // 6 nums TOTP code from auth app
 
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
}
