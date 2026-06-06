package pki_project.pki.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.LoginRequestDTO;
import pki_project.pki.dto.LoginResponseDTO;
import pki_project.pki.dto.RefreshRequestDTO;
import pki_project.pki.model.RefreshTokenEntity;
import pki_project.pki.model.User;
import pki_project.pki.security.JwtUtil;

import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenService refreshTokenService){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponseDTO login(LoginRequestDTO request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        if (user.isTwoFactorEnabled()) {
            String tempToken = jwtUtil.generateTemp2FAToken(
                    user.getUsername(),
                    user.getRole().name()
            );
            // requires2FA = true we need entering code on front page
            return new LoginResponseDTO(tempToken, null, true);
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        String refreshToken = refreshTokenService.generateToken(user, UUID.randomUUID().toString());

        return new LoginResponseDTO(token, refreshToken, false);
    }

    public LoginResponseDTO refreshTokens(RefreshRequestDTO request){
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.findByToken(request.getRefreshToken());

        if (!refreshTokenService.validateRefreshToken(refreshTokenEntity)){
            throw new RuntimeException("Refresh token is not valid");
        }

        refreshTokenService.setInvalid(refreshTokenEntity);

        String accessToken = jwtUtil.generateToken(
                refreshTokenEntity.getUser().getUsername(),
                refreshTokenEntity.getUser().getRole().name()
        );

        String refreshToken = refreshTokenService.generateToken(refreshTokenEntity.getUser(), refreshTokenEntity.getFamilyId());

        return new LoginResponseDTO(accessToken, refreshToken, false);
    }

    public void logout(RefreshRequestDTO request) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.findByToken(request.getRefreshToken());

        refreshTokenService.setFamilyInvalid(refreshTokenEntity);
    }

}
