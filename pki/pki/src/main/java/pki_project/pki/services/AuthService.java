package pki_project.pki.services;

import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.*;
import pki_project.pki.model.Organization;
import pki_project.pki.model.RefreshTokenEntity;
import pki_project.pki.model.User;
import pki_project.pki.model.enums.Role;
import pki_project.pki.repositories.OrganizationRepository;
import pki_project.pki.repositories.UserRepository;
import pki_project.pki.security.JwtUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EmailService emailService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    CryptoService cryptoService;

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

    public boolean containsSpecialCharacters(String password){
        for(char ch : password.toCharArray()){
            boolean isDigit = ch >= '0' && ch <= '9';
            boolean isChar = (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
            if(!isDigit && !isChar)
                return true;
        }
        return false;
    }
    public boolean containsDigits(String password){
        for(char ch : password.toCharArray()){
            if(ch >= '0' && ch <= '9')
                return true;
        }
        return false;
    }

    public void activateUser(ActivateUserDTO dto){
        User user = userRepository.findByActivationToken(dto.getActivationToken())
                .orElseThrow(()-> new RuntimeException("Invalid activation token"));

        if(user.getActivationTokenExpiresAt().before(Date.from(Instant.now())))
            throw new RuntimeException("Activation token expired");

        if(!user.getActivationChallenge().equals(dto.getDecryptedActivationChallenge()))
            throw new RuntimeException("Invalid activation");
        user.setEnabled(true);
        user.setActivationChallenge(null);
        user.setEncryptedActivationChallenge(null);
        user.setActivationToken(null);
        userRepository.save(user);
    }

    public String getActivationChallenge(String activationToken){
        User user = userRepository.findByActivationToken(activationToken)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));
        String activationChallenge = user.getEncryptedActivationChallenge();
        if(activationChallenge == null)
            throw new RuntimeException("Activation challenge not found");
        return  activationChallenge;
    }

    public void register(RegisterDTO dto){
        if(dto.getUsername() == null)
            throw new RuntimeException("Username can not be empty!");

        if(dto.getPassword() == null)
            throw new RuntimeException("Password can not be empty!");

        if(dto.getConfirmedPassword() == null)
            throw new RuntimeException("Confirmed password can not be empty!");

        if(dto.getName() == null)
            throw new RuntimeException("Name can not be empty!");

        if(dto.getLastName() == null)
            throw new RuntimeException("Last name can not be empty!");

        if(dto.getOrganizationId() == null)
            throw new RuntimeException("Organization can not be empty!");

        if(dto.getPublicKey() == null)
            throw new RuntimeException("Public key can not be empty!");

        if(!dto.getPassword().equals(dto.getConfirmedPassword()))
            throw new RuntimeException("Password and confirmed password need to match!");

        if(dto.getPassword().length() < 8 || !containsSpecialCharacters(dto.getPassword())
            || !containsDigits(dto.getPassword()))
            throw new RuntimeException("Password must have at least 8 characters, at least one special character and at least one digit!");

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("User already exists");
        }
        String publicKeyPem = dto.getPublicKey()
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPem);

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        Organization organization = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        User newUser = new User(dto.getUsername(), encodedPassword, dto.getName(), dto.getLastName(),
                publicKeyBytes, Role.USER, organization);

        String token = UUID.randomUUID().toString();
        newUser.setEnabled(false);
        newUser.setActivationToken(token);
        newUser.setActivationTokenExpiresAt(
                Date.from(Instant.now().plus(24, ChronoUnit.HOURS))
        );
        //encrypting random string with user's public key for verification
        String challenge = UUID.randomUUID().toString();
        String encryptedActivationChallenge = cryptoService.encryptWithPublicKey(challenge, publicKeyBytes);
        newUser.setActivationChallenge(challenge);
        newUser.setEncryptedActivationChallenge(encryptedActivationChallenge);
        userRepository.save(newUser);
        emailService.sendUserActivationEmail(newUser.getUsername(), token);
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
