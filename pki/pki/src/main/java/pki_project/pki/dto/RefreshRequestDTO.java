package pki_project.pki.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequestDTO {
    @NotBlank
    String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
