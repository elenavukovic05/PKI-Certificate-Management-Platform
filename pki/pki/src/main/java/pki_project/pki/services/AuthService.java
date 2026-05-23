package pki_project.pki.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.LoginRequestDTO;
import pki_project.pki.dto.LoginResponseDTO;
import pki_project.pki.model.User;
import pki_project.pki.security.JwtUtil;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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
            return new LoginResponseDTO(tempToken, true);
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new LoginResponseDTO(token, false);
    }

}
