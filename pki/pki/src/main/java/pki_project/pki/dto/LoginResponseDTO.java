package pki_project.pki.dto;

public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private boolean requires2FA; // true = 2fa on, token is temp -> qr code, false = normal, logged in

    public LoginResponseDTO(String accessToken, String refreshToken, boolean requires2FA) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.requires2FA = requires2FA;
    }

    public String getAccessToken() { return accessToken; }

    public boolean isRequires2FA() { return requires2FA; }

    public String getRefreshToken() { return refreshToken; }
}
